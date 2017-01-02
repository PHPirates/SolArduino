 char *data = "?stormstart=1483124192&stormend=1483124200";
//char *data = "?degrees=58";
 long now = 1483124180;
 int degrees;
 
 const int DEGREES_LOWEND = 50;
 boolean autoMode = false;

// ADDED
long stormTimes[2] = {0,0};
boolean wasAuto = false;
int angleBeforeStorm = -1;

void setup() {
  // put your setup code here, to run once:
  Serial.begin(9600);
  // storm start hour from now, end 12 hours later
  receiveHttpRequests();
}

void loop() {
  Serial.print("now: ");
  Serial.println(now);
  // put your main code here, to run repeatedly:

  // ADDED
  if(stormTimes[0] != 0){ // check if storm mode is planned
    Serial.println("storm mode planned");
    enterStormMode();
  } 
  
    // do regular things
    Serial.println("Doing regular things.");
  
  now++;
  delay(1000); 
}

void receiveHttpRequests() {
 
         //data += 5;
         //start parsing data
         if (data[0] == ' ') {
             // No parameters given (http://192.168.2.10), return home page
             //homePage();
             Serial.println("Homepage.");
         }
         else if (strncmp("?panel=up ", data, 10) == 0) {
           Serial.println("Panels up.");
         }
         else if (strncmp("?panel=down ", data, 12) == 0) {
           Serial.println("Panels down.");
         }
         else if (strncmp("?panel=stop ", data, 12) == 0) {
           Serial.println("Panels stop.");
         }
         else if (strncmp("?panel=auto ", data, 12) == 0) {
             Serial.println("Panels auto.");
         }
         else if (strncmp("?panel=manual ", data, 12) == 0){
             Serial.println("Panels manual.");
         }
         else if (strncmp("?degrees=", data, 9) == 0) {
            degrees = receiveDegrees(data);
         }
         else if (strncmp("?update", data, 7) == 0) {
           Serial.println("Updating...");
         }
         else if (strncmp("?storm", data, 6) == 0){ // ADDED
            solarPanelStorm(data);
         }
         else {
           Serial.println(F("Page not found"));
         }
  
}

// reads start and end time from url and stores them in stormTimes[]
void solarPanelStorm(char *data) {
    String startTime;
    for(int i = 12; i < 22; i++) {
      startTime += (char)data[i];
    }
    
    String endTime;
    for(int i = 32; i < 42; i++){
      endTime += (char)data[i];
    }
    
    Serial.print("start time: ");
    Serial.println(startTime);
    
    Serial.print("end time: ");
    Serial.println(endTime);

    stormTimes[0] = atol(startTime.c_str());
    stormTimes[1] = atol(endTime.c_str());
}

void enterStormMode() {
    if (now < stormTimes[0]){
      // no time for storm mode yet
      Serial.println("Before storm.");
    } else if (now >= stormTimes[0] && now < stormTimes[1]){
      Serial.println("During storm.");
      // storm mode active
      setSolarPanel(DEGREES_LOWEND);
      
      if(autoMode){ // check if panels were in autoMode before entering storm mode so they can go back to auto afterwards
        wasAuto = true;
        autoMode = false;
      }

      if(angleBeforeStorm == -1){
        angleBeforeStorm = getCurrentAngle();
      }
    } else if (now > stormTimes[1]){
      Serial.println("After storm.");
      // deactivate storm mode
      stormTimes[0] = 0;
      stormTimes[1] = 0;
      
      if(wasAuto) {
        autoMode = true; // solarPanelAuto() is called in loop()
      } else {
        setSolarPanel(angleBeforeStorm);
        angleBeforeStorm = -1;
      }
    }
}

int receiveDegrees(char *data) {
  //print digit that comes after
  String stringDegrees;
  stringDegrees += (char)data[9]; //convert to char and add to string
  stringDegrees += (char)data[10];

 int degrees = stringDegrees.toInt(); //convert string to integer
 String response = "Panels going to ";
 response += stringDegrees;
 response += " degrees";
 Serial.print(F("panels to degrees: "));
 Serial.println(degrees);

 return degrees;
}

void setSolarPanel(int degrees){
  Serial.print("Panels going to ");
  Serial.println(degrees);
}

int getCurrentAngle(){
  return 42;
}


