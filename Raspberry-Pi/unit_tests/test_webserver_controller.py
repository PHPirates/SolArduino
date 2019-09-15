import cherrypy
from cherrypy.process.plugins import Daemonizer, PIDFile

from solarduino import kill_if_exists, pid_path
from unit_tests.test_panel_control import panels_up, panels_stop, panels_down


class Root(object):
    @cherrypy.expose
    def index(self, **params):
        if 'panel' in params:
            if params['panel'] == 'up':
                panels_up()
            elif params['panel'] == 'down':
                panels_down()
            elif params['panel'] == 'stop':
                panels_stop()

        return "You sent me " + str(params)


if __name__ == '__main__':
    cherrypy.engine.exit()
    kill_if_exists()
    PIDFile(cherrypy.engine, pid_path).subscribe()
    # Daemonizer(cherrypy.engine).subscribe()
    cherrypy.config.update({'server.socket_host': '192.168.8.42',
                            'server.socket_port': 8080,
                            })
    cherrypy.quickstart(Root(), '/')
