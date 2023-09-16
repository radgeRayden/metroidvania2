using import enum glm Map struct

enum CollisionShape
    Circle : (radius = f32)
    AABB : (half-size = vec2)

struct Collider
    position : vec2
    shape : CollisionShape

vvv bind collision-tests
do
    fn AABB-AABB (a-pos a-hs b-pos b-hs)
        collision? := & (unpack ((abs (b-pos - a-pos)) <= (a-hs + b-hs)))

    fn AABB-Circle (a-pos a-hs b-pos b-radius)
        false

    Circle-AABB := (a b c d) -> (AABB-Circle c d a b)

    fn Circle-Circle (a-pos a-radius b-pos b-radius)
        dv := b-pos - a-pos
        dist2 := dv.x ** 2 + dv.y ** 2
        collision? := dist2 <= (a-radius + b-radius) ** 2

    local-scope;

inline get-test-function (shapeA shapeB)
    getattr collision-tests (static-eval (Symbol (.. (shapeA.Name as string) "-" (shapeB.Name as string))))

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
            :: collision-test
            for k other in self.objects
                if (k != id)
                    'apply object.shape
                        inline (aT ...)
                            a... := _ object.position (va-dekey ...)
                            'apply other.shape
                                inline (bT ...)
                                    b... := _ other.position (va-dekey ...)
                                    testf := get-test-function aT bT
                                    collided? := testf ((va-join a...) b...)
                                    if collided? (merge collision-test true)
            false
            collision-test ::
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
