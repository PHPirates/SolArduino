from util.stoppable_thread import StoppableThread


class GoToAngleThread(StoppableThread):
    """
    Try to set the panels at a certain angle.
    """

    # How close to try to get to the target
    accuracy = 1  # degrees

    def stop_panels(self):
        """
        Stop panels. We don't want to stop the current thread,
        and by the time this thread is started, the auto thread is
        either running and we don't want to stop it, or it is already stopped.
        """
        self.panel_controller.stop(stop_angle_thread=False,
                                   stop_auto_thread=False)

    def __init__(self, angle: float, panel_controller):
        super(GoToAngleThread, self).__init__()
        self.daemon = True
        self.target_angle = angle
        self.panel_controller = panel_controller

    def run(self):
        if not self.panel_controller.emergency.is_set:

            current_angle = self.panel_controller.get_angle()
            while abs(self.target_angle - current_angle) > self.accuracy \
                    and not self.panel_controller.emergency.is_set:
                # If someone else stopped this thread
                if self.stopped():
                    return

                if current_angle < self.target_angle:
                    # Attempt to move up, stop if not possible
                    # This will also make sure to stop when bounds are reached
                    if not self.panel_controller.up():
                        self.stop_panels()
                        return
                else:
                    if not self.panel_controller.down():
                        self.stop_panels()
                        return

                current_angle = self.panel_controller.get_angle()

        self.stop_panels()
