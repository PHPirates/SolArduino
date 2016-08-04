#include <EtherCard.h>      // https://github.com/jcw/ethercard
#include <Time.h>           // http://www.arduino.cc/playground/Code/Time
#include <Timezone.h>       // https://github.com/JChristensen/Timezone

static byte mymac[] = { 0x74,0x69,0x69,0x2D,0x30,0x31 };
static long daySecs = 60 * 60 * 24; //used for sun calculations
const char poolNTP[] PROGMEM = "0.pool.ntp.org"; //pool to get time server from
uint8_t ntpMyPort = 123; //port for the time server, TODO why is this needed?

byte Ethernet::buffer[700];

double rad = PI / 180;
double e = rad * 23.4397;

double locationLatitude = 51.546825;
double locationLongitude = 4.412033;

// TimeZone : GMT+1. Helpful for getting correct current time
TimeChangeRule summerTime = {"UTC+1", Last, Sun, Mar, 2, +120};
TimeChangeRule winterTime = {"UTC+2", Last, Sun, Oct, 3, +60};
Timezone timeZone(summerTime, winterTime);

void setup () {
  Serial.begin(9600);
  // Serial.println(F("\n[testDHCP]"));
  //
  // Serial.print("MAC: ");
  // for (byte i = 0; i < 6; ++i) {
  //   Serial.print(mymac[i], HEX);
  //   if (i < 5)
  //     Serial.print(':');
  // }
  // Serial.println();
  //
  // if (ether.begin(sizeof Ethernet::buffer, mymac, 10) == 0)
  //   Serial.println(F("Failed to access Ethernet controller"));
  //
  // Serial.println(F("Setting up DHCP"));
  // if (!ether.dhcpSetup())
  //   Serial.println(F("DHCP failed"));
  //
  // ether.printIp("My IP: ", ether.myip);
  // ether.printIp("Netmask: ", ether.netmask);
  // ether.printIp("GW IP: ", ether.gwip);
  // ether.printIp("DNS IP: ", ether.dnsip);
  //
  // //Find ip address of a time server from the pool
  // if (!ether.dnsLookup(poolNTP)) {
  //   Serial.println("DNS failed");
  // }
  // ether.printIp("Lookup IP   : ", ether.hisip);
  //
  // //sync arduino clock, current time in seconds can be found with now();
  // setTime(getNtpTime());
  Serial.println(powerLoss(6029.649,30)); //
  // Serial.println(sumPowerLoss(30)); //
}

void loop () {
  // Serial.print("time: ");
  // Serial.println(now());
  // Serial.println("azimuth, altitude");
  // double position[2];
  // getSunPosition(&position[0], locationLatitude, locationLongitude, 6030.04);

  // double times[2];
  // getTimes(&times[0],locationLatitude,locationLongitude);
  // delay(60000);


}

void getSunPosition(double *position, double locationLatitude,
  double locationLongitude, double days) { // previously getSunAltitude()

    double lw = rad * -locationLongitude; // what is lw?
    double phi = rad * locationLatitude;
    //int days = secondsToDays(); //days since epoch?, gets current time in millis
    // days = 6030.0368445949s06;

    double sun[2];
    sunCoords(&sun[0],days);

    double h = siderealTime(days, lw) - sun[1]; // what is h?
    position[0] = azimuth(h, phi, sun[0]);
    position[1] = altitude(h, phi, sun[0]);

    for (size_t i = 0; i < 2; i++) {
      // Serial.print(position[i]);
      // Serial.println(" rad");
    }

    for (size_t i = 0; i < 2; i++) {
      position[i] = position[i] * 180 / PI;
      // Serial.print(position[i]);
      // Serial.println(" degree");
    }
}


int secondsToDays() {
  long currentTime = now(); //get current time in seconds
  return currentTime / daySecs - 0.5 + 2440588 - 2451545; //-10957.5
}

unsigned long getNtpTime() {
  unsigned long timeFromNTP;
  const unsigned long seventy_years = 2208988800UL;
  ether.ntpRequest(ether.hisip, ntpMyPort);
  while(true) {
    word length = ether.packetReceive();
    ether.packetLoop(length);
    if(length > 0 && ether.ntpProcessAnswer(&timeFromNTP, ntpMyPort)) {
      // Serial.print("Time from NTP: ");
      // Serial.println(timeFromNTP);
      return timeZone.toLocal(timeFromNTP - seventy_years);
    }
  }
  return 0;
}

void sunCoords(double *sun, double days) {
  sun[0] = 1;
  sun[1] = 3;

  double mean = solarMeanAnomaly(days);
  double longitude = eclipticLongitude(mean);

  sun[0] = declination(longitude, 0);
  sun[1] = rightAscension(longitude, 0);
}

double solarMeanAnomaly(double days) {
  return rad * (357.5291 + 0.98560028 * days);
}

double eclipticLongitude(int mean) {
  double center = rad * (sin(mean) + 0.02 * sin(2 * mean) + 0.0003 * sin(3 * mean));
  double perihelion = rad * 102.9372;

  return mean + center + perihelion + PI;
}

double declination(double longitude, double b) {
  return asin(sin(b) * cos(e) + cos(b) * sin(e) * sin(longitude));
}

double rightAscension(double longitude, double b) {
  return atan2(sin(longitude) * cos(e) - tan(b) * sin(e), cos(longitude));
}

double siderealTime(int days, double lw) {
  return rad * (280.16 + 360.9856235 * days) - lw;
}

double azimuth(double h, double phi, double sun) {
  return PI + atan2(sin(h), cos(h) * sin(phi) - tan(sun) * cos(phi));
}

double altitude(double h, double phi, double sun) {
  return asin(sin(phi) * sin(sun) + cos(phi) * cos(sun) * cos(h));
}

int powerLoss(double daysEpoch, int angle) {
  //daysEpoch is a double so accurate to 15 mins
  double position[2]; //initialise array in which getSunPosition will put
  //azimuth and altitude of the sun
  getSunPosition(&position[0], locationLatitude, locationLongitude, daysEpoch);
  //convert everything to radians
  double azimuth = position[0] * rad;
  double altitude = position[1] * rad;
  double azimuthPanel = 0;
  double anglePanel = angle * rad;
  //now the magik formula (see docs)
  double angleSunPanel = acos(cos(azimuthPanel-azimuth)
  *cos(altitude)*sin(anglePanel)+cos(anglePanel)*sin(altitude)); //is in radians?
  Serial.println(angleSunPanel*1000);
  return 1- cos(angleSunPanel) * 100; //convert to power loss percentage
}

int sumPowerLoss(int angle) {
  long sunriseSet[3];
  // getTimes(); //returns an array with today's sunrise and sunset in days*1000
  sunriseSet[0] = 6029649; //TODO debug magik
  sunriseSet[1] = 6030335;
  int sum = 0;
  //about one hour intervals
  for (int i = sunriseSet[0]; i < sunriseSet[1]; i+=40) {
      sum += powerLoss(i, angle);
  }

}
