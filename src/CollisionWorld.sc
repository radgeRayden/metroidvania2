using import Array enum glm hash Map struct

typedef ColliderId : (storageof usize)
    inline __typecall (cls value)
        bitcast (value and value or 0:usize) cls
    inline __hash (self)
        hash (storagecast self)
    inline __copy (self)
        this-type
            copy (storagecast self)
    inline __== (thisT otherT)
        static-if (thisT == otherT)
            inline (self other)
                (storagecast self) == (storagecast other)

enum CollisionShape
    Circle : (radius = f32)
    AABB : (half-size = vec2)

struct Collider
    position : vec2
    shape : CollisionShape

inline mag-squared (v)
    v.x ** 2 + v.y ** 2

vvv bind collision-tests
do
    fn AABB-AABB (a-pos a-hs b-pos b-hs)
        _
            & (unpack ((abs (b-pos - a-pos)) <= (a-hs + b-hs)))
            (vec2)

    fn AABB-Circle (aabb-pos aabb-hs circle-pos radius)
        # https://stackoverflow.com/a/1879223
        # Find the closest point to the circle within the rectangle
        closest-point := clamp circle-pos (aabb-pos - aabb-hs) (aabb-pos + aabb-hs)
        # Calculate the distance between the circle's center and this closest point
        dv := circle-pos - closest-point
        dist2 := dv.x ** 2 + dv.y ** 2
        # If the distance is less than the circle's radius, an intersection occurs
        _
            dist2 <= radius ** 2
            (vec2)

    Circle-AABB := (a b c d) -> (AABB-Circle c d a b)

    fn Circle-Circle (a-pos a-radius b-pos b-radius)
        dv := b-pos - a-pos
        dist2 := dv.x ** 2 + dv.y ** 2
        _
            dist2 <= (a-radius + b-radius) ** 2
            b-radius - a-radius

    local-scope;

struct CollisionData
    active-object : ColliderId
    passive-object : ColliderId
    penetration : vec2

typedef CollisionResolutionCallback <<: (pointer (function vec2 CollisionData))

@@ memo
inline get-test-function (shapeA shapeB)
    getattr collision-tests (Symbol (.. (shapeA.Name as zarray) "-" (shapeB.Name as zarray)))

struct CollisionWorld
    objects : (Map ColliderId Collider)
    _next-id : ColliderId
    _collision-list : (Array CollisionData)

    fn next-id (self)
        id := copy self._next-id
        (storagecast self._next-id) += 1
        id

    fn... add (self, collider : Collider)
        id := 'next-id self
        'set self.objects id collider
        id

    fn... move (self, id : ColliderId, translation : vec2, cb : CollisionResolutionCallback)
        try
            object := 'get self.objects id
            object.position += translation

            for k other in self.objects
                if (k != id)
                    'apply object.shape
                        inline (aT ...)
                            a... := _ object.position (va-dekey ...)
                            'apply other.shape
                                inline (bT ...)
                                    b... := _ other.position (va-dekey ...)
                                    testf := get-test-function aT bT
                                    collided? penetration := testf ((va-join a...) b...)
                                    if collided?
                                        'append self._collision-list
                                            CollisionData
                                                active-object = copy id
                                                passive-object = copy k
                                                penetration = penetration

            'sort self._collision-list ((x) -> (mag-squared x.penetration))
            collided? := copy ((countof self._collision-list) > 0)
            for collision in self._collision-list
                cb collision
            'clear self._collision-list
            collided?
        else false

    fn... get-position (self, id : ColliderId)
        try
            object := 'get self.objects id
            copy object.position
        else
            vec2;
do
    let CollisionShape Collider ColliderId CollisionWorld
    local-scope;
