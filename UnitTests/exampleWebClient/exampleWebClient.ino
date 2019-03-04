// Example showing a web request, using static setup. Based on the webClient built-in example.

#include <EtherCard.h>

// ethernet interface mac address, must be unique on the LAN
static byte mymac[] = { 0x46,0x7B,0x22,0x7B,0xB3,0x70 };

static byte myip[] = {192, 168, 8, 42};// ip Thomas
//static byte myip[] = {192, 168, 0, 23}; // ip Abby
//hard coded for a static setup:
const static uint8_t gw[] = {192,168,8,1}; 
//const static uint8_t dns[] = {195,121,1,34};  //works for ntp, doesn't work for web request
const static uint8_t dns[] = {192,168,8,1};  //address from dhcp setup,  not for ntp, works for web request
//const static uint8_t dns[] = {195,168,2,254}; // doesn't work for ntp, works for web request
const static uint8_t mask[] = {255,255,255,0}; 

byte Ethernet::buffer[480]; //minimum for requesting 10 angles
static uint32_t timer;

// called when the client request is complete
static void my_callback (byte status, word off, word len) {
  Serial.println(">>>");
  Ethernet::buffer[off+480] = 0; //480 chars needed for 10 angles
  const char* result = (const char*) Ethernet::buffer + off;
  Serial.print(result);
  Serial.println(freeRam());
//  parse(result);
  
}

void setup () {
  Serial.begin(9600);
  Serial.println(F("\n[webClient]"));

  //do not forget to add the extra '10' argument because of this ethernet shield
  if (ether.begin(sizeof Ethernet::buffer, mymac, 10) == 0) {
    Serial.println(F("Failed to access Ethernet controller"));
  }
  ether.staticSetup(myip,gw,dns,mask); //returns true anyway
  //no serial print because ether.myip is a char[] array
  ether.printIp("Address: http://", ether.myip);

//  if (!ether.dnsLookup(website))
//    Serial.println("DNS failed");
  //instead of dns lookup, set hisip manually to be used by browseURL
  ether.hisip[0]=192;
  ether.hisip[1]=168;
  ether.hisip[2]=8;
  ether.hisip[3]=200;

  ether.printIp("IP:  ", ether.myip);
  ether.printIp("GW:  ", ether.gwip);  
  ether.printIp("DNS: ", ether.dnsip); 
  ether.printIp("SRV: ", ether.hisip);

  Serial.print("<<< REQ ");
    ether.browseUrl(PSTR("/"), "", NULL, my_callback);
}

void loop () {
  ether.packetLoop(ether.packetReceive());
  
//  if (millis() > timer) {
//    timer = millis() + 5000;
//    Serial.println();
//    Serial.print("<<< REQ ");
//    ether.browseUrl(PSTR("/index.php"), "", NULL, my_callback);
//  }
}

int freeRam () {
  extern int __heap_start, *__brkval; 
  int v; 
  return (int) &v - (__brkval == 0 ? (int) &__heap_start : (int) __brkval); 
}
