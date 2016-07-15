// Arduino demo sketch for testing the DHCP client code
//
// Original author: Andrew Lindsay
// Major rewrite and API overhaul by jcw, 2011-06-07
//
// Copyright: GPL V2
// See http://www.gnu.org/licenses/gpl.html

#include <EtherCard.h>

//pin declarations
const byte POTMETERPIN = 14;
const byte HIGH_END_PIN = 2;
const byte LOW_END_PIN = 3;

//experimentally determined values
const int POTMETER_LOWEND = 360;
const int POTMETER_HIGHEND = 74;
const byte DEGREES_HIGHEND = 50;
const byte DEGREES_LOWEND = 5;

static byte mymac[] = { 0x74,0x69,0x69,0x2D,0x30,0x31 };

static byte myip[] = {192, 168, 2, 14};

byte Ethernet::buffer[700];


void setup () {
  Serial.begin(9600);
  // Serial.println(F("\n[testDHCP]"));
  //
  // Serial.print("MAC: ");
  // for (byte i = 0; i < 6; ++i) {
  //   Serial.print(mymac[i], HEX);
  //   if (i < 5)
  //     Serial.print(':');
  // }
  // Serial.println();
  //
  // if (ether.begin(sizeof Ethernet::buffer, mymac, 10) == 0)
  //   Serial.println(F("Failed to access Ethernet controller"));
  //
  //   // ether.staticSetup(myip);
  //   //
  //   // ether.printIp("My IP: ", ether.myip);
  //   // ether.printIp("GW IP: ", ether.gwip);
  //
  // Serial.println(F("Setting up DHCP"));
  // if (!ether.dhcpSetup())
  //   Serial.println(F("DHCP failed"));
  //
  // ether.printIp("My IP: ", ether.myip);
  // ether.printIp("Netmask: ", ether.netmask);
  // ether.printIp("GW IP: ", ether.gwip);
  // ether.printIp("DNS IP: ", ether.dnsip);

  setSolarPanel(27);
}

void loop () {}


void setSolarPanel(byte degrees) {
  //calculation is because of integer division at most 3 'voltage points' off, so only half a degree
  //times hundred to avoid integer division just possible without integer overflow
  int expectedVoltage = POTMETER_LOWEND +
  ( (degrees - DEGREES_LOWEND) * 100 / (DEGREES_HIGHEND - DEGREES_LOWEND) )
  * (POTMETER_HIGHEND - POTMETER_LOWEND) / 100 ;
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

//assumes setting either high or low end pin high will move the panel
//up or down
void solarPanelDown() {
  digitalWrite(HIGH_END_PIN, LOW); //disconnect high end stop circuit
  digitalWrite(LOW_END_PIN, HIGH); //connect with circuit though low end stop
}

void solarPanelUp() {
  digitalWrite(HIGH_END_PIN, HIGH); //connect high end stop circuit
  digitalWrite(LOW_END_PIN, LOW); //disconnect with circuit though low end stop
}

void solarPanelStop() {
  digitalWrite(HIGH_END_PIN, LOW); //disconnect high end stop circuit
  digitalWrite(LOW_END_PIN, LOW); //disconnect with circuit though low end stop
}

void sendErrorMessage(char* message) {
  //dispatch error message to all phones?
}

int getCurrentAngle() {
  int potMeterValue = analogRead(POTMETERPIN);
  //fraction of potmetervalue from the low end. Times hundred
  //to maintain accuracy with integer division
  int fraction = ( ( abs(potMeterValue - POTMETER_LOWEND) ) * 100 )
  / abs( POTMETER_HIGHEND - POTMETER_LOWEND );
  return ( fraction * (DEGREES_HIGHEND - DEGREES_LOWEND) ) / 100 + DEGREES_LOWEND;
}
