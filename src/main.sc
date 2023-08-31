using import print
using import radl.version-string radl.strfmt

import bottle
using bottle.enums

VERSION := (git-version)
run-stage;

@@ 'on bottle.configure
fn (cfg)
    cfg.window.title = f"gloopmancer - ${VERSION}"

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
    bottle.run;
    0

sugar-if main-module?
    main 0 0
else
    main
