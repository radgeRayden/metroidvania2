using import Array String struct

struct AseRect plain
    x : i32
    y : i32
    w : i32
    h : i32

struct AseSize plain
    w : i32
    h : i32

struct AseAnimationFrame
    filename : String
    frame : AseRect
    rotated : bool
    trimmed : bool
    spriteSourceSize : AseRect
    sourceSize : AseSize
    duration : i32

struct AseAnimationTag
    name : String
    from : i32
    to : i32
    direction : String

struct AseLayer
    name : String
    opacity : i32
    blendMode : String

struct AseMetadata
    image : String
    format : String
    size : AseSize
    scale : String
    frameTags : (Array AseAnimationTag)
    layers : (Array AseLayer)

struct AseSpriteSheet
    frames : (Array AseAnimationFrame)
    meta : AseMetadata

do
    local-scope;
