import time

from src.stoppable_timer import StoppableTimer


def action():
    print(f'Timeout reached at : {time.time() - start_time}')


start_time = time.time()
timer = StoppableTimer(1, action)
timer.start()
# Stop should become one sec before next request
time.sleep(2)
print(f'next request at: {time.time() - start_time}')
timer.stop()

start_time = time.time()
# Timeout should not be reached
timer = StoppableTimer(1, action)
timer.start()
print(f'Started at: {time.time() - start_time}')
timer.stop()
time.sleep(2)
