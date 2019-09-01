import socket
import subprocess
import sys

import cherrypy
import psutil
from cherrypy.process.plugins import Daemonizer, PIDFile

from src.webserver.webserver import Webserver


def get_ip() -> str:
    s = socket.socket(socket.AF_INET, socket.SOCK_DGRAM)
    s.connect(("8.8.8.8", 80))
    ip = s.getsockname()[0]
    s.close()
    return ip


def kill_if_exists():
    """ Kill current process if it is running. """
    try:
        with open(pid_path, 'r') as f:
            pid = int(f.read())

        process = psutil.Process(pid)
        process.terminate()
    except psutil.AccessDenied:
        subprocess.check_call(['sudo', 'kill', str(pid)])
    except FileNotFoundError:
        pass


if __name__ == '__main__':
    """ Start SolArduino. """
    pid_path = '/tmp/solarduino.pid'
    cherrypy.engine.exit()
    kill_if_exists()
    PIDFile(cherrypy.engine, pid_path).subscribe()
    # Don't daemonize when Pycharm is debugging
    # gettrace = getattr(sys, 'gettrace', None)
    # if gettrace is None or not gettrace():
    #     Daemonizer(cherrypy.engine,
    #                stdout='logs/solarduino_access.log',
    #                stderr='logs/solarduino_error.log').subscribe()

    cherrypy.config.update({'server.socket_host': get_ip(),
                            'server.socket_port': 8080,
                            })
    cherrypy.quickstart(Webserver(), '')
