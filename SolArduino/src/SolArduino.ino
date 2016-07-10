// Arduino demo sketch for testing the DHCP client code
//
// Original author: Andrew Lindsay
// Major rewrite and API overhaul by jcw, 2011-06-07
//
// Copyright: GPL V2
// See http://www.gnu.org/licenses/gpl.html

#include <EtherCard.h>

static byte mymac[] = { 0x74,0x69,0x69,0x2D,0x30,0x31 };

static byte myip[] = {192, 168, 2, 10};

byte Ethernet::buffer[500];

BufferFiller bfill;   //used in homepage

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

    ether.staticSetup(myip);
    //
    // ether.printIp("My IP: ", ether.myip);
    // ether.printIp("GW IP: ", ether.gwip);

  // Serial.println(F("Setting up DHCP"));
  // if (!ether.dhcpSetup())
  //   Serial.println(F("DHCP failed"));

  ether.printIp("My IP: ", ether.myip);
  // ether.printIp("Netmask: ", ether.netmask);
  // ether.printIp("GW IP: ", ether.gwip);
  // ether.printIp("DNS IP: ", ether.dnsip);
}

void loop () {
 word len = ether.packetReceive();
 word pos = ether.packetLoop(len);
 if (pos)  // check if valid tcp data is received
   ether.httpServerReply(homePage()); // send web page data
}

static word homePage() {
 long t = millis() / 1000;
 word h = t / 3600;
 byte m = (t / 60) % 60;
 byte s = t % 60;
 bfill = ether.tcpOffset();                         //???????
 bfill.emit_p(PSTR(                                 //???????
   "HTTP/1.0 200 OK\r\n"
   "Content-Type: text/html\r\n"
   "Pragma: no-cache\r\n"
   "\r\n"
   "<meta http-equiv='refresh' content='1'/>"
   "<title>RBBB server</title>"
   "<h1>$D$D:$D$D:$D$D</h1>"),
     h/10, h%10, m/10, m%10, s/10, s%10);                //???????
 return bfill.position();                                           //???????
}
