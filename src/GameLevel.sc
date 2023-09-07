using import Array enum glm struct

enum LdtkLayerType plain
    Entities
    IntGrid
    Tiles
    AutoLayer

struct LdtkLayer
    __type : LdtkLayerType
    identifier : String
    uid : i32
    gridSize : i32
    displayOpacity : f32
    pxOffsetX : i32
    pxOffsetY : i32
    parallaxFactorX : f32
    parallaxFactorY : f32
    parallaxScaling : bool
    # intGridValues : [],
    tilesetDefUid : (Option i32)

struct LdtkDefinitions
    layers : (Array LdtkLayer)

struct LdtkBgPos
    cropRect : vec4
    scale : vec2
    topLeftPx : ivec2

struct LdtkNeighbourLevel
    dir : String
    levelIid : String

struct LdtkTileInstance
    a : f32
    f : u8
    px : ivec2
    src : ivec2
    t : i32

struct LdtkTilesetRectangle
    tilesetUid : i32
    w : i32
    h : i32
    x : i32
    y : i32

struct LdtkEnum
    type : String
    value : i32

enum LdtkFieldValue
    Int : i32
    Float : f32
    String : String
    Bool : bool
    Color : String
    Enum : LdtkEnum

    fn __json (self parent)
        enum-prefix := "Enum("
        value-kind := json.decode-value String (json.get-object-item parent "__type")
        if ((lslice value-kind (countof enum-prefix)) == enum-prefix)
            LdtkFieldValue.Enum
                LdtkEnum
                    slice value-kind (countof enum-prefix) ((countof value-kind) - 1)
                    json.decode-value i32 self
        else (json.decode-value this-type self value-kind)


struct LdtkFieldInstance
    __identifier : String
    __tile : (Option LdtkTilesetRectangle)
    __type : String
    __value : LdtkFieldValue
    defUid : i32

    let __json-defer-handlers =
        do
            inline __value (self value)
                value-kind := self.__type
                enum-prefix := "Enum("
                if ((lslice value-kind (countof enum-prefix)) == enum-prefix)
                    LdtkFieldValue.Enum
                        LdtkEnum
                            slice value-kind (countof enum-prefix) ((countof value-kind) - 1)
                            json.decode-value i32 value
                elseif (value-kind == "Int")
                    LdtkFieldValue.Int (json.decode-value i32 value)
                elseif (value-kind == "Float")
                    LdtkFieldValue.Float (json.decode-value f32 value)
                elseif (value-kind == "String")
                    LdtkFieldValue.String (json.decode-value String value)
                elseif (value-kind == "Bool")
                    LdtkFieldValue.Bool (json.decode-value bool value)
                elseif (value-kind == "Color")
                    LdtkFieldValue.Bool (json.decode-value String value)

            local-scope;

struct LdtkEntityInstance
    __grid : ivec2
    __identifier : String
    __pivot : vec2
    __smartColor : String
    __tags : (Array String)
    __tile : (Option LdtkTilesetRectangle)
    __worldX : i32
    __worldY : i32
    defUid : i32
    fieldInstances : (Array LdtkFieldInstance)

struct LdtkLayerInstance
    __cHei : i32
    __cWid : i32
    __gridSize : i32
    __identifier : String
    __opacity : f32
    __pxTotalOffsetX : i32
    __pxTotalOffsetY : i32
    __tilesetDefUid : (Option i32)
    __tilesetRelPath : (Option String)
    __type : LdtkLayerType
    autoLayerTiles : (Array LdtkTileInstance)
    entityInstances : (Array LdtkEntityInstance)
    gridTiles : (Array LdtkTileInstance)
    iid : String
    intGridCsv : (Array i32)
    layerDefUid : i32
    LevelId : i32
    overrideTilesetUid : (Option i32)
    pxOffsetX : i32
    pxOffsetY : i32
    visible : bool

struct LdtkLevel
    __bgcolor : String
    __bgPos : (Option LdtkBgPos)
    __neighbours : (Array LdtkNeighbourLevel)
    bgRelPath : (Option String)
    externalRelPath : (Option String)
    fieldInstances : (Array LdtkFieldInstance)
    identifier : String
    iid : String
    layerInstances : (Array LdtkLayerInstance)
    pxHei : i32
    pxWid : i32
    uid : i32
    worldDepth : i32
    worldX : i32
    worldY : i32

struct LdtkWorld
    identifier : String
    iid : String
    levels : (Array LdtkLevel)
    worldGridHeight : i32
    worldGridWidth : i32
    worldLayout : LdtkWorldLayout

struct LdtkProject
    defs : LdtkDefinitions
    levels : (Array LdtkLevel)
    worldGridHeight : (Option i32)
    worldGridWidth : (Option i32)
    worldLayout : (Option LdtkWorldLayout)
    worlds : (Array LdtkWorld)

struct GameLevel

do
    let GameLevel
    local-scope;
