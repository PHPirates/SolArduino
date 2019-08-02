from gpiozero import LED
from time import sleep

power_down = LED(26)
direction = LED(19)
power_up = LED(13)


def panels_up():
    power_down.off()
    power_up.on()
    direction.off()


def panels_down():
    power_down.on()
    power_up.off()
    direction.on()


def panels_stop():
    power_down.off()
    power_up.off()
    direction.off()


delay = 20
while True:
    panels_up()
    print("Up...")
    sleep(delay)

    panels_stop()
    print("Stop")
    sleep(delay)

    panels_down()
    print("Down...")
    sleep(delay)

    panels_stop()
    print("Stop")
    sleep(delay)
