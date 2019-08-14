from dataclasses import dataclass


@dataclass
class HttpResponse:
    """ Represents a webserver response. """
    emergency: bool
    angle: float
    mode: str
    message: str
