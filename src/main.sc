using import glm Option print String struct
using import radl.version-string radl.strfmt

import bottle
using bottle.enums

VERSION := (git-version) as string
run-stage;

using import .CollisionWorld

global world : CollisionWorld
global player-collider : usize
global player-vel : vec2

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
        'add world
            Collider (vec2 (i * 32 + 16) 16) (CollisionShape.AABB (vec2 16))

    player-collider =
        'add world (Collider (vec2 100 100) (CollisionShape.AABB (vec2 16)))

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
    'move world player-collider (player-vel * 200 * (f32 dt))

@@ 'on bottle.render
fn ()
    from bottle let plonk

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
