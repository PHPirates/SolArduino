class PanelController:
    """ Control the solar panels. """

    def move_panels(self, direction: list) -> str:
        """
        Start moving the solar panels.

        :param direction: One of 'up', 'down', 'auto' or 'stop'.
        :return: Appropriate human readable response message.
        """

        if len(direction) > 0:
            raise ValueError(f'Panels can only move in one direction but was '
                             f'given panel={direction}.')

        direction = direction[0]

        if direction == 'up':
            return 'Panels going up.'
        elif direction == 'down':
            return 'Panels going down.'
        elif direction == 'auto':
            return 'Panels switching to auto mode.'
        elif direction == 'stop':
            return 'Panels stopping.'
        else:
            raise ValueError(f'Expected one of panel=up, panel=down, '
                             f'panel=auto or panel=stop but received '
                             f'panel={direction} instead.')
