import logging
from http.server import HTTPServer, BaseHTTPRequestHandler

from pep3143daemon import DaemonContext, PidFile


class MyServer(BaseHTTPRequestHandler):
    def do_GET(self):
        self.send_response(200)
        self.send_header("Content-type", "text/html")
        self.end_headers()
        self.wfile.write(bytes("test", "utf-8"))

    # def log_message(self, format, *args):
    #     pass


# Make the HTTP Server instance.
server = HTTPServer(('192.168.178.42', 8081), MyServer)

# Make the context manager for becoming a daemon process.

pidfile = PidFile('/tmp/test_daemon_webserver.pid')
daemon_context = DaemonContext(pidfile=pidfile)
daemon_context.files_preserve = [server.fileno()]

# Get the default logger
default_logger = logging.getLogger('')

# Add the handler
default_logger.addHandler(server)

# Remove the default stream handler
for handler in default_logger.handlers:
    if isinstance(handler, logging.StreamHandler):
        default_logger.removeHandler(handler)

# Become a daemon process.
with daemon_context:
    server.serve_forever()
