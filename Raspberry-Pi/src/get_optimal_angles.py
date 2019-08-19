from datetime import datetime


def get_optimal_angles(sun_samples=1000, nr_angles=10):
    """
    Get all optimal angles and times for yesterday, today and
    tomorrow (because it doesn't cost much time, so just to be sure).

    NOTE We use utc time to avoid timezone problems, so be sure to compare with
     datetime.utcnow() instead of datetime.now().

    :return: A list of tuples (time: datetime, angle: float)
    """
    times_and_angles = []

    # todo #78 eventually this should be killable since called from a
    #  daemon thread. Would be okay if calling haskell directly?
    with open('angles.times') as f:
        content = f.readlines()

        for line in content:
            # Strip whitespace and split angle and time
            angle_time = line.strip().split(' ')
            angle = float(angle_time[0])
            time = datetime.utcfromtimestamp(float(angle_time[1]))
            times_and_angles.append((time, angle))

    return times_and_angles
