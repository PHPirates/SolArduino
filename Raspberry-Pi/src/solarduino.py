import cherrypy
import psutil
from cherrypy.process.plugins import Daemonizer, PIDFile

from src.webserver.webserver import Webserver


def kill_if_exists(pid_path: str):
    """ Kill current process if it is running. """
    try:
        with open(pid_path, 'r') as f:
            pid = int(f.read())

        process = psutil.Process(pid)
        process.terminate()
    except FileNotFoundError:
        pass


if __name__ == '__main__':
    """ Start SolArduino. """
    pid_path = '/tmp/solarduino.pid'
    cherrypy.engine.exit()
    kill_if_exists(pid_path)
    PIDFile(cherrypy.engine, pid_path).subscribe()
    # todo don't daemonize when Pycharm debugging
    # Daemonizer(cherrypy.engine,
    #            stdout='solarduino_access.log',
    #            stderr='solarduino_error.log').subscribe()
    cherrypy.config.update({'server.socket_host': '192.168.178.42',
                            'server.socket_port': 8080,
                            })
    cherrypy.quickstart(Webserver(), '')
