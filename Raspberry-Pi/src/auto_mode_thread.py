from datetime import date
from datetime import datetime
from typing import Callable

from get_optimal_angles import get_optimal_angles
from src.emergency import Emergency
from util.stoppable_thread import StoppableThread


class AutoModeThread(StoppableThread):
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
                 go_to_angle_function: Callable[[float], str]):
        """
        :param go_to_angle_function: What function to use to move the panels.
        """
        super(AutoModeThread, self).__init__()
        self.daemon = True
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
                self.times_and_angles = get_optimal_angles()
                self.latest_time_index = 0

            # If we passed the last time, we did not receive correct times
            if self.times_and_angles[-1][0] < datetime.utcnow():
                self.emergency.set('Could not retrieve optimal angles')
                return

            # Now we will assume the index is safe to work with
            new_index = self.latest_time_index
            while new_index + 1 < len(self.times_and_angles) and \
                    self.times_and_angles[new_index + 1][0] \
                    < datetime.utcnow():
                new_index += 1

            # If we need to advance to a next time and angle
            if new_index != self.latest_time_index:
                self.latest_time_index = new_index
                # If not at the end, move panels (otherwise request new angles
                # in next loop)
                if self.latest_time_index + 1 < len(self.times_and_angles):
                    angle = self.times_and_angles[self.latest_time_index][1]
                    self.go_to_angle_function(angle)
