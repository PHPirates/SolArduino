import urllib.parse
from http.server import BaseHTTPRequestHandler

from src.panel_control.panel_controller import PanelController

# hostName = '192.168.8.42'
hostName = 'localhost'
hostPort = 8080


class Webserver(BaseHTTPRequestHandler):
    """
    Serve an http server.
    """

    panel_controller = PanelController()

    def append_content(self, content):
        self.wfile.write(bytes(content, 'utf-8'))

    # noinspection PyPep8Naming
    def do_GET(self):
        self.send_response(200)
        self.send_header('Content-type', 'text/html')
        self.end_headers()

        preamble = '<html><head><title>SolArduino Pi</title></head><body>'
        self.append_content(preamble)

        emergency = self.panel_controller.emergency
        if emergency.is_set:
            self.append_content(f'Emergency: {emergency.message}')
        else:
            try:
                url_params = urllib.parse.parse_qs(self.path[2:])
                self.parse_params(url_params)
            except Exception as e:
                emergency.set(str(e))
                self.append_content(str(e))

        self.append_content('</body></html>')

    def parse_params(self, url_params):
        # todo always return angle and auto/manual
        self.append_content(self.panel_controller.get_angle() + ' manual/auto')
        # todo return something easy to parse for client

        # No parameters given.
        if not url_params.keys():
            self.append_content('No parameters were found in the request. '
                                'Available parameters: '
                                'panel=[up/down/auto/stop], '
                                'update=[true/false], '
                                'degrees=[number]')

        if 'panel' in url_params.keys():
            try:
                message = self.panel_controller.move_panels(
                    url_params['panel'])
                self.append_content(message)
            except ValueError as e:
                self.append_content(str(e))
                return

        if 'degrees' in url_params.keys():
            self.append_content('Not implemented yet')
            # todo make sure to handle multiple params properly
