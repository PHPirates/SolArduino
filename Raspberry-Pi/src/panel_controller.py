from src.panel_mover import PanelMover
from src.solar_panel import SolarPanel


class PanelController:
    """
    Control the solar panels. To avoid confusion, only one
    instance of this class should exist.
    """

    panel = SolarPanel()
    panel_mover = PanelMover(panel)

    def __init__(self):
        """ For safety, stop panels and disable auto mode. """
        self.stop()
        self.disable_auto_mode()

    def move_panels(self, direction: list) -> str:
        """
        Start moving the solar panels.

        :param direction: One of 'up', 'down', 'auto' or 'stop'.
        :return: Appropriate human readable response message.
        """

        if len(direction) > 1:
            raise ValueError(f'Panels can only move in one direction but was '
                             f'given panel={direction}.')

        direction = direction[0]

        if direction == 'up':
            return 'Panels going up.'
        elif direction == 'down':
            return 'Panels going down.'
        elif direction == 'auto':
            return 'Panels switching to auto mode.'
        elif direction == 'stop':
            return 'Panels stopping.'
        else:
            raise ValueError(f'Expected one of panel=up, panel=down, '
                             f'panel=auto or panel=stop but received '
                             f'panel={direction} instead.')

    def emergency_message(self) -> str:
        """
        :return: Emergency message if there is one, None otherwise.
        """
        return self.panel_mover.emergency

    def get_angle(self):
        # some sampling...
        try:
            return self.panel.get_potmeter_value()
        except ValueError as e:
            self.panel_mover.set_emergency(str(e))
            raise e

    def up(self):
        self.panel_mover.up()

    def down(self):
        self.panel_mover.down()

    def stop(self):
        self.panel.stop()

    def disable_auto_mode(self):
        raise NotImplementedError
