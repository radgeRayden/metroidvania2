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

    }


def task_dist():
    return notdone("dist")
