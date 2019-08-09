from statistics import mean

from src.auto_mode_thread import AutoModeThread
from src.emergency import Emergency
from src.panel_control.go_to_angle import GoToAngleThread
from src.panel_control.panel_mover import PanelMover
from src.panel_control.solar_panel import SolarPanel


class PanelController:
    """
    Control the solar panels. To avoid confusion, only one
    instance of this class should exist.
    """

    panel = SolarPanel()
    go_to_angle_thread: GoToAngleThread = None
    auto_mode_thread: AutoModeThread = None

    def __init__(self):
        """ For safety, stop panels and disable auto mode. """
        self.emergency = Emergency(self.stop)
        self.panel_mover = PanelMover(self.panel, self.emergency)
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

    def get_angle(self):
        """
        Get the current angle of the solar panels, where 0 is flat and 90
        upright.
        """
        nr_samples = 500
        try:
            sample_mean = mean([self.panel.get_potmeter_value()
                                for _ in range(nr_samples)])
            fraction = (sample_mean - self.panel.lower_bound) / \
                       (self.panel.upper_bound - self.panel.lower_bound)
            return fraction * (self.panel.max_angle - self.panel.min_angle) \
                + self.panel.min_angle
        except ValueError as e:
            self.emergency.set(str(e))
            raise e

    def up(self) -> bool:
        return self.panel_mover.up()

    def down(self) -> bool:
        return self.panel_mover.down()

    def stop(self):
        """
        Will cancel any movement and disable auto mode.
        """
        self.panel.stop()
        self.panel_mover.timer.stop()
        # Make sure to wait for the thread to stop, to ensure it doesn't
        # sneakily move something between us asking to stop and it stopping
        if self.go_to_angle_thread is not None:
            self.go_to_angle_thread.stop()
            self.go_to_angle_thread.join()
        if self.auto_mode_thread is not None:
            self.auto_mode_thread.stop()
            self.auto_mode_thread.join()

        self.panel.stop()

    def enable_auto_mode(self):
        """
        Will not do anything if already started.
        """
        if self.auto_mode_thread is None:
            self.auto_mode_thread = AutoModeThread(self.emergency,
                                                   self.go_to_angle)
            self.auto_mode_thread.start()

    def disable_auto_mode(self):
        if self.auto_mode_thread is not None:
            self.auto_mode_thread.stop()
            self.auto_mode_thread.join()

    def go_to_angle(self, angle: int):
        """ Start a new thread which will move the panels. """
        if self.go_to_angle_thread is not None:
            self.go_to_angle_thread.stop()
            self.go_to_angle_thread.join()
        self.go_to_angle_thread = GoToAngleThread(angle, self)
        self.go_to_angle_thread.start()
