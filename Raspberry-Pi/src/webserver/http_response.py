from dataclasses import dataclass


@dataclass
class HttpResponse:
    """ Represents a webserver response. """
    emergency: bool
    angle: int
    mode: str
    message: str
