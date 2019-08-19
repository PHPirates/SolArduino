# Installation

We use supervisor to ensure the program is started after boot.
* Put the file `solarduino_meta.conf` in `/etc/supervisor/conf.d/`, then `sudo supervisorctl reread && sudo supervisorctl update`.

# Development
* To start debugging, just in PyCharm debug the program (it will automatically kill the running instance). To start it again, reboot or try to run it from pycharm. Perhaps `sudo supervisorctl start solarduino_meta` works.
* To stop it manually, `supervisorctl shutdown`
