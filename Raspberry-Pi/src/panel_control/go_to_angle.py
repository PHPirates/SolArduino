from src.panel_control.panel_controller import PanelController
from src.stoppable_thread import StoppableThread


class GoToAngleThread(StoppableThread):
    """
    Try to set the panels at a certain angle.
    """

    # How close to try to get to the target
    accuracy = 0.1  # degrees

    def __init__(self, angle: int, panel_controller: PanelController):
        super(GoToAngleThread, self).__init__()
        self.target_angle = angle
        self.panel_controller = panel_controller

    def run(self):
        if not self.panel_controller.emergency.is_set:

            current_angle = self.panel_controller.get_angle()
            while abs(self.target_angle - current_angle) > self.accuracy \
                    and not self.panel_controller.emergency.is_set:
                # If someone else stopped this thread
                if self.stopped():
                    self.panel_controller.stop()
                    return

                if current_angle < self.target_angle:
                    # Attempt to move up, stop if not possible
                    # This will also make sure to stop when bounds are reached
                    if not self.panel_controller.up():
                        self.panel_controller.stop()
                        return
                else:
                    if not self.panel_controller.down():
                        self.panel_controller.stop()
                        return

                current_angle = self.panel_controller.get_angle()

        self.panel_controller.stop()
