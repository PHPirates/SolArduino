import threading
import time
import types


class StoppableTimer(threading.Thread):
    """
    An asynchronous timer which can be cancelled.
    """

    def __init__(self, timeout: int, action: types.FunctionType):
        """
        :param timeout: The timer will execute the action after timeout seconds
        :param action: A function to execute after the timeout
        """
        super(StoppableTimer, self).__init__()
        self._stop_event = threading.Event()
        self.timeout = timeout
        self.action = action

    def stop(self):
        self._stop_event.set()

    def stopped(self):
        return self._stop_event.is_set()

    def run(self):
        start_time = time.time()
        while time.time() - start_time < self.timeout:
            if self.stopped():
                return

        self.action()
