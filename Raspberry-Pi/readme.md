# Installation

We use supervisor to ensure the program is started after boot.
* Put the file `solarduino_meta.conf` in `/etc/supervisor/conf.d/`, then make sure you are not in `/opt/solarduino_venv/solarduino` and run `sudo supervisorctl reread && sudo supervisorctl update`.

# Development
* To start debugging, just in PyCharm debug the program (it will automatically kill the running instance). To start it again, reboot, or go to `/opt/solarduino_venv/solarduino` and execute `sudo supervisorctl shutdown` and `sudo supervisord -c supervisord.conf && sudo supervisorctl update`.
* To stop it manually, `supervisorctl shutdown`.


@reboot bash -c 'export PYTHONPATH=/opt/solarduino_venv/solarduino && cd /opt/solarduino_venv/solarduino && source /opt/solarduino_venv/bin/activate && python /opt/solarduino_venv/solarduino/src/solarduino.py'

# Troubleshooting
* `kex_exchange_identification: read: Connection reset by peer` when connecting via ssh: https://raspberrypi.stackexchange.com/a/102076/106568