# import cherrypy
# from cherrypy.process.plugins import Daemonizer
# d = Daemonizer(cherrypy.engine)
# d.subscribe()

import cherrypy


class Root(object):
    @cherrypy.expose
    def index(self):
        return "Hello World!"


if __name__ == '__main__':
    cherrypy.config.update({'server.socket_host': '192.168.178.42',
                            'server.socket_port': 8082,
                            })
    cherrypy.quickstart(Root(), '/')
