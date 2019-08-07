from http.server import HTTPServer

from src.daemonizer import Daemonizer
from src.panel_controller import PanelController
from src.webserver import Webserver, hostPort, hostName

if __name__ == '__main__':
    """ Start SolArduino. """
    # Run in the background
    Daemonizer().start()

    # Start webserver
    HTTPServer((hostName, hostPort), Webserver).serve_forever()
