import cherrypy
from cherrypy.process.plugins import Daemonizer


class Root(object):
    @cherrypy.expose
    def index(self, **params):

        return "You sent me " + str(params)


if __name__ == '__main__':
    Daemonizer(cherrypy.engine).subscribe()
    cherrypy.config.update({'server.socket_host': '192.168.178.42',
                            'server.socket_port': 8082,
                            })
    cherrypy.quickstart(Root(), '/')
