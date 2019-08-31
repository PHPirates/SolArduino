import cherrypy
from cherrypy.process.plugins import Daemonizer

from unit_tests.test_panel_control import panels_up, panels_stop, panels_down


class Root(object):
    @cherrypy.expose
    def index(self, **params):
        if params['panel'] == 'up':
            panels_up()
        elif params['panel'] == 'down':
            panels_down()
        elif params['panel'] == 'stop':
            panels_stop()

        return "You sent me " + str(params)


if __name__ == '__main__':
    Daemonizer(cherrypy.engine).subscribe()
    cherrypy.config.update({'server.socket_host': '192.168.8.42',
                            'server.socket_port': 8081,
                            })
    cherrypy.quickstart(Root(), '/')
