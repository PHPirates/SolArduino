from src.emergency import Emergency
from src.panel_control.solar_panel import SolarPanel
from util.stoppable_timer import StoppableTimer


class PanelMover:
    """
    This class can move the solar panels up or down, and checks
    whether that is allowed or not.
    To avoid confusion, only one instance of this class should exist.
    """

    # Whether the panels are too high (above soft high end stop) or not
    # Remember this state to provide a more smooth transition
    cannot_go_up = False
    cannot_go_down = False

    # Stop moving automatically after timeout
    movement_timeout = 10  # seconds

    def __init__(self, panel: SolarPanel, emergency: Emergency):
        """
        :param panel: Solar panel.
        """
        self.panel = panel
        self.emergency = emergency
        self.timer = StoppableTimer(self.movement_timeout, self.panel.stop)

    def up(self) -> bool:
        """
        Move panels up, if allowed.
        :return: True if the panels will start to move up, false otherwise.
        """
        if self.emergency.is_set:
            self.panel.stop()
            return False
        else:
            if self.cannot_go_up:
                self.panel.stop()
                return False
            else:
                # If we move above the lower bound, reset state
                if not self.panel.is_below_lower_bound():
                    self.cannot_go_down = False
                # Check if we can move up
                if self.panel.is_above_upper_bound():
                    self.cannot_go_up = True
                    self.panel.stop()
                    return False
                else:
                    # Start timer
                    self.timer.stop()
                    self.timer = StoppableTimer(self.movement_timeout,
                                                self.panel.stop)
                    self.timer.start()
                    self.panel.move_up()
                    return True

    def down(self) -> bool:
        """
        Move panels down, if allowed.
        :return: True if the panels will start to move down, false otherwise.
        """
        if self.emergency.is_set:
            self.panel.stop()
            return False
        else:
            if self.cannot_go_down:
                self.panel.stop()
                return False
            else:
                # If we move below the upper bound, reset state
                if not self.panel.is_above_upper_bound():
                    self.cannot_go_up = False
                # Check if we can move down
                if self.panel.is_below_lower_bound():
                    self.cannot_go_down = True
                    self.panel.stop()
                    return False
                else:
                    # Start timer
                    self.timer.stop()
                    self.timer = StoppableTimer(self.movement_timeout,
                                                self.panel.stop)
                    self.timer.start()
                    self.panel.move_down()
                    return True
