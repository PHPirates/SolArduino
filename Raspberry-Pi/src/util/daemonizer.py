from socketserver import TCPServer

import psutil
from pep3143daemon import PidFile, DaemonContext


class Daemonizer:
    """
    This class can daemonize a program, which means that it will run in the
    background as a daemon process.
    """

    # File where the pid is stored
    pid_path = '/tmp/solarduino.pid'

    def kill_if_exists(self):
        """ Kill current process if it is running. """
        try:
            with open(self.pid_path, 'r') as f:
                pid = int(f.read())

            process = psutil.Process(pid)
            process.terminate()
        except FileNotFoundError:
            pass

    def start(self, fileno):
        self.kill_if_exists()
        pidfile = PidFile(self.pid_path)
        daemon_context = DaemonContext(pidfile=pidfile)
        daemon_context.files_preserve = [fileno]
        return daemon_context
