
//pin declarations
const byte POWER_HIGH = 3;
const byte DIRECTION_PIN = 4;
const byte POWER_LOW = 5;
const byte POTMETERPIN = A7;

//experimentally determined values of potmeter and angle ends
const int SOFT_BOUND = 7; // about 7 /( (405-40)/(570-50) ) = 10, so 1 degree safety
const int POTMETER_LOWEND = 405;
const int POTMETER_HIGHEND = 40; 
const int DEGREES_HIGHEND = 570 - 10; //angle * 10 for more precision, including soft bound
const int DEGREES_LOWEND = 50 + 10;

String EmergencyState = "";
boolean aboveUpperBound; // two cases which are recoverable errors and hence not a normal emergency
boolean belowLowerBound;
bool panelsStopped = true; //needed to control timer logic

void setup() {
  // put your setup code here, to run once:
  pinMode(POWER_HIGH,OUTPUT);
  pinMode(DIRECTION_PIN,OUTPUT);
  pinMode(POWER_LOW,OUTPUT);
  Serial.begin(9600);
}

void loop() {
  // put your main code here, to run repeatedly:
  solarPanelUp();
  delay(5000);
  solarPanelStop();
  delay(5000);
  solarPanelDown();
  delay(5000);
  solarPanelStop();
  delay(5000);
}

//solar panel movements
void solarPanelDown() {
  // extra, redundant, safety.
  //Intentionally does not rely on readPotMeter(), which reduces accuracy
  //near the soft end stops but increases safety. Accuracy inbetween should not
  // be influenced
  // move only down when no emergency and panels not below lower bound
  if ((EmergencyState == "") && !belowLowerBound) {
    

    // Condition on which potmeter reading (lowest or highest) corresponds to which panel bound (lower, upper).
    if (POTMETER_LOWEND < POTMETER_HIGHEND) {

      // Reset when panels are below upper bound again
      if (analogRead(POTMETERPIN) < POTMETER_HIGHEND) { 
        aboveUpperBound = false;
      }
      // If panels not below lower bound, move down
      if (analogRead(POTMETERPIN) > POTMETER_LOWEND) { 
        panelsStopped = false;
        digitalWrite(POWER_LOW, HIGH); //Put current via the low end stop to 28
        digitalWrite(POWER_HIGH, LOW); //Make sure the high end circuit is not on
        digitalWrite(DIRECTION_PIN, HIGH); //To go down, also let the current flow to E4
      } else { // panels below lower bound
        belowLowerBound = true;
        solarPanelStop();
      }
      
    } else { // Same, but inequalities reversed.

      // If panels are below upper bound again
      if (analogRead(POTMETERPIN) > POTMETER_HIGHEND) { 
        aboveUpperBound = false;
      }
      // If panels not below lower bound, move down
      if (analogRead(POTMETERPIN) < POTMETER_LOWEND) { 
        panelsStopped = false;
        digitalWrite(POWER_LOW, HIGH); 
        digitalWrite(POWER_HIGH, LOW); 
        digitalWrite(DIRECTION_PIN, HIGH); 
      } else { // panels below lower bound
        belowLowerBound = true;
        solarPanelStop();
      }
      
    }
    
  } else {
    solarPanelStop();
  }
}

void solarPanelUp() {
  // go up when we can
  if ((EmergencyState == "") && !aboveUpperBound) {

    // Condition on which potmeter reading (lowest or highest) corresponds to which panel bound (lower, upper).
    if (POTMETER_LOWEND < POTMETER_HIGHEND) {
      
      if (analogRead(POTMETERPIN) > POTMETER_LOWEND) { // if we move above low bound, reset flag
        belowLowerBound = false;
      }
      if (analogRead(POTMETERPIN) < POTMETER_HIGHEND) {
        panelsStopped = false;
  //      Serial.println(F("Panels going up"));
        digitalWrite(POWER_LOW, LOW);
        digitalWrite(POWER_HIGH, HIGH);
        digitalWrite(DIRECTION_PIN, LOW);
      } else { // we are above upper bound, do not go up
        aboveUpperBound = true;
        solarPanelStop();
      }

    } else { // Same, but inequalities reversed.

      if (analogRead(POTMETERPIN) < POTMETER_LOWEND) { // if we move above low bound, reset flag
        belowLowerBound = false;
      }
      if (analogRead(POTMETERPIN) > POTMETER_HIGHEND) {
        panelsStopped = false;
  //      Serial.println(F("Panels going up"));
        digitalWrite(POWER_LOW, LOW);
        digitalWrite(POWER_HIGH, HIGH);
        digitalWrite(DIRECTION_PIN, LOW);
      } else { // we are above upper bound, do not go up
        aboveUpperBound = true;
        solarPanelStop();
      }
      
    }
  } else {
    solarPanelStop();
  }
}

void solarPanelStop() {
  digitalWrite(POWER_LOW, LOW);
  digitalWrite(POWER_HIGH, LOW);
  digitalWrite(DIRECTION_PIN, LOW);
}
