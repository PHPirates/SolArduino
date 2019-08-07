void setSolarPanel(int degrees) {
  //upper and lower bounds
  if (degrees > max (DEGREES_HIGHEND,DEGREES_LOWEND)) {
    degrees = max (DEGREES_HIGHEND,DEGREES_LOWEND);
  } else if(degrees < min (DEGREES_HIGHEND,DEGREES_LOWEND)) {
    degrees = min (DEGREES_HIGHEND,DEGREES_LOWEND);
  }

  //times hundred to avoid integer division just possible without integer overflow
  float fraction = ( ( (float) ( (degrees - DEGREES_LOWEND) ) ) / (float) (DEGREES_HIGHEND - DEGREES_LOWEND) );
  int expectedVoltage = POTMETER_LOWEND +
  ( (long) ( fraction*100 * (POTMETER_HIGHEND - POTMETER_LOWEND) ) ) / 100 ;
  Serial.print(F("Going to degrees: "));
  Serial.println(degrees);
  Serial.print(F("Expected voltage: "));
  Serial.println(expectedVoltage);

  int potMeterValue = readPotMeter();
  while (potMeterValue != expectedVoltage && EmergencyState == "") {
    // The breaks below will ensure the Arduino won't get stuck here when it
    // can't reach the expectedVoltage: flags will be set when panels out of bounds.
    // If the potmeter happens to skip the value, the panels will go back towards the value.
    receiveHttpRequests(); //keep responsive
    if (POTMETER_LOWEND > POTMETER_HIGHEND) {
      if (potMeterValue > expectedVoltage) {
        //only move up when it is possible, otherwise just quit it
        if (!aboveUpperBound) {
          solarPanelUp();
        } else {
          break;
        }
      } else {
        if (!belowLowerBound) {
          solarPanelDown();
        } else {
          break;
        }
      }
    } else { // low end has lowest voltage points
      if (potMeterValue < expectedVoltage) {
        if (!aboveUpperBound) {
          solarPanelUp();
        } else {
          break;
        }
      } else {
        if (!belowLowerBound) {
          solarPanelDown();
        } else {
          break;
        }
      }
    }
    potMeterValue = readPotMeter();
  }
  solarPanelStop(); //stop movement when close enough or otherwise aborted
}

void solarPanelAuto() {
  autoMode = true;
  int angle = getNextAngle();
  if (angle == -1) {
    requestNewTable();
  } else {
    setSolarPanel(angle);
  }
}

// timeout is set to a value in the future on user up/down requests
void checkMovingTimeout() {
  if (millis() > moveTimeout and !panelsStopped) {
//    Serial.print(F("timed out at"));
//    Serial.println(millis());
    solarPanelStop();
  }
}

void resetMoveTimeout() {
  moveTimeout = millis() + MOVE_TIMEOUT_DELTA;
//  Serial.print(F("set moveTimeout at "));
//  Serial.print(millis());
//  Serial.print(F(" for "));
//  Serial.println(moveTimeout);
}

int getCurrentAngle() {
  int potMeterValue = readPotMeter();
  Serial.print(F("Potmeter: "));
  Serial.println(potMeterValue);
  //fraction of potmetervalue from the low end. Times hundred
  //to maintain accuracy with integer division
  int fraction = ( (long)( abs(potMeterValue - POTMETER_LOWEND) ) * 100 )
  / abs( POTMETER_HIGHEND - POTMETER_LOWEND );
  return ( (long) fraction * (DEGREES_HIGHEND - DEGREES_LOWEND) ) / 100 + DEGREES_LOWEND;
}

int readPotMeter() {
  long total = 0;
  for (int i=0; i<SAMPLE_RATE; i++) {
    total += analogRead(POTMETERPIN);
  }
  return total/SAMPLE_RATE;
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
  panelsStopped = true;
//  Serial.println(F("Panels stopped"));
  digitalWrite(POWER_LOW, LOW);
  digitalWrite(POWER_HIGH, LOW);
  digitalWrite(DIRECTION_PIN, LOW);
}
