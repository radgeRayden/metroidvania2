using import Array enum glm hash Map struct

MAX-RESOLUTION-ATTEMPTS := 16

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
    trigger? : bool
    index : i32

inline mag-squared (v)
    v.x ** 2 + v.y ** 2

vvv bind collision-tests
do
    fn AABB-AABB (p1 s1 p2 s2)
        dv := p2 - p1
        s := sign dv
        c1 := clamp (p1 - s1) (p2 - s2) (p2 + s2)
        c2 := clamp (p1 + s1) (p2 - s2) (p2 + s2)
        ext := (c2 - c1)
        p := ext * s
        _
            & (unpack ((abs (p2 - p1)) < (s1 + s2)))
            if ((abs p.x) > (abs p.y))
                if (p.y == 0)
                    vec2 p.x (0.001 * dv.y)
                else
                    vec2 (0.001 * dv.x) p.y
            else
                if (p.x == 0)
                    vec2 (0.001 * dv.x) p.y
                else
                    vec2 p.x (0.001 * dv.y)

    fn AABB-Circle (aabb-pos aabb-hs circle-pos radius)
        # https://stackoverflow.com/a/1879223
        # Find the closest point to the circle within the rectangle
        closest-point := clamp circle-pos (aabb-pos - aabb-hs) (aabb-pos + aabb-hs)
        # Calculate the distance between the circle's center and this closest point (manhattan distance)
        dv := circle-pos - closest-point
        dist2 := dv.x ** 2 + dv.y ** 2
        # If the distance is less than the circle's radius, an intersection occurs
        _
            dist2 < radius ** 2
            (normalize dv) * (radius - (length dv))

    Circle-AABB := (a b c d) -> (do (c? v := (AABB-Circle c d a b)) (c?, -v))

    fn Circle-Circle (a-pos a-radius b-pos b-radius)
        dv := b-pos - a-pos
        dist2 := dv.x ** 2 + dv.y ** 2
        _
            dist2 < (a-radius + b-radius) ** 2
            ((a-radius + b-radius) - (length dv)) * (normalize dv)

    local-scope;

struct CollisionData
    active-object : ColliderId
    passive-object : ColliderId
    msv : vec2

enum CollisionResponse plain
    Slide
    Trigger
    NoCollision

CollisionResponseCallback := @ (function CollisionResponse (viewof CollisionData))

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

    fn... move (self, id : ColliderId, translation : vec2, cb : CollisionResponseCallback)
        try
            object := 'get self.objects id
            object.position += translation

            loop (unresolved? attempts = true 0)
                if ((not unresolved?) or (attempts >= MAX-RESOLUTION-ATTEMPTS))
                    break (attempts > 1)

                for k other in self.objects
                    if (k != id)
                        'apply object.shape
                            inline (aT ...)
                                a... := _ object.position (va-dekey ...)
                                'apply other.shape
                                    inline (bT ...)
                                        b... := _ other.position (va-dekey ...)
                                        testf := get-test-function aT bT
                                        collided? msv := testf ((va-join a...) b...)
                                        if collided?
                                            'append self._collision-list
                                                CollisionData
                                                    active-object = copy id
                                                    passive-object = copy k
                                                    msv = msv

                'sort self._collision-list ((x) -> (mag-squared x.msv))
                collided? := copy ((countof self._collision-list) > 0)
                if collided?
                    collision-data := self._collision-list @ 0
                    object.position -= collision-data.msv

                for i c in (enumerate self._collision-list)
                    ('get self.objects c.passive-object) . index = (i + 1)

                'clear self._collision-list

                _ collided? (attempts + 1)
        else false
    case (self, id : ColliderId, translation : vec2)
        fn default-callback (data)
            CollisionResponse.Slide

        this-function
            (va-join *...) default-callback

    fn... get-position (self, id : ColliderId)
        try
            object := 'get self.objects id
            copy object.position
        else
            vec2;

    fn center-distance (self a b)
        ap bp := 'get-position self a, 'get-position self b
        b - a

    fn contact-point (self a b)
do
    let CollisionShape Collider ColliderId CollisionWorld
    local-scope;
