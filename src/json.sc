using import Array enum Map Option String print
import cJSON

fn tolower (str)
    using import String

    delta := char"a" - char"A"
    local result : String
    for c in str
        if (c >= char"A" and c <= char"Z")
            'append result (c + delta)
        else
            'append result c
    result

inline match-string-enum (enum-type value f)
    using import hash
    using import switcher
    using import print

    call
        switcher sw
            va-map
                inline (field)
                    fT := field.Type
                    k := keyof fT
                    case (static-eval (hash (tolower (k as string))))
                        f k
                enum-type.__fields__
            default
                raise;
        hash (tolower value)

inline check-type (item checkf)
    if (not (checkf item))
        raise false

inline decode-struct

inline decode-value (vT item ...)
    if (cJSON.IsInvalid item)
        raise false

    static-if ((vT < real) or (vT < integer))
        check-type item cJSON.IsNumber
        (cJSON.GetNumberValue item) as vT
    elseif (vT == String)
        check-type item cJSON.IsString
        'from-rawstring String (cJSON.GetStringValue item)
    elseif (vT == bool)
        check-type item cJSON.IsBool
        not (cJSON.IsFalse item)
    elseif (vT < Array)
        check-type item cJSON.IsArray
        local arr : vT

        loop (element = item.child)
            if (element == null)
                break;
            'append arr (this-function vT.ElementType element)
            element.next
    elseif ((vT < Struct) or (vT < CStruct))
        check-type item cJSON.IsObject
        decode-struct vT item
    elseif (vT < CEnum)
        check-type item cJSON.IsString
        match-string-enum vT
            'from-rawstring String (cJSON.GetStringValue item)
            inline (k v)
                getattr vT k
    elseif (vT < Option)
        if (cJSON.IsNull item)
            (vT)
        else
            vT (this-function vT.Type item)
    else (raise false)

fn get-object-item (object key)
    cJSON.GetObjectItem object key

inline decode-struct (sT object)
    sT.__typecall sT
        va-map
            inline (field)
                fT := field.Type
                k T := keyof fT, unqualified fT
                try (decode-value T (get-object-item object (static-eval (k as string))))
                then (value) value
                else (T)
            sT.__fields__

inline parse-as-struct (T source)
    json-object := cJSON.ParseWithLength source (countof source)
    result := decode-struct T json-object
    cJSON.Delete json-object
    result

enum JSONValue
JSONObject := (Map String JSONValue)
JSONArray := (Array JSONValue)

SCString := String
enum JSONValue
    Object : JSONObject
    Array : JSONArray
    Number : f64
    Bool : bool
    String : SCString
    Null

    fn from-item (item)
        returning this-type
        if (cJSON.IsObject item)
            local map : JSONObject
            loop (element = item.child)
                if (element == null)
                    break;
                'set map ('from-rawstring SCString element.string) (this-function element)
                element.next
            this-type.Object map
        elseif (cJSON.IsArray item)
            local arr : JSONArray
            loop (element = item.child)
                if (element == null)
                    break;
                'append arr (this-function element)
                element.next
            this-type.Array arr
        elseif (cJSON.IsNumber item)
            this-type.Number (cJSON.GetNumberValue item)
        elseif (cJSON.IsBool item)
            this-type.Bool (not cJSON.IsFalse item)
        elseif (cJSON.IsString item)
            this-type.String ('from-rawstring SCString (cJSON.GetStringValue item))
        elseif (cJSON.IsNull item)
            this-type.Null;
        else (assert false)

    inline... @ (self, k : SCString)
        dispatch self
        case Object (obj)
            'getdefault obj k (this-type.Null)
        default (this-type.Null)
    case (self, idx : integer)
        dispatch self
        case Array (arr)
            if ((idx >= 0) and (idx < (countof arr)))
                deref (arr @ idx)
            else (this-type.Null)
        default (this-type.Null)

fn parse-generic (source)
    json-object := cJSON.ParseWithLength source (countof source)
    result := JSONValue.from-item json-object
    cJSON.Delete json-object
    result

do
    let parse-as-struct parse-generic decode-value get-object-item
    local-scope;
