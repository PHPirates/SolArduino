const byte POTMETERPIN = A7;

const int SAMPLE_RATE = 500; //amount of readings to take the average of when reading the potmeter

void setup() {
  Serial.begin(9600);
  pinMode(POTMETERPIN,INPUT);

}

void loop() {
  
  Serial.println(readPotMeter());
  delay(1000);
}

int readPotMeter() {
  long total = 0;
  for (int i=0; i<SAMPLE_RATE; i++) {
    total += analogRead(POTMETERPIN);
  }
  return total/SAMPLE_RATE;
}
