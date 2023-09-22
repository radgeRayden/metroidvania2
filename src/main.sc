using import glm Option print String struct
using import radl.version-string radl.strfmt

import bottle
using bottle.enums

VERSION := (git-version) as string
run-stage;

using import .CollisionWorld

global world : CollisionWorld
global player-vel : vec2
global player-collider : ColliderId
global colliding? : bool
global player2-collider : ColliderId
global colliding2? : bool

global root-dir : (Option String)
@@ 'on bottle.configure
fn (cfg)
    cfg.window.title = f"gloopmancer - ${VERSION}"
    cfg.window.width = 800
    cfg.window.height = 600

    try ('unwrap root-dir)
    then (dir)
        print dir
        cfg.filesystem.root = copy dir

@@ 'on bottle.load
fn ()
    print f"bottle version: ${(bottle.get-version)}"

    # load level data
    # lets make up a level for testing
    w-tiles := 800 // 32
    for i in (range w-tiles)
        if (i % 2 == 0)
            'add world
                Collider (vec2 (i * 32 + 16) 16) (CollisionShape.AABB (vec2 16))

    player-collider =
        'add world (Collider (vec2 110 100) (CollisionShape.Circle 16))
    player2-collider =
        'add world (Collider (vec2 300 100) (CollisionShape.AABB (vec2 16)))

    # spawn entities

@@ 'on bottle.key-pressed
fn (key)
    if (key == KeyboardKey.Escape)
        bottle.quit!;
    elseif ((key == KeyboardKey.Return) and (bottle.keyboard.down? KeyboardKey.LAlt))
        bottle.window.toggle-fullscreen;
    elseif (key == KeyboardKey.Down)
        player-vel = vec2 0 -1
    elseif (key == KeyboardKey.Up)
        player-vel = vec2 0 1

@@ 'on bottle.key-released
fn (key)
    if (key == KeyboardKey.Up)
        player-vel = (vec2)
    elseif (key == KeyboardKey.Down)
        player-vel = (vec2)

@@ 'on bottle.update
fn (dt)
    colliding? = 'move world player-collider (player-vel * 80 * (f32 dt)) null
    colliding2? = 'move world player2-collider (player-vel * 80 * (f32 dt)) null

@@ 'on bottle.render
fn ()
    from bottle let plonk

    player-pos := 'get-position world player-collider
    plonk.circle player-pos 16.0 (color = (colliding? (vec4 1 0 0 1) (vec4 1)))

    player2-pos := 'get-position world player2-collider
    plonk.rectangle player2-pos (vec2 32) (color = (colliding2? (vec4 1 0 0 1) (vec4 1)))

    for k v in world.objects
        dispatch v.shape
        case Circle (radius)
            plonk.circle-line v.position radius
        case AABB (half-size)
            plonk.rectangle-line v.position (half-size * 2)
        default (unreachable)

fn main (argc argv)
    if (argc > 1)
        root-dir = 'from-rawstring String (argv @ 1)
    bottle.run;
    0

sugar-if main-module?
    name argc argv := (script-launch-args)

    # make it appear as if it was launched as a regular executable
    argv* := alloca-array rawstring (argc + 1)
    argv* @ 0 = name as rawstring
    for i in (range argc)
        argv* @ (i + 1) = (argv @ i)
    main (argc + 1) argv*
else
    main
