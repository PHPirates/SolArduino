from datetime import datetime


class OptimalAnglesCalculator:

    def get(self, sun_samples=1000, nr_angles=10):
        """
        Get all optimal angles and times for yesterday, today and
        tomorrow (because it doesn't cost much time, so just to be sure).
        :return: A list of tuples (time: datetime, angle: float)
        """
        times_and_angles = []

        # todo eventually this should be killable since called from a
        #  daemon thread. Would be okay if calling haskell directly?
        with open('angles.times') as f:
            content = f.readlines()

            for line in content:
                # Strip whitespace and split angle and time
                angle_time = line.strip().split(' ')
                angle = float(angle_time[0])
                time = datetime.utcfromtimestamp(int(angle_time[1]))
                times_and_angles.append((time, angle))
