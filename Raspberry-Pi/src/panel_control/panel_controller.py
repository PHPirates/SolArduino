from src.panel_control.go_to_angle import GoToAngleThread
from src.panel_control.panel_mover import PanelMover
from src.panel_control.solar_panel import SolarPanel


class PanelController:
    """
    Control the solar panels. To avoid confusion, only one
    instance of this class should exist.
    """

    panel = SolarPanel()
    panel_mover = PanelMover(panel)
    go_to_angle_thread: GoToAngleThread = None

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
            self.up()
            return 'Panels going up.'
        elif direction == 'down':
            self.down()
            return 'Panels going down.'
        elif direction == 'auto':
            self.enable_auto_mode()
            return 'Panels switching to auto mode.'
        elif direction == 'stop':
            self.stop()
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
        # todo some sampling...
        try:
            return self.panel.get_potmeter_value()
        except ValueError as e:
            self.panel_mover.set_emergency(str(e))
            raise e

    def up(self) -> bool:
        return self.panel_mover.up()

    def down(self) -> bool:
        return self.panel_mover.down()

    def stop(self):
        self.panel_mover.timer.stop()
        # Make sure to wait for the thread to stop, to ensure it doesn't
        # sneakily move something between us asking to stop and it stopping
        self.go_to_angle_thread.stop()
        self.go_to_angle_thread.join()
        self.panel.stop()

    def enable_auto_mode(self):
        raise NotImplementedError

    def disable_auto_mode(self):
        raise NotImplementedError

    def go_to_angle(self, angle: int):
        """ Start a new thread which will move the panels. """
        if self.go_to_angle_thread is not None:
            self.go_to_angle_thread.stop()
        self.go_to_angle_thread = GoToAngleThread(angle, self)
        self.go_to_angle_thread.start()
