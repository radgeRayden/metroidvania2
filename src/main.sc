using import Option print String
using import radl.version-string radl.strfmt

import bottle
using bottle.enums

VERSION := (git-version) as string
run-stage;

global root-dir : (Option String)
@@ 'on bottle.configure
fn (cfg)
    cfg.window.title = f"gloopmancer - ${VERSION}"

    try ('unwrap root-dir)
    then (dir)
        print dir
        cfg.filesystem.root = copy dir

@@ 'on bottle.load
fn ()
    print f"bottle version: ${(bottle.get-version)}"

@@ 'on bottle.key-pressed
fn (key)
    if (key == KeyboardKey.Escape)
        bottle.quit!;
    elseif ((key == KeyboardKey.Return) and (bottle.keyboard.down? KeyboardKey.LAlt))
        bottle.window.toggle-fullscreen;

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
