//tested and worked

//ADD
unsigned long upTimer = millis();
const int movingTimeout = 2000; // time in milliseconds for the panels to stop moving after having received no command
bool panelsStopped = true; //needed to control timer logic

void setup() {
  Serial.begin(9600);
  receiveHttpRequests();
  checkMovingTimeout();
  receiveHttpRequests();
  checkMovingTimeout();
  receiveHttpRequests();
  
}

void loop() {
  checkMovingTimeout(); //ADD
}

void receiveHttpRequests() {
  if (true) {
    solarPanelUp();
    resetUpTimeout(); //ADD
    Serial.println("timer reset");
  }
}

void checkMovingTimeout() { //ADD
  if ( millis() > upTimer and !panelsStopped ) {
    solarPanelStop();
  }
}

void resetUpTimeout() { //ADD
  upTimer = millis() + movingTimeout; 
}

void solarPanelUp() {
  panelsStopped = false; //ADD
}

void solarPanelStop() {
  panelsStopped = true; //ADD
  Serial.println("panels stopped");
}

