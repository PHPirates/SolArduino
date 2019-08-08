from contextlib import redirect_stderr
from http.server import HTTPServer

from src.daemonizer import Daemonizer
from src.webserver import Webserver, hostPort, hostName

if __name__ == '__main__':
    """ Start SolArduino. """
    # Run in the background
    Daemonizer().start()

    # Start webserver
    with open('solarduino.err.log', 'w') as stderr, redirect_stderr(stderr):
        HTTPServer((hostName, hostPort), Webserver).serve_forever()
