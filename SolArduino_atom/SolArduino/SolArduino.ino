//libraries used
#include <EtherCard.h>      // https://github.com/jcw/ethercard
#include <TimeLib.h>           // http://www.arduino.cc/playground/Code/Time
#include <Timezone.h>       // https://github.com/JChristensen/Timezone

//variables about angles and times
const int TABLE_LENGTH = 10; // declare length here is easier
int angles[TABLE_LENGTH]; //stores angles*10
long dates[TABLE_LENGTH]; //stores unix times
const int TABLE_SIZE = 400; //this many bytes for 10 angles will do, used in ethernet buffer and when parsing
int tableIndex = 0; // The current index in the table when in auto mode

//pin declarations
const byte POWER_HIGH = 3;
const byte DIRECTION_PIN = 4;
const byte POWER_LOW = 5;
const byte POTMETERPIN = A7;
//rate of 28 (for int, max 31) instead of 5 hopefully solves panels stopping one degree off
const byte SAMPLE_RATE = 500; //amount of readings to take the average of when reading the potmeter

//experimentally determined values of potmeter and angle ends
const int POTMETER_LOWEND = 652;
const int POTMETER_HIGHEND = 1007;
const int DEGREES_HIGHEND = 570; //angle * 10 for more precision
const int DEGREES_LOWEND = 50;

//ethernet variables, these are hard coded for a static setup
static byte mymac[] = { 0x74,0x69,0x69,0x2D,0x30,0x31 };
static byte myip[] = {192, 168, 8, 42};
const static uint8_t gw[] = {192,168,8,1}; //gateway ip
//address that works for NTP, not needed to change later in the code anymore
//but that can always change
static uint8_t dns[] = {192,168,8,1};
//old dns:
// static uint8_t dns[] = {195,121,1,34};
// const static uint8_t dns[] = {195,168,2,254};  //address from latest dhcp setup but doesn't work for NTP
const static uint8_t mask[] = {255,255,255,0}; //standard netmask

// NTP globals
const char poolNTP[] PROGMEM = "europe.pool.ntp.org"; //pool to get time server from
uint8_t ntpMyPort = 123; //port for the time server, does not seem to matter
// TimeZone : GMT+1. Helpful for getting correct current time
TimeChangeRule summerTime = {"UTC+2", Last, Sun, Mar, 2, +60};
TimeChangeRule winterTime = {"UTC+1", Last, Sun, Oct, 3, +30};
Timezone timeZone(summerTime, winterTime);

//about http responses
byte Ethernet::buffer[TABLE_SIZE]; //minimum for requesting 10 angles, this seems pre-allocated
BufferFiller bfill;   //used in every http response sent

//to be reused in every http response sent
const char http_OK[] PROGMEM =
   "HTTP/1.0 200 OK\r\n"
   "Content-Type: text/html\r\n"
   "Pragma: no-cache\r\n\r\n";

//global states
boolean autoMode;
boolean responseReceived = true; // a flag for knowing whether the response from the NAS was received or not, because we need to wait on that
String EmergencyState = "";
// variables needed for storm mode
unsigned long stormTimes[2] = {0,0}; // start and end time, {0,0} when no storm mode planned
boolean wasAuto = false; // to remember if the panels were on auto mode before entering storm mode
int angleBeforeStorm = -1; // to remember angle before storm, remains -1 if panels were on auto
bool printed = false;

void setup () {
  //the serial shouldn't be used in final code, but this is always in development...
  Serial.begin(9600);

  setPinModes();
  setupEthernet();
  setupNTP();
  setupNAS();
  setupPanels();
}

void loop () {
  // Serial.print("stormTimes[0] = ");
  // Serial.println(stormTimes[0]);
  if(stormTimes[0] != 0){ // check if storm mode is planned
    enterStormMode();
  }

  //especially if the response is not received yet, keep receiving the response
  ether.packetLoop(ether.packetReceive());
  receiveHttpRequests(); //be responsive as a webserver
  if (responseReceived && EmergencyState == "") { // a check to make sure we don't request angles again before we received the ones we already had requested
    if (tableIndex+1 >= TABLE_LENGTH) { //if we are at the end
      requestNewTable();
      if (autoMode) {
        solarPanelAuto();
      }
    } else if (autoMode && dates[tableIndex+1]<now()) { //if time walked into next part
      Serial.println(F("Advancing to next angle"));
      tableIndex++;
      Serial.println(angles[tableIndex]);
      setSolarPanel(angles[tableIndex]);
    }
  }
}

//get next auto angle, if -1 then ran out of angles
int getNextAngle() {
  int i = 0;
  //advance i to i= index of next date in the future
  while(dates[i]<now() && i<TABLE_LENGTH){
    i++;
  }

  if (i >= TABLE_LENGTH) { //in the case we ran out of angles
    Serial.print(F("I ran out of angles, i= "));
    Serial.println(i);
    tableIndex = i;
    return -1;
  } else {
    tableIndex = i-1; //correct for i being one too much after searching to get corresponding angle
    Serial.print(F("Next degrees found: "));
    Serial.println(angles[tableIndex]);
    return angles[tableIndex];
  }
}

// debug function, returns how much free ram there is
int freeRam () {
  extern int __heap_start, *__brkval;
  int v;
  return (int) &v - (__brkval == 0 ? (int) &__heap_start : (int) __brkval);
}
