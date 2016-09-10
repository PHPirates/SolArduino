#include <Arduino.h>
#include <EtherCard.h>      // https://github.com/jcw/ethercard
#include <Time.h>           // http://www.arduino.cc/playground/Code/Time
#include <Timezone.h>       // https://github.com/JChristensen/Timezone
//#include <avr/pgmspace.h>   // To use PROGMEM
#include "numbers.h"        // The file which contains the data with angles and unix times
int tableLength = 10; // It's easiest to declare the length here. Changing it on the webserver may break the app because of initialisation of arrays with length 10, although it shouldn't. Change the tableSize to corresponding amount of bytes anyway, and initialise both arrays with the correct length
const int tableSize = 680; //480 bytes for 10 angles will do, used in ethernet buffer and when parsing
int tableIndex; // The current index in the table when in auto mode

//pin declarations
const byte POWER_HIGH = 3;
const byte DIRECTION_PIN = 4;
const byte POWER_LOW = 5;

const byte POTMETERPIN = A7;
byte sampleRate = 5; //amount of readings to take the average of when reading the potmeter

//experimentally determined values
const int POTMETER_LOWEND = 650;
const int POTMETER_HIGHEND = 1007;
const int DEGREES_HIGHEND = 570; //angle * 10 for more precision
const int DEGREES_LOWEND = 50;


static byte mymac[] = { 0x74,0x69,0x69,0x2D,0x30,0x31 };

static byte myip[] = {192, 168, 2, 106};// ip Thomas
//static byte myip[] = {192, 168, 0, 23}; // ip Abby
//hard coded for a static setup:
const static uint8_t gw[] = {192,168,2,254};
static uint8_t dns[] = {195,121,1,34}; //address that works for NTP, we change it later in the code
// const static uint8_t dns[] = {195,168,2,254};  //address from latest dhcp setup but doesn't work for NTP
const static uint8_t mask[] = {255,255,255,0};

// NTP globals
const char poolNTP[] PROGMEM = "europe.pool.ntp.org"; //pool to get time server from
uint8_t ntpMyPort = 123; //port for the time server, TODO why is this needed?
// TimeZone : GMT+1. Helpful for getting correct current time
TimeChangeRule summerTime = {"UTC+1", Last, Sun, Mar, 2, +0};
TimeChangeRule winterTime = {"UTC+2", Last, Sun, Oct, 3, -60};
Timezone timeZone(summerTime, winterTime);

byte Ethernet::buffer[tableSize]; //minimum for requesting 10 angles, this seems pre-allocated
BufferFiller bfill;   //used in every http response sent

//to be reused in every http response sent
const char http_OK[] PROGMEM =
   "HTTP/1.0 200 OK\r\n"
   "Content-Type: text/html\r\n"
   "Pragma: no-cache\r\n\r\n";

boolean autoMode;

boolean responseReceived = true;; // a check for knowing whether the response from the NAS was received or not, because we need to wait on that

void setup () {
  Serial.begin(9600);
  pinMode(POWER_HIGH,OUTPUT);
  pinMode(DIRECTION_PIN,OUTPUT);
  pinMode(POWER_LOW,OUTPUT);

  //do not forget to add the extra '10' argument because of this ethernet shield
  if (ether.begin(sizeof Ethernet::buffer, mymac, 10) == 0) {
    Serial.println(F("Failed to access Ethernet controller"));
  }
  ether.staticSetup(myip,gw,dns,mask); //returns true anyway
  //no serial print because ether.myip is a char[] array
  ether.printIp("Address: http://", ether.myip);

  //NTP syncing
//    Find ip address of a time server from the pool
   if (!ether.dnsLookup(poolNTP)) {
     Serial.println("DNS failed");
   }
   ether.printIp("Lookup IP   : ", ether.hisip);
   //sync arduino clock, current time in seconds can be found with now();
   setTime(getNtpTime());
   Serial.print("time: ");
   Serial.println(now());

//  if (!ether.dnsLookup(website))
//    Serial.println("DNS failed");
  //instead of dns lookup, set hisip manually to be used by browseURL
  ether.hisip[0]=192;
  ether.hisip[1]=168;
  ether.hisip[2]=2;
  ether.hisip[3]=7;
  // the http request needs a different dns, so we set that here and do setup again
  dns[0] = 192;
  dns[1] = 168;
  dns[2] = 2;
  dns[3] = 254;
  ether.staticSetup(myip,gw,dns,mask);
//
//    ether.printIp("IP:  ", ether.myip);
//  ether.printIp("GW:  ", ether.gwip);
//  ether.printIp("DNS: ", ether.dnsip);
//  ether.printIp("SRV: ", ether.hisip);

   solarPanelStop();
   autoMode = true;
   Serial.println(F("calling Auto() from setup")); //too much serial prints results in bad request?
   solarPanelAuto(); //panels start up in auto mode, this makes sure tableIndex is initialised to a correct value
}

void loop () {
  ether.packetLoop(ether.packetReceive()); //something to do with http request?
  receiveHttpRequests();
  if (responseReceived) { // a check to make sure we don't request angles again before we received the ones we already had requested
    if (tableIndex+1 >= tableLength) {
      requestNewTable();
    } else if (autoMode && dates[tableIndex+1]<now()) { //if time walked into next part
      Serial.println(F("Advancing to next angle"));
      tableIndex++;
      Serial.println(angles[tableIndex]);
      setSolarPanel(angles[tableIndex]);

    }
  }
}

void setSolarPanel(int degrees) {
  //upper and lower bounds
  if (degrees > max (DEGREES_HIGHEND,DEGREES_LOWEND)) {
    degrees = max (DEGREES_HIGHEND,DEGREES_LOWEND);
  } else if(degrees < min (DEGREES_HIGHEND,DEGREES_LOWEND)) {
    degrees = min (DEGREES_HIGHEND,DEGREES_LOWEND);
  }

  //?: calculation is because of integer division at most 3 'voltage points' off, so only half a degree
  //times hundred to avoid integer division just possible without integer overflow
  float fraction = ( ( (float) ( (degrees - DEGREES_LOWEND) ) ) / (float) (DEGREES_HIGHEND - DEGREES_LOWEND) );
  int expectedVoltage = POTMETER_LOWEND +
  ( (long) ( fraction*100 * (POTMETER_HIGHEND - POTMETER_LOWEND) ) ) / 100 ;
  Serial.print(F("Going to degrees: "));
  Serial.println(degrees);
  Serial.print(F("expected: "));
  Serial.println(expectedVoltage);

    int total = 0;
    for (int i=0; i<sampleRate; i++) {
      total += analogRead(POTMETERPIN);
    }
    int potMeterValue = total/sampleRate;
//    Serial.print("potmeter: ");
//    Serial.println(potMeterValue);
//    Serial.println(abs (potMeterValue - expectedVoltage));
    while (abs (potMeterValue - expectedVoltage) > 3) { //3 is about half a degree accuracy
      receiveHttpRequests(); //keep responsive
      if (POTMETER_LOWEND > POTMETER_HIGHEND) {
        if (potMeterValue > expectedVoltage) {
          solarPanelUp();
        } else {
          solarPanelDown();
        }
      } else {
        if (potMeterValue < expectedVoltage) {
          solarPanelUp();
        } else {
          solarPanelDown();
        }
      }
      int total = 0;
      for (int i=0; i<sampleRate; i++) {
        total += analogRead(POTMETERPIN);
      }
      potMeterValue = total/sampleRate;
//      Serial.print("potmeter: ");
//      Serial.println(potMeterValue);
//      Serial.println(abs (potMeterValue - expectedVoltage));

    }
    solarPanelStop(); //stop movement when close enough

}

void solarPanelAuto() {
  Serial.println(F("solarPanelAuto() called"));
  int i = 0;
  while(dates[i]<now() && i<tableLength){
//    Serial.println(i);
    i++;
  }

  if (i >= tableLength) { //in the case we ran out of angles
    Serial.print("I ran out of angles, i= ");
    Serial.println(i);
    tableIndex = i;
    requestNewTable();
  } else {
      tableIndex = i-1; //correct for i being one too much after searching to get corresponding angle
      Serial.print("set on auto, degrees passed: ");
  Serial.println(angles[tableIndex]);
  setSolarPanel(angles[tableIndex]);
  }
}

void requestNewTable() {
  Serial.println(F("requesting new data"));
  dates[0] = 1471903200;
  dates[1] = 1471903210;
  responseReceived = false; // set boolean to 'wait for request'
  Serial.print("<<< REQ ");
    ether.browseUrl(PSTR("/index.php"), "", NULL, my_callback);
  Serial.print(F("free ram after browseurl: "));
  Serial.println(freeRam());
  tableIndex = 0; //reset global index
}

// called when the client request is complete
static void my_callback (byte status, word off, word len) {
  if (!responseReceived) { // for some reason responses keep coming although the url is only requested once
    Serial.println(">>>");
    Serial.print(F("free ram: "));
    Serial.println(freeRam());
    Ethernet::buffer[off+tableSize] = 0; //480 chars needed for 10 angles
    char* result = (char*) Ethernet::buffer + off;
    delay(42); // Make sure the request is sent and received properly, no delay results in a 400
    Serial.print(result);
    Serial.println(freeRam());
    parseString(result); // fill the arrays with the data
    Serial.print(F("received: "));
    Serial.println(responseReceived);
    responseReceived = true;
    Serial.println(F("calling Auto() from my_callback"));
    solarPanelAuto(); // set the solar panels to the right angle
  }
}

void receiveHttpRequests() {
  int degrees = -1;

  //receive the http request
 word len = ether.packetReceive();
 word pos = ether.packetLoop(len);
 if (pos) { // check if valid tcp data is received
   delay(1); //just to be sure
   bfill = ether.tcpOffset();
   char *data = (char *) Ethernet::buffer + pos;
   if (strncmp("GET /", data, 5) != 0) {
          Serial.println(F("no valid GET request"));
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
           solarPanelUp();
             acknowledge("Panels going up."); //send acknowledge http response
             autoMode = false;
         }
         else if (strncmp("?panel=down ", data, 12) == 0) {
           solarPanelDown();
             acknowledge("Panels going down.");
             autoMode = false;
         }
         else if (strncmp("?panel=stop ", data, 12) == 0) {
           solarPanelStop();
             acknowledge("Panels stopped/not moving.");
             autoMode = false;
         }
         else if (strncmp("?panel=auto ", data, 12) == 0) {
//              Serial.println(F("calling Auto() by app request"));
             Serial.println(F("Auto mode switched on."));
             autoMode = true; //solarPanelAuto() is called later, first we handle off the request
             acknowledge("Auto mode switched on.");
         }
         else if (strncmp("?panel=manual ", data, 12) == 0){
             acknowledge("Auto mode switched off.");
             autoMode = false;
         }
         else if (strncmp("?degrees=", data, 9) == 0) {
              //print digit that comes after
              String stringDegrees;
              stringDegrees += (char)data[9]; //convert to char and add to string
              stringDegrees += (char)data[10];

             degrees = stringDegrees.toInt(); //convert string to integer
//             stringDegrees = String(degrees);
             stringDegrees += " &#176;";
             Serial.print(F("panels to degrees: "));
             Serial.println(degrees);


             //convert string to const char, easier than a modifiable char array
             acknowledge(stringDegrees.c_str());
             autoMode = false;
         }
         else if (strncmp("?update", data, 7) == 0) {
           //update requested, sent back current angle
           Serial.println(F("Update requested."));
           int angle = (getCurrentAngle()+5)/10; //round to int
           String update = String(angle);
           if(autoMode) {
              update = update + " auto";
           } else {
              update = update + " manual";
           }

           acknowledge(update.c_str()); //convert to string, then to const char
         }
         else {
             Serial.println(F("Page not found"));
         }
     }
   ether.httpServerReply(bfill.position()); //send the reply, if there was one
   delay(42); // no delay here causes a bad request for requesting angles from the NAS
   if(autoMode) { //then it is switched on, or was on for example with update requested
      solarPanelAuto();
    }

   if(degrees != -1) { // if degrees were changed by a manual request, set solar panels
     setSolarPanel(degrees*10); //degrees go in times ten for more precision
   }
 }
}

// returns how much free ram there is
int freeRam () {
  extern int __heap_start, *__brkval;
  int v;
  return (int) &v - (__brkval == 0 ? (int) &__heap_start : (int) __brkval);
}

void parseString(char *from) {

//  char from [tableSize]; // 480 bytes should be enough for 10 angles, see top of code
//  strcpy(from, everything); //because we can't modify a constant char we need to copy it
  char *found;
  int leng;
  char *times;
  char dateString[120]; //these may need some tuning
  char angleString[60];

  Serial.print(F("free ram after assignments: "));
  Serial.println(freeRam());

  found = strtok(from, "_");
  int i = 0;
  while(found != NULL){

    if(i==1){
      tableLength = atoi(found);
//      Serial.println(leng);
    } else if(i==2) {
      strcpy(dateString,found);
      Serial.println(found);
//      Serial.println(dateString);
    } else if(i==3) {
      Serial.println(found);
      strcpy(angleString,found);
    }
    found = strtok(NULL, "_"); //extract next token
    i++;
  }

  // now the same splitting up again but for the substrings with dates and angles
  found = strtok(dateString,",");
  i = 0;
  while (found != NULL) {
    //convert char* 'found' to long
    dates[i] = atol(found);
    found = strtok(NULL, ","); //extract next token
//    Serial.println(dates[i]);
    i++;
  }

  //Now again for the angles
    found = strtok(angleString,",");
  i = 0;
  while (found != NULL) {
    //convert char* 'found' to int
    angles[i] = atoi(found);
    found = strtok(NULL, ","); //extract next token
    i++;
  }

  Serial.println(F("strings parsed: "));
  Serial.println(F("dates: "));
  for(int i = 0; i<tableLength; i++) {
    Serial.println(dates[i]);
  }
    Serial.println(F("angles: "));
  for(int i = 0; i<tableLength; i++) {
    Serial.println(angles[i]);
  }

}

int getCurrentAngle() {
  int potMeterValue = analogRead(POTMETERPIN);
  Serial.println(potMeterValue);
  //fraction of potmetervalue from the low end. Times hundred
  //to maintain accuracy with integer division
  int fraction = ( (long)( abs(potMeterValue - POTMETER_LOWEND) ) * 100 )
  / abs( POTMETER_HIGHEND - POTMETER_LOWEND );
  return ( (long) fraction * (DEGREES_HIGHEND - DEGREES_LOWEND) ) / 100 + DEGREES_LOWEND;
}

//solar panel movements
void solarPanelDown() {
  digitalWrite(POWER_LOW, HIGH); //Put current via the low end stop to 28
  digitalWrite(POWER_HIGH, LOW); //Make sure the high end circuit is not on
  digitalWrite(DIRECTION_PIN, HIGH); //To go down, also let the current flow to E4
}

void solarPanelUp() {
  digitalWrite(POWER_LOW, LOW);
  digitalWrite(POWER_HIGH, HIGH);
  digitalWrite(DIRECTION_PIN, LOW);
}

void solarPanelStop() {
  digitalWrite(POWER_LOW, LOW);
  digitalWrite(POWER_HIGH, LOW);
  digitalWrite(DIRECTION_PIN, LOW);
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
    "$S"), //$S for a c string
  http_OK,message); //parameters to be replaced go here
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
