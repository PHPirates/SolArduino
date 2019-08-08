import types
from datetime import date
from datetime import datetime
from typing import Callable

from src.emergency import Emergency
from src.get_optimal_angles import OptimalAnglesCalculator
from src.stoppable_thread import StoppableThread


class AutoMode(StoppableThread):
    """
    This class, when started as a new thread, enables the system to run in
    auto mode.
    """

    # Remember what day it is
    day_state = date.today()

    # List of tuples (time: datetime, angle: float)
    times_and_angles = []

    # Remember what angle we last used
    latest_time_index = 0

    def __init__(self, emergency: Emergency,
                 go_to_angle_function: Callable[[int], None]):
        """
        :param go_to_angle_function: What function to use to move the panels.
        """
        super(AutoMode, self).__init__()
        self.emergency = emergency
        self.go_to_angle_function = go_to_angle_function

    def run(self):
        while not self.stopped():

            not_initialised = len(self.times_and_angles) == 0
            out_of_angles = self.latest_time_index + 1 >= \
                            len(self.times_and_angles)
            day_changed = date.today() != self.day_state

            # Retrieve new angles when necessary
            if not_initialised or out_of_angles or day_changed:
                self.day_state = date.today()
                self.times_and_angles = OptimalAnglesCalculator().get()
                self.latest_time_index = 0

            # If we passed the last time, we did not receive correct times
            if self.times_and_angles[-1][0] < datetime.now():
                self.emergency.set('Could not retrieve optimal angles')
                return

            # Now we will assume the index is safe to work with
            while self.latest_time_index + 1 < len(self.times_and_angles) and \
                    self.times_and_angles[self.latest_time_index + 1][0] \
                    < datetime.now():
                self.latest_time_index += 1

            # If not at the end, move panels (otherwise request new angles
            # in next loop)
            if self.latest_time_index + 1 < len(self.times_and_angles):
                angle = self.times_and_angles[self.latest_time_index][1]
                self.go_to_angle_function(angle)


            # todo if time has progressed past a time, set panels
            # todo error handling
