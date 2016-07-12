//code adapted from //http://forum.arduino.cc/index.php?topic=90269.msg1991068#msg1991068


#include <EtherCard.h>

static byte mymac[] = { 0x74,0x69,0x69,0x2D,0x30,0x31 };

static byte myip[] = {192, 168, 2, 10};

byte Ethernet::buffer[500];

BufferFiller bfill;   //used in homepage

void setup () {
  Serial.begin(9600);

  if (ether.begin(sizeof Ethernet::buffer, mymac, 10) == 0)
    Serial.println(F("Failed to access Ethernet controller"));

    ether.staticSetup(myip);
    ether.printIp("Address: http://", ether.myip);
}

void loop () {
 word len = ether.packetReceive();
 word pos = ether.packetLoop(len);
 if (pos) { // check if valid tcp data is received
   delay(1); //just to be sure
   bfill = ether.tcpOffset();
   char *data = (char *) Ethernet::buffer + pos;
   if (strncmp("GET /", data, 5) != 0) {
          Serial.println("no valid GET request");
     }
     else {
         data += 5;
         if (data[0] == ' ') {
             // Return home page
             homePage();
         }
         else if (strncmp("?panel=up ", data, 10) == 0) {
             Serial.println("up");
         }
         else if (strncmp("?panel=down ", data, 12) == 0) {
             Serial.println("down");
         }
         else if (strncmp("?panel=stop ", data, 12) == 0) {
             Serial.println("stop");
         }
         else if (strncmp("?panel=auto ", data, 12) == 0) {
             Serial.println("auto");
         }
         else {
             Serial.println("page not found");
         }
     }
   ether.httpServerReply(bfill.position()); //send the reply, if there was one
 }
}

void homePage() {
 long t = millis() / 1000;
 word h = t / 3600;
 byte m = (t / 60) % 60;
 byte s = t % 60;
 bfill = ether.tcpOffset();
 bfill.emit_p(PSTR(
   "HTTP/1.0 200 OK\r\n"
   "Content-Type: text/html\r\n"
   "Pragma: no-cache\r\n"
   "\r\n"
  //  "<meta http-equiv='refresh' content='1'/>"
   "<title>RBBB server</title>"
   "<h1>$D$D:$D$D:$D$D</h1>"),
     h/10, h%10, m/10, m%10, s/10, s%10);
  }
