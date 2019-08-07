from gpiozero import LED


class SolarPanel:
    """
    Low level panel control. Use PanelController instead for interaction with
    the panels.
    """

    power_down = LED(26)
    direction = LED(19)
    power_up = LED(13)

    # Potmeter soft end stops
    upper_bound = None
    lower_bound = None

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
        raise NotImplementedError
