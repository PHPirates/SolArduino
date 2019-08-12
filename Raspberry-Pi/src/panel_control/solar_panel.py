from gpiozero import LED
import spidev  # To communicate with SPI devices


class SolarPanel:
    """
    Low level panel control. Use PanelController instead for interaction with
    the panels.
    """

    def __init__(self):
        # Start SPI connection
        self.spi = spidev.SpiDev()
        self.spi.open(0, 0)
        self.spi.max_speed_hz = 1350000

    power_down = LED(26)
    direction = LED(19)
    power_up = LED(13)

    # Keep some safe distance from the real end stops
    soft_bound_angle = 1
    hard_max_angle = 57
    hard_min_angle = 5
    max_angle = hard_max_angle - soft_bound_angle
    min_angle = hard_min_angle + soft_bound_angle

    # Potmeter end stops, upper bound should be higher than lower bound,
    # so that's why we take 1023 - x
    # The hard bounds are the ones on which the panels will hit the
    # real end stop
    hard_upper_bound = 1023 - 57
    hard_lower_bound = 1023 - 415
    # The soft bounds are the bounds at which the software will stop
    # to avoid hitting the hard bounds
    soft_bound_value = (hard_upper_bound - hard_lower_bound) / \
                       (hard_max_angle - hard_min_angle)
    upper_bound = hard_upper_bound - soft_bound_value
    lower_bound = hard_lower_bound + soft_bound_value

    def move_up(self):
        self.power_down.off()
        self.power_up.on()
        self.direction.off()

    def move_down(self):
        self.power_down.on()
        self.power_up.off()
        self.direction.on()

    def stop(self):
        self.power_down.off()
        self.power_up.off()
        self.direction.off()

    def get_potmeter_value(self):
        if self.upper_bound < self.lower_bound:
            raise ValueError('Upper bound potmeter value has to be'
                             ' higher than the lower bound value.')
        channel = 0
        adc = self.spi.xfer2([1, (8 + channel) << 4, 0])
        data = ((adc[1] & 3) << 8) + adc[2]
        # We prefer having a high value when the panels are high,
        # so since we connected it the wrong way around we switch it here
        reversed_data = 1023 - data
        return reversed_data

    def is_above_upper_bound(self) -> bool:
        return self.get_potmeter_value() > self.upper_bound

    def is_below_lower_bound(self) -> bool:
        return self.get_potmeter_value() < self.lower_bound
