from src.panel_control.solar_panel import SolarPanel
from src.stoppable_timer import StoppableTimer


class PanelMover:
    """
    This class can move the solar panels up or down, and checks
    whether that is allowed or not.
    To avoid confusion, only one instance of this class should exist.
    """

    emergency: str = None
    # Whether the panels are too high (above soft high end stop) or not
    # Remember this state to provide a more smooth transition
    cannot_go_up = False
    cannot_go_down = False

    timeout = 4  # seconds
    timer: StoppableTimer = StoppableTimer(timeout, lambda: True)

    def __init__(self, panel: SolarPanel):
        """
        :param panel: Solar panel.
        """
        self.panel = panel

    def set_emergency(self, message: str):
        self.emergency = message
        self.panel.stop()

    def up(self) -> bool:
        """
        Move panels up, if allowed.
        :return: True if the panels will start to move up, false otherwise.
        """
        if self.emergency is not None:
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
                    self.timer = StoppableTimer(self.timeout,
                                                self.panel.stop)
                    self.timer.start()
                    self.panel.move_up()
                    return True

    def down(self) -> bool:
        """
        Move panels down, if allowed.
        :return: True if the panels will start to move down, false otherwise.
        """
        if self.emergency is not None:
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
                    self.timer = StoppableTimer(self.timeout,
                                                self.panel.stop)
                    self.timer.start()
                    self.panel.move_down()
                    return True
