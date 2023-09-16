using import enum glm Map struct

enum CollisionShape
    Circle : (radius = f32)
    AABB : (half-size = vec2)

struct Collider
    position : vec2
    shape : CollisionShape

fn AABB-AABB (a-pos a-hs b-pos b-hs)
    # dv := b-pos - a-pos
    false

struct CollisionWorld
    objects : (Map usize Collider)
    _next-id : usize

    fn next-id (self)
        id := copy self._next-id
        self._next-id += 1
        id

    fn... add (self, collider : Collider)
        id := 'next-id self
        'set self.objects id collider
        id

    fn move (self id translation)
        try
            object := 'get self.objects id
            object.position += translation
            for k v in objects
                if (k != id)
                    aabb := 'unsafe-extract-payload object.shape vec2
                    dispatch v.shape
                    case Circle
                        ()
                    case AABB (half-size)
                        if (AABB-AABB object.position aabb v.position half-size)
                            return true
        else false

    fn get-position (self id)
        try
            object := 'get self.objects id
            copy object.position
        else
            vec2;
do
    let CollisionShape Collider CollisionWorld
    local-scope;
