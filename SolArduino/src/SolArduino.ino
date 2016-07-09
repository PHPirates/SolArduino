#include <EtherCard.h>      // https://github.com/jcw/ethercard
#include <Time.h>           // http://www.arduino.cc/playground/Code/Time
#include <Timezone.h>       // https://github.com/JChristensen/Timezone

static byte mymac[] = { 0x74,0x69,0x69,0x2D,0x30,0x31 };
static long daySecs = 60 * 60 * 24; //used for sun calculations
const char poolNTP[] PROGMEM = "0.pool.ntp.org"; //pool to get time server from
uint8_t ntpMyPort = 123; //port for the time server, TODO why is this needed?

byte Ethernet::buffer[700];

// TimeZone : GMT+1. Helpful for getting correct current time
TimeChangeRule summerTime = {"UTC+1", Last, Sun, Mar, 2, +120};
TimeChangeRule winterTime = {"UTC+2", Last, Sun, Oct, 3, +60};
Timezone timeZone(summerTime, winterTime);

void setup () {
  Serial.begin(9600);
  Serial.println(F("\n[testDHCP]"));

  Serial.print("MAC: ");
  for (byte i = 0; i < 6; ++i) {
    Serial.print(mymac[i], HEX);
    if (i < 5)
      Serial.print(':');
  }
  Serial.println();

  if (ether.begin(sizeof Ethernet::buffer, mymac, 10) == 0)
    Serial.println(F("Failed to access Ethernet controller"));

  Serial.println(F("Setting up DHCP"));
  if (!ether.dhcpSetup())
    Serial.println(F("DHCP failed"));

  ether.printIp("My IP: ", ether.myip);
  ether.printIp("Netmask: ", ether.netmask);
  ether.printIp("GW IP: ", ether.gwip);
  ether.printIp("DNS IP: ", ether.dnsip);

  //Find ip address of a time server from the pool
  if (!ether.dnsLookup(poolNTP)) {
    Serial.println("DNS failed");
  }
  ether.printIp("Lookup IP   : ", ether.hisip);

//sync arduino clock, current time in seconds can be found with now();
  setTime(getNtpTime());
}

void loop () {
  Serial.println(now());
  delay(1000);
}

void getSunAltitude() {
  int days = millisToDays(); //days since epoch, gets current time in millis
  Serial.println(days);
}

int millisToDays() {
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
