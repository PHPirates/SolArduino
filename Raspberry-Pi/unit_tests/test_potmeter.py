import spidev  # To communicate with SPI devices
# from numpy import interp  # To scale values
from time import sleep  # To add delay

# Start SPI connection
# Requires installing the Debian package libatlas-base-dev
# and a sudo usermod -aG spi myusername
spi = spidev.SpiDev()  # Created an object
spi.open(0, 0)


def analog_input(channel):
    """ Read MCP3008 data """
    spi.max_speed_hz = 1350000
    adc = spi.xfer2([1, (8 + channel) << 4, 0])
    data = ((adc[1] & 3) << 8) + adc[2]
    return data


while True:
    output = analog_input(0)  # Reading from CH0
    # output = interp(output, [0, 1023], [0, 100])
    print(output)
    sleep(2)
