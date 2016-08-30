#include <Arduino.h>
#include <EtherCard.h>      // https://github.com/jcw/ethercard
#include <Time.h>           // http://www.arduino.cc/playground/Code/Time
#include <Timezone.h>       // https://github.com/JChristensen/Timezone

//pin declarations
const byte POWER_HIGH = 3;
const byte DIRECTION_PIN = 4;
const byte POWER_LOW = 5;

const byte POTMETERPIN = A7;

//experimentally determined values
const int POTMETER_LOWEND = 641;
const int POTMETER_HIGHEND = 1015;
const byte DEGREES_HIGHEND = 57;
const byte DEGREES_LOWEND = 5;


static byte mymac[] = { 0x74,0x69,0x69,0x2D,0x30,0x31 };

static byte myip[] = {192, 168, 2, 106};// ip Thomas
//static byte myip[] = {192, 168, 0, 23}; // ip Abby

// NTP globals
const char poolNTP[] PROGMEM = "0.pool.ntp.org"; //pool to get time server from
uint8_t ntpMyPort = 123; //port for the time server, TODO why is this needed?
// TimeZone : GMT+1. Helpful for getting correct current time
TimeChangeRule summerTime = {"UTC+1", Last, Sun, Mar, 2, +120};
TimeChangeRule winterTime = {"UTC+2", Last, Sun, Oct, 3, +60};
Timezone timeZone(summerTime, winterTime);

byte Ethernet::buffer[500];

BufferFiller bfill;   //used in every http response sent

//to be reused in every http response sent
const char http_OK[] PROGMEM =
   "HTTP/1.0 200 OK\r\n"
   "Content-Type: text/html\r\n"
   "Pragma: no-cache\r\n\r\n";

boolean autoMode;

void setup () {
  pinMode(POWER_HIGH,OUTPUT);
  pinMode(DIRECTION_PIN,OUTPUT);
  pinMode(POWER_LOW,OUTPUT);

  solarPanelStop();

  Serial.begin(9600);

  //do not forget to add the extra '10' argument because of this ethernet shield
  if (ether.begin(sizeof Ethernet::buffer, mymac, 10) == 0) {
    Serial.println(F("Failed to access Ethernet controller"));
  }
  ether.staticSetup(myip);
  //no serial print because ether.myip is a char[] array
  ether.printIp("Address: http://", ether.myip);

  //NTP syncing
  //Find ip address of a time server from the pool
  if (!ether.dnsLookup(poolNTP)) {
    Serial.println("DNS failed");
  }
  ether.printIp("Lookup IP   : ", ether.hisip);
  //sync arduino clock, current time in seconds can be found with now();
  setTime(getNtpTime());
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
             //solarPanelAuto(); //to be implemented
             acknowledge("Auto mode switched on.");
             autoMode = true;
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

             int degrees = stringDegrees.toInt(); //convert string to integer
             stringDegrees = String(degrees);
             stringDegrees += " &#176;";
             Serial.print("panels to degrees: ");
             Serial.println(degrees);
            //  setSolarPanel(degrees);

             //convert string to const char, easier than a modifiable char array
             acknowledge(stringDegrees.c_str());
             autoMode = false;
         }
         else if (strncmp("?update", data, 7) == 0) {
           //update requested, sent back current angle
           int angle = getCurrentAngle();
           String update = String(angle);
           if(autoMode) {
              update = update + " auto";
           } else {
              update = update + " manual";
           }

           acknowledge(update.c_str()); //convert to string, then to const char
         }
         else {
             Serial.println("Page not found");
         }
     }
   ether.httpServerReply(bfill.position()); //send the reply, if there was one
 }
}


void setSolarPanel(byte degrees) {
  //calculation is because of integer division at most 3 'voltage points' off, so only half a degree
  //times hundred to avoid integer division just possible without integer overflow
  int expectedVoltage = POTMETER_LOWEND +
  ( (long) ( (degrees - DEGREES_LOWEND) * 100 / (DEGREES_HIGHEND - DEGREES_LOWEND) )
  * (POTMETER_HIGHEND - POTMETER_LOWEND) ) / 100 ;
  Serial.print("expected: ");
  Serial.println(expectedVoltage);
  if (expectedVoltage > max (POTMETER_LOWEND,POTMETER_HIGHEND) || expectedVoltage < min (POTMETER_LOWEND,POTMETER_HIGHEND)) {
    sendErrorMessage("Degrees Out Of Range");
  } else {
    int potMeterValue = analogRead(POTMETERPIN);
    while (abs (potMeterValue - expectedVoltage) > 3) { //3 is about half a degree accuracy
      if (POTMETER_LOWEND > POTMETER_HIGHEND) {
        if (potMeterValue > expectedVoltage) {
          solarPanelUp;
        } else {
          solarPanelDown;
        }
      } else {
        if (potMeterValue < expectedVoltage) {
          solarPanelUp;
        } else {
          solarPanelDown;
        }
      }
      potMeterValue = analogRead(POTMETERPIN);

    }
    solarPanelStop(); //stop movement when close enough
  }
}

void sendErrorMessage(char* message) {
  //dispatch error message to all phones?
}

int getCurrentAngle() {
  int potMeterValue = analogRead(POTMETERPIN);
  Serial.println(potMeterValue);
  //fraction of potmetervalue from the low end. Times hundred
  //to maintain accuracy with integer division
  int fraction = ( (long)( abs(potMeterValue - POTMETER_LOWEND) ) * 100 )
  / abs( POTMETER_HIGHEND - POTMETER_LOWEND );
  return ( fraction * (DEGREES_HIGHEND - DEGREES_LOWEND) ) / 100 + DEGREES_LOWEND;
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
