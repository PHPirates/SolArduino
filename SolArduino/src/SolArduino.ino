//code adapted from //http://forum.arduino.cc/index.php?topic=90269.msg1991068#msg1991068


#include <EtherCard.h>

//choose a unique mac address
static byte mymac[] = { 0x74,0x69,0x69,0x2D,0x30,0x31 };

static byte myip[] = {192, 168, 2, 10};

byte Ethernet::buffer[500];

BufferFiller bfill;   //used in every http response sent

//to be reused in every http response sent
const char http_OK[] PROGMEM =
   "HTTP/1.0 200 OK\r\n"
   "Content-Type: text/html\r\n"
   "Pragma: no-cache\r\n\r\n";

void setup () {
  Serial.begin(9600);

  //do not forget to add the extra '10' argument because of this ethernet shield
  if (ether.begin(sizeof Ethernet::buffer, mymac, 10) == 0) {
    Serial.println(F("Failed to access Ethernet controller"));
  }
  ether.staticSetup(myip);
  //no serial print because ether.myip is a char[] array
  ether.printIp("Address: http://", ether.myip);
}

void loop () {
  //receive the http request
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
         //start parsing data
        //  Serial.println(data);
         if (data[0] == ' ') {
             // No parameters given (http://192.168.2.10), return home page
             homePage();
         }
         else if (strncmp("?panel=up ", data, 10) == 0) {
             acknowledge("Panels going up"); //send acknowledge http response
         }
         else if (strncmp("?panel=down ", data, 12) == 0) {
             acknowledge("Panels going down");
         }
         else if (strncmp("?panel=stop ", data, 12) == 0) {
             acknowledge("Panels stopped");
         }
         else if (strncmp("?panel=auto ", data, 12) == 0) {
             acknowledge("Panels going on auto");
         }
         else if (strncmp("?degrees=", data, 9) == 0) {
              //print digit that comes after
              String string;
              string += (char)data[9]; //convert to char and add to string
              string += (char)data[10];
             int degrees = string.toInt(); //convert string to integer
             Serial.print("panels to degrees: ");
             Serial.println(degrees);
            //  setSolarPanel(degrees);

             //add degrees in a string
             String message1 = "Panel going to ";
             String message2 = " degrees";
             String message = message1 + degrees + message2;
             char charMessage[message.length() +1];
             //convert string to const char, easier than a modifiable char array
             acknowledge(message.c_str());
         }
         else {
             Serial.println("Page not found");
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
   "$F"
  //  "<meta http-equiv='refresh' content='1'/>"
   "<title>SolArduino</title>"
   "<h1>$D$D:$D$D:$D$D</h1>"),
   http_OK,
     h/10, h%10, m/10, m%10, s/10, s%10);
  }

  void acknowledge(const char* message) {
    //send a http response
    bfill = ether.tcpOffset();
    bfill.emit_p(PSTR(
      "$F" //$F is for a progmem string,
      "<title>SolArduino</title>"
      "<h1>$S</h1>"), //$S for a c string
    http_OK,message); //parameters to be replaced go here
  }
