using import Array Map String print
import cJSON

inline check-type (item checkf)
    if (not (checkf item))
        raise false

inline decode-struct

inline decode-value (vT item)
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
    else (raise false)

inline decode-struct (sT object)
    sT.__typecall sT
        va-map
            inline (field)
                fT := field.Type
                k T := keyof fT, unqualified fT
                try (decode-value T (cJSON.GetObjectItem object (static-eval (k as string))))
                then (value) value
                else (T)
            sT.__fields__

inline parse-typed (T source)
    json-object := cJSON.ParseWithLength source (countof source)
    print
        'from-rawstring String (cJSON.Print json-object)
    result := decode-struct T json-object
    cJSON.Delete json-object
    result

do
    let parse-typed
    local-scope;
