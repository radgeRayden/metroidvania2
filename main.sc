using import String
using import struct
using import Rc

import bottle

using bottle.input
using bottle.enums

inline sign (v)
    (typeof v)
        (i8 (v > 0)) - (i8 (v < 0))

struct Player
    x : i32
    y : i32
    dir : f32

    fn jump (self)
        print "jump"

    fn set-direction (self dir)
        self.dir = dir

struct GameContext
    player : Player

global game-context : GameContext
global player-layer : (Rc InputLayer)

@@ 'on bottle.load
fn ()
    player-layer = (bottle.input.new-layer)

    # for a specific, instant action, a virtual button is the best approach.
    fn player-jump ()
        'jump game-context.player

    'define-button player-layer "jump"
    'map-action player-layer "jump" player-jump ButtonInput.Pressed
    'bind-to-button player-layer "jump" ControllerButton.A
    'bind-to-button player-layer "jump" KeyboardKey.Space

    # for something like player movement where you want to take a value indicating direction,
    # an axis is recommended. Our movement is completely binary, so we just flatten the input using sign.
    fn player-move (dir)
        # deadzone hack
        dir :=
            ? ((abs dir) < 0.05) 0.0 dir

        if (((abs dir) > 0.5) or (dir == 0.0))
            'set-direction game-context.player (sign dir)

    'define-axis player-layer "move"
    'map-action player-layer "move" player-move
    'bind-to-axis player-layer "move" ControllerAxis.LeftX
    # for digital inputs, bind-to-axis takes the value that will be triggered on button press.
    'bind-to-axis player-layer "move" ControllerButton.Left -1.0
    'bind-to-axis player-layer "move" ControllerButton.Right 1.0
    'bind-to-axis player-layer "move" KeyboardKey.Left -1.0
    'bind-to-axis player-layer "move" KeyboardKey.Right 1.0

    # for some inputs we only care about their continuous pressed state, so we don't map an action
    # to it. It can be queried inside update.
    'define-button player-layer "run"
    'bind-to-button player-layer "run" ControllerButton.RightBumper
    'bind-to-button player-layer "run" ControllerAxis.TriggerRight 1.0
    'bind-to-button player-layer "run" KeyboardKey.LShift

@@ 'on bottle.update
fn (dt)
    print ('button-down? player-layer "run")

bottle.run;
