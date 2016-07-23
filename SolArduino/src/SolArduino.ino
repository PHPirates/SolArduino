// Arduino demo sketch for testing the DHCP client code
//
// Original author: Andrew Lindsay
// Major rewrite and API overhaul by jcw, 2011-06-07
//
// Copyright: GPL V2
// See http://www.gnu.org/licenses/gpl.html

#include <EtherCard.h>

//pin declarations
const byte POWER_HIGH = 2;
const byte DIRECTION_PIN = 3;
const byte POWER_LOW = 4;

const byte POTMETERPIN = A7;

//experimentally determined values
const int POTMETER_LOWEND = 641;
const int POTMETER_HIGHEND = 1022;
const byte DEGREES_HIGHEND = 50;
const byte DEGREES_LOWEND = 5;


static byte mymac[] = { 0x74,0x69,0x69,0x2D,0x30,0x31 };

static byte myip[] = {192, 168, 2, 14};

byte Ethernet::buffer[700];


void setup () {
  pinMode(POWER_HIGH,OUTPUT);
  pinMode(DIRECTION_PIN,OUTPUT);
  pinMode(POWER_LOW,OUTPUT);

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

  // setSolarPanel(27);
  solarPanelUp();
}

void loop () {
  int sensorValue = analogRead(A7);
  // print out the value you read:
  Serial.println(sensorValue);
  delay(100);        // delay in between reads for stability
}


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
