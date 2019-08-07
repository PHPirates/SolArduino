from src.solar_panel import SolarPanel


class PanelMover:
    """
    This class can move the solar panels up or down, and checks
    whether that is allowed or not.
    To avoid confusion, only one instance of this class should exist.
    """

    emergency: str = None

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
            self.panel.move_down()
            return True
