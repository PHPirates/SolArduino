import time

import psutil as psutil
from pep3143daemon import DaemonContext, PidFile

counter = 0
pid_path = '/tmp/pep3143daemon_example.pid'

# Kill old process before starting new one
try:
    with open(pid_path, 'r') as f:
        pid = int(f.read())

    process = psutil.Process(pid)
    process.terminate()
except FileNotFoundError:
    pass

pidfile = PidFile(pid_path)
daemon = DaemonContext(pidfile=pidfile)

print('pidfile is: {0}'.format(pid_path))
print('daemonizing...')

daemon.open()

with open("/tmp/current_time.txt", "w") as f:
    f.write('pep3143daemon_example: daemonized')

while True:
    counter += 1
    with open("/tmp/current_time.txt", "w") as f:
        f.write("The time is now " + time.ctime() + '\n')
        f.write('pep3143daemon_example: counter: {0}'.format(counter))
    time.sleep(2)

# with open("/tmp/current_time.txt", "w") as f:
#     f.write('pep3143daemon_example: terminating...')
