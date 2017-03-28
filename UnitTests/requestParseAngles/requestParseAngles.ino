//WORKS 4/2/2017

//libraries used
#include <EtherCard.h>      // https://github.com/jcw/ethercard

//variables about angles and times
const int TABLE_LENGTH = 10; // declare length here is easier
int angles[TABLE_LENGTH]; //stores angles*10
long dates[TABLE_LENGTH]; //stores unix times
const int TABLE_SIZE = 400; //this many bytes for 10 angles will do, used in ethernet buffer and when parsing
int tableIndex = 0; // The current index in the table when in auto mode

//ethernet variables, these are hard coded for a static setup
static byte mymac[] = { 0x74,0x69,0x69,0x2D,0x30,0x31 };
static byte myip[] = {192, 168, 8, 42};
const static uint8_t gw[] = {192,168,8,1}; //gateway ip
static uint8_t dns[] = {192,168,8,1};
const static uint8_t mask[] = {255,255,255,0}; //standard netmask

boolean responseReceived = true; // a flag for knowing whether the response from the NAS was received or not, because we need to wait on that
String EmergencyState = "";

//about http responses
byte Ethernet::buffer[TABLE_SIZE]; //minimum for requesting 10 angles, this seems pre-allocated

void setup() {
  Serial.begin(9600);
  
  setupEthernet();
  setupNAS();
  setupPanels();
}

void loop() {
//  ether.packetLoop(ether.packetReceive());

}

// debug function, returns how much free ram there is
int freeRam () {
  extern int __heap_start, *__brkval;
  int v;
  return (int) &v - (__brkval == 0 ? (int) &__heap_start : (int) __brkval);
}
