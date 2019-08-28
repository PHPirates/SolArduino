from typing import Callable


class Emergency:
    """ Represents an emergency. """

    def __init__(self, stop_function: Callable[[bool, bool], None]):
        """
        :param stop_function: What to do when an emergency is set.
        First bool should be stop_angle_thread, second stop_auto_thread as in
         panel_controller.stop().
        """
        self.stop_function = stop_function

    is_set = False
    message = None

    def set(self, message: str,
            stop_angle_thread: bool = True,
            stop_auto_thread: bool = True):
        self.is_set = True
        self.message = message
        self.stop_function(stop_angle_thread, stop_auto_thread)

    def reset(self):
        self.is_set = False
        self.message = None
