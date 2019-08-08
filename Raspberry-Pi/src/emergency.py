import types


class Emergency:
    """ Represents an emergency. """

    def __init__(self, stop_function: types.FunctionType):
        """
        :param stop_function: What to do when an emergency is set.
        """
        self.stop_function = stop_function

    is_set = False
    message = None

    def set(self, message: str):
        self.is_set = True
        self.message = message
        self.stop_function()
