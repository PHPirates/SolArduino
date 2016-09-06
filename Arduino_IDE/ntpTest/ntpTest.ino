#include <EtherCard.h>      // https://github.com/jcw/ethercard
#include <Time.h>           // http://www.arduino.cc/playground/Code/Time
#include <Timezone.h>       // https://github.com/JChristensen/Timezone

static byte mymac[] = { 0x74,0x69,0x69,0x2D,0x30,0x31 };

static byte myip[] = {192, 168, 2, 106};// ip Thomas
const static uint8_t gw[] = {192,168,2,254}; 
//const static uint8_t dns[] = {195,121,1,34};  //works for ntp
//const static uint8_t dns[] = {195,168,2,254}; //doesn't work for ntp
const static uint8_t dns[] = {195,121,2,254}; //doesn't work for ntp
const static uint8_t mask[] = {255,255,255,0}; 

const char poolNTP[] PROGMEM = "0.pool.ntp.org"; //pool to get time server from
uint8_t ntpMyPort = 123; //port for the time server, TODO why is this needed?

byte Ethernet::buffer[700];

// TimeZone : GMT+1. Helpful for getting correct current time
TimeChangeRule summerTime = {"UTC+1", Last, Sun, Mar, 2, +120};
TimeChangeRule winterTime = {"UTC+2", Last, Sun, Oct, 3, +60};
Timezone timeZone(summerTime, winterTime);

void setup () {
  Serial.begin(9600);

 //do not forget to add the extra '10' argument because of this ethernet shield
  if (ether.begin(sizeof Ethernet::buffer, mymac, 10) == 0) {
    Serial.println(F("Failed to access Ethernet controller"));
  }

  //static setup
  ether.staticSetup(myip,gw,dns,mask);
  //no serial print because ether.myip is a char[] array
  ether.printIp("Address: http://", ether.myip);
  
  //Find ip address of a time server from the pool
  if (!ether.dnsLookup(poolNTP)) {
    Serial.println("DNS failed");
  }
  ether.printIp("Lookup IP   : ", ether.hisip);

//sync arduino clock, current time in seconds can be found with now();
  setTime(getNtpTime());
  delay(1000);
  Serial.print("time: ");
  Serial.println(now());
}

void loop () {}

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
