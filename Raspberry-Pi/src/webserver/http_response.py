from dataclasses import dataclass


@dataclass
class HttpResponse:
    """ Represents a webserver response. """
    emergency: bool
    angle: float
    auto_mode: bool
    message: str
    min_angle: float
    max_angle: float
