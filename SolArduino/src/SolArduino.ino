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

double days;
double position[2];
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
}

void loop () {
  // Serial.print("time: ");
  // Serial.println(now());
  // Serial.println("azimuth, altitude");
  // getSunPosition(&position[0], locationLatitude, locationLongitude);
  double times[2];
  getTimes(&times[0],locationLatitude,locationLongitude);
  delay(60000);
}

void getSunPosition(double *position, double locationLatitude,
  double locationLongitude) { // previously getSunAltitude()

    double lw = rad * -locationLongitude; // what is lw?
    double phi = rad * locationLatitude;
    //int days = secondsToDays(); //days since epoch, gets current time in millis
    days = 6030.036844594906;

    double sun[2];
    sun[0] = days;
    sunCoords(&sun[0]);

    double h = siderealTime(days, lw) - sun[1]; // what is h?
    position[0] = azimuth(h, phi, sun[0]);
    position[1] = altitude(h, phi, sun[0]);

    for (size_t i = 0; i < 2; i++) {
      Serial.print(position[i]);
      Serial.println(" rad");
    }

    for (size_t i = 0; i < 2; i++) {
      position[i] = position[i] * 180 / PI;
      Serial.print(position[i]);
      Serial.println(" degree");
    }
}


int secondsToDays() {
  long currentTime = now(); //get current time in seconds
  return currentTime / daySecs - 0.5 + 2440588 - 2451545;
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

void sunCoords(double *sun) {
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

void getTimes(double *times, double locationLatitude,
  double locationLongitude) {
    double lw = rad * -locationLongitude; // what is lw?
    double phi = rad * locationLatitude;
    //int d = secondsToDays(); //days since epoch, gets current time in millis
    double d = 6030.036844594906;
    int n = julianCycle(d,lw);
    double ds = approxTransit(0,-0.0767,6030);
    double M = solarMeanAnomaly(ds);
    double L = eclipticLongitude(M);
    double dec = declination(L, 0);
    double Jnoon = solarTransitJ(ds, M, L);
    //result
    Serial.println(-0.833 * rad);
    Serial.println(lw);
    Serial.println(phi);
    Serial.println(dec);
    n = 6046;
    Serial.println(n);
    M = 110.24;
    Serial.println(M);
    Serial.println(L);
    Serial.println();
    long Jset = getSetJ(-0.833 * rad, lw, phi, dec, n, M, L);
    //Jset is 2457575.25 instead of .30 as in the .js
    unsigned long JsetJs = 2457596320; //is Jset * 1000, from js. 10 is about 15mins, 1 about 1.5 mins (accurate enough)
    long sunsetSeconds = (JsetJs + 500 - 2440588000) * 36 * 2.4; //*24 and then /10 doesn't work.

    // Serial.println(Jset*100);

    // long sunsetSeconds = (Jset + 0.5 - 2440588)  * 60 * 60 * 24;
    // long sunsetSeconds = ((Jset*100 + 0.5*100 - 2440588*100)  * 60 * 60 * 24)/100;
    // Serial.println(sunsetSeconds);

    // Jset2 = (2457591.33 + 0.5 - 2440588)  * 60 * 60 * 24;
    // Serial.println((2457591.33 + 0.5 - 2440588)  * 60 * 60 * 24);
}

//tested
double julianCycle(double d, double lw) {
  //first adding half and then casting to int is equal to rounding
  return (int)((d - 0.0009 - ( lw / (2 * PI) ) ) + 0.5);
}

//tested
double approxTransit(double Ht, double lw, double n) {
  return 0.0009 + (Ht + lw) / (2 * PI) + n;
}

//tested
double solarTransitJ(double ds, double M, double L) {
  return 2451545 + ds + 0.0053 * sin(M) - 0.0069 * sin(2 * L);
}

//tested
//returns time in seconds
long fromJulian(double j) {
  return (j + 0.5 - 2440588)  * 60 * 60 * 24;
}

long getSetJ(double h, double lw, double phi, double dec,
  double n, double M, double L) {
  double w = hourAngle(h, phi, dec); //2.00 compared to 1.99 from .js
  double a = approxTransit(w, lw, n);
  return solarTransitJ(a, M, L)*100; //should be a long to retain enough precision
}

double hourAngle(double h, double phi, double d) {
  return acos((sin(h) - sin (phi) * sin (d)) / (cos(phi) * cos(d)));
}
