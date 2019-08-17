import dataclasses
import json
import urllib.parse
import traceback
from http.server import BaseHTTPRequestHandler

from emergency import Emergency
from src.panel_control.panel_controller import PanelController
from webserver.http_response import HttpResponse

hostName = '192.168.178.42'  # todo get real hostname
hostPort = 8080


class Webserver(BaseHTTPRequestHandler):
    """
    Serve an http server.
    """

    panel_controller = PanelController()

    def write_dataclass(self, dataclass):
        """
        Put the dataclass as the content of the http response.
        """
        content = json.dumps(dataclasses.asdict(dataclass))
        self.wfile.write(bytes(content, 'utf-8'))

    def write_emergency(self, emergency: Emergency):
        response = HttpResponse(emergency=True,
                                angle=self.panel_controller.get_angle(),
                                mode='manual',
                                message=emergency.message)
        self.write_dataclass(response)

    # noinspection PyPep8Naming
    def do_GET(self):
        self.send_response(200)
        self.send_header('Content-type', 'application/json')
        self.end_headers()

        emergency = self.panel_controller.emergency
        if emergency.is_set:
            self.write_emergency(emergency)
        else:
            # noinspection PyBroadException
            try:
                url_params = urllib.parse.parse_qs(self.path[2:])
                self.parse_params(url_params)
            except Exception:
                emergency.set(traceback.format_exc())
                self.write_emergency(emergency)

        # self.append_content('</body></html>')

    def parse_params(self, url_params):
        message = ""

        # No parameters given.
        if not url_params.keys():
            message = 'No parameters were found in the request. Available parameters: panel=[up/down/auto/manual/stop], degrees=[number]'  # noqa

        if 'panel' in url_params.keys():
            try:
                message = self.panel_controller.move_panels(
                    url_params['panel'])
            except ValueError as e:
                message = str(e)
                return

        if 'degrees' in url_params.keys():
            angle = float(url_params['degrees'][0])
            message = self.panel_controller.go_to_angle(angle)
            # todo make sure to handle multiple params properly

        emergency = False
        angle = self.panel_controller.get_angle()
        auto_mode = self.panel_controller.is_auto_mode_on()
        min_angle = self.panel_controller.panel.min_angle
        max_angle = self.panel_controller.panel.max_angle

        response = HttpResponse(emergency, angle, auto_mode, message,
                                min_angle, max_angle)
        self.write_dataclass(response)
