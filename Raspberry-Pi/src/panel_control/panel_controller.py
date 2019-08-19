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
    auto_mode_enabled = False

    def __init__(self):
        """ For safety, stop panels and disable auto mode. """
        self.emergency = Emergency(self.stop)
        self.panel_mover = PanelMover(self.panel, self.emergency)
        self.stop()
        self.disable_auto_mode()

    def move_panels(self, direction: list) -> str:
        """
        Start moving the solar panels.

        :param direction: One of 'up', 'down', 'auto', 'manual' or 'stop'.
        :return: Appropriate human readable response message.
        """

        if direction == 'up':
            self.up()
            return 'Panels going up.'
        elif direction == 'down':
            self.down()
            return 'Panels going down.'
        elif direction == 'auto':
            self.enable_auto_mode()
            return 'Panels switching to auto mode.'
        elif direction == 'manual':
            self.disable_auto_mode()
            return 'Auto mode switched off.'
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
        nr_samples = 2
        try:
            sample_mean = mean([self.panel.get_potmeter_value()
                                for _ in range(nr_samples)])
            # By calculating this way, we linearly project the expected angle
            # even when the potmeter value is out of known bounds
            degrees_per_value = (self.panel.max_angle - self.panel.min_angle) / (self.panel.upper_bound - self.panel.lower_bound)  # noqa
            angle = (sample_mean - self.panel.lower_bound) * degrees_per_value + self.panel.min_angle  # noqa

            return angle
        except ValueError as e:
            self.emergency.set(str(e))
            raise e

    def up(self) -> bool:
        self.disable_auto_mode()
        return self.panel_mover.up()

    def down(self) -> bool:
        self.disable_auto_mode()
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

        self.disable_auto_mode()
        self.panel.stop()

    def enable_auto_mode(self):
        """
        Will not do anything if already started.
        """
        if not self.is_auto_mode_on():
            self.auto_mode_thread = AutoModeThread(self.emergency,
                                                   self.go_to_angle)
            self.auto_mode_thread.start()
            self.auto_mode_enabled = True

    def disable_auto_mode(self):
        if self.auto_mode_enabled:
            self.auto_mode_thread.stop()
            self.auto_mode_thread.join()
            self.auto_mode_enabled = False

    def is_auto_mode_on(self):
        """
        :return: True if auto mode is enabled and thread is running,
         false otherwise.
        """
        if not self.auto_mode_enabled:
            return False

        if self.auto_mode_thread is None:
            return False
        else:
            return self.auto_mode_thread.is_alive()

    def go_to_angle(self, angle: float) -> str:
        """
        Start a new thread which will move the panels.
        :return: A human readable message.
        """
        self.disable_auto_mode()

        if self.go_to_angle_thread is not None:
            self.go_to_angle_thread.stop()
            self.go_to_angle_thread.join()
        self.go_to_angle_thread = GoToAngleThread(angle, self)
        self.go_to_angle_thread.start()
        return f'Going to {angle} degrees'
