import time
import types

from util.stoppable_thread import StoppableThread


class StoppableTimer(StoppableThread):
    """
    An asynchronous timer which can be cancelled.
    """

    def __init__(self, timeout: int, action: types.FunctionType):
        """
        :param timeout: The timer will execute the action after timeout seconds
        :param action: A function to execute after the timeout
        """
        super(StoppableTimer, self).__init__()
        self.daemon = True
        self.timeout = timeout
        self.action = action

    def run(self):
        start_time = time.time()
        while time.time() - start_time < self.timeout:
            if self.stopped():
                return

        self.action()
