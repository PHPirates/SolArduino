import dataclasses
import traceback

import cherrypy

from emergency import Emergency
from src.panel_control.panel_controller import PanelController
from webserver.http_response import HttpResponse


class Webserver(object):
    """
    Serve an http server.
    """

    panel_controller = PanelController()

    def emergency_to_dict(self, emergency: Emergency) -> dict:
        resp = HttpResponse(emergency=True,
                            angle=self.panel_controller.get_angle(),
                            auto_mode=False,
                            message=emergency.message,
                            max_angle=self.panel_controller.panel.max_angle,
                            min_angle=self.panel_controller.panel.min_angle)
        return dataclasses.asdict(resp)

    @cherrypy.expose
    @cherrypy.tools.json_out()
    def index(self, **params):

        emergency = self.panel_controller.emergency
        if emergency.is_set:
            if 'emergency' in params.keys():
                if params['emergency'] == 'reset':
                    self.panel_controller.emergency.reset()
            return self.emergency_to_dict(emergency)
        else:
            # noinspection PyBroadException
            try:
                return self.parse_params(params)
            except Exception:
                emergency.set(traceback.format_exc())
                return self.emergency_to_dict(emergency)

    def parse_params(self, url_params) -> dict:
        """
        Handle the request.
        :param url_params: Dict with keys and values.
        :return: Response.
        """
        message = ""

        # No parameters given.
        if not url_params.keys():
            message = 'No parameters were found in the request. Available parameters: panel=[up/down/auto/manual/stop], degrees=[number]'  # noqa

        if 'panel' in url_params.keys():
            try:
                message = self.panel_controller.move_panels(
                    url_params['panel'])
            except ValueError as e:
                # An incorrect request is not an emergency, we allow the user
                # to try again
                message = str(e)

        if 'degrees' in url_params.keys():
            if message:
                message = f'Can do only one thing at a time, skipping' \
                          f' degrees={url_params["degrees"]}'
            else:
                angle = float(url_params['degrees'])
                message = self.panel_controller.go_to_angle(angle)

        emergency = False
        angle = self.panel_controller.get_angle()
        auto_mode = self.panel_controller.is_auto_mode_on()
        min_angle = self.panel_controller.panel.min_angle
        max_angle = self.panel_controller.panel.max_angle

        response = HttpResponse(emergency, angle, auto_mode, message,
                                min_angle, max_angle)
        return dataclasses.asdict(response)
