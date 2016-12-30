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
  while (potMeterValue != expectedVoltage) {
    //if the potmeter happens to skip the value, the panels will go back towards the value
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
    potMeterValue = readPotMeter();
  }
  solarPanelStop(); //stop movement when close enough
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
  if (millis() > upTimeout and !panelsStopped) {
    solarPanelStop();
  }
  if (millis() > downTimeout and !panelsStopped) {
    solarPanelStop();
  }
}

void resetUpTimeout() {
  upTimeout = millis() + movingTimeout;
}

void resetDownTimeout() {
  downTimeout = millis() + movingTimeout;
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
  if (analogRead(POTMETERPIN) > POTMETER_LOWEND) {
    panelsStopped = false;
    Serial.println(F("panels moving down")); //TODO DEBUG
    digitalWrite(POWER_LOW, HIGH); //Put current via the low end stop to 28
    digitalWrite(POWER_HIGH, LOW); //Make sure the high end circuit is not on
    digitalWrite(DIRECTION_PIN, HIGH); //To go down, also let the current flow to E4
  }
}

void solarPanelUp() {
  if (analogRead(POTMETERPIN) < POTMETER_HIGHEND) {
    panelsStopped = false;
    Serial.println(F("panels moving up")); //TODO DEBUG
    digitalWrite(POWER_LOW, LOW);
    digitalWrite(POWER_HIGH, HIGH);
    digitalWrite(DIRECTION_PIN, LOW);
  }
}

void solarPanelStop() {
  panelsStopped = true;
  Serial.println(F("panels stopped")); //TODO DEBUG
  digitalWrite(POWER_LOW, LOW);
  digitalWrite(POWER_HIGH, LOW);
  digitalWrite(DIRECTION_PIN, LOW);
}
