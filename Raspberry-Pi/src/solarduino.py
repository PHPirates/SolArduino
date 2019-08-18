from http.server import HTTPServer
from socketserver import ThreadingMixIn

from src.webserver.webserver import hostPort, hostName, Webserver
from util.daemonizer import Daemonizer


class ThreadedHTTPServer(ThreadingMixIn, HTTPServer):
    """Handle requests in a separate thread."""

if __name__ == '__main__':
    """ Start SolArduino. """
    # Run in the background
    # todo webserver doesn't work in background?

    # Start webserver
    # with open('solarduino.err.log', 'w') as stderr, redirect_stderr(stderr):
    server = ThreadedHTTPServer(
        (hostName, hostPort),
        Webserver)

    daemon_context = Daemonizer().start(server.fileno())

    with daemon_context:
        server.serve_forever()

