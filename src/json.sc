using import Array Map String print
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
                inline (fT)
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
                getattr enum-type k
    elseif (vT < Enum)

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

inline parse-decode (T source)
    json-object := cJSON.ParseWithLength source (countof source)
    result := decode-struct T json-object
    cJSON.Delete json-object
    result

do
    let parse-decode decode-value get-object-item
    local-scope;
