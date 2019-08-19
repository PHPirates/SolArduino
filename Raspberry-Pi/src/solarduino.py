import cherrypy
from cherrypy.process.plugins import Daemonizer

from src.webserver.webserver import Webserver

if __name__ == '__main__':
    """ Start SolArduino. """
    Daemonizer(cherrypy.engine,
               stdout='solarduino_access.log',
               stderr='solarduino_error.log').subscribe()
    cherrypy.config.update({'server.socket_host': '192.168.178.42',
                            'server.socket_port': 8080,
                            })
    cherrypy.quickstart(Webserver(), '')
