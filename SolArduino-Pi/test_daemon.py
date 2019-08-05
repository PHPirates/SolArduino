from pep3143daemon import DaemonContext, PidFile
import syslog, time

counter = 0
# todo kill previous process before starting another one
pid = '/tmp/pep3143daemon_example.pid'

pidfile = PidFile(pid)
daemon = DaemonContext(pidfile=pidfile)
# we could have written this also as:
# daemon.pidfile = pidfile

print('pidfile is: {0}'.format(pid))
print('daemonizing...')

daemon.open()

# todo create shutdown method by overriding terminate?

with open("/tmp/current_time.txt", "w") as f:
    f.write('pep3143daemon_example: daemonized')

while True:
    counter += 1
    with open("/tmp/current_time.txt", "w") as f:
        f.write("The time is now " + time.ctime())
        f.write('pep3143daemon_example: counter: {0}'.format(counter))
    time.sleep(2)

# with open("/tmp/current_time.txt", "w") as f:
#     f.write('pep3143daemon_example: terminating...')
