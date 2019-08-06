from http.server import HTTPServer

from src.daemonizer import Daemonizer
from src.webserver import Webserver, hostPort, hostName

if __name__ == '__main__':
    """ Run the webserver in the background. """
    Daemonizer().start()
    HTTPServer((hostName, hostPort), Webserver).serve_forever()
