import platform
from doit.tools import LongRunning

DOIT_CONFIG = {'default_tasks': ['update_bottle', 'run_game', "dist"], 'verbosity': 2}

def cmd(cmd):
    return f"bash -c \"{cmd}\""

bottle_dep = "lib/scopes/packages/bottle/BOTTLE_VERSION"
def task_install_bottle():
    return {
        'actions': [cmd("../bottle/install")],
        'targets': [bottle_dep],
        'uptodate': [True],
    }

def task_update_bottle():
    return {
        'actions': [cmd("../bottle/install")],
        'uptodate': [False],
    }

def task_run_game():
    return {
        'actions': [LongRunning(cmd("scopes -e -m .src.main"))],
        'uptodate': [False],
        'file_dep': [bottle_dep]
    }

def notdone(name):
    return {
        'basename': name,
        'actions': [f"echo {name} to be implemented"]
    }

def task_artifact():
    return {
        'actions': [cmd("rm -rf ./dist"), cmd("mkdir ./dist"), cmd("mkdir ./dist/obj"), cmd("mkdir ./dist/bin"),
                    "scopes -e -m .build",
                    "rm -r ./dist/obj",
                    "cp -r game ./dist"],
        'targets': ["./dist/bin/game"],
        'file_dep': [bottle_dep],
        'uptodate': [False],
    }


def task_push_itch():
    return {
        'actions': [f"./tools/butler push ./dist radgerayden/gloopmancer:development-{platform.system()}"],
        'file_dep': ["./dist/bin/game"],
    }
