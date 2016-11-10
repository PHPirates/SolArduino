void receiveHttpRequests() {
  int degrees = -1;
  boolean askedUpdate = false;

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
             Serial.println(F("Auto mode switched on."));
             autoMode = true; //solarPanelAuto() is called later, first we handle off the request
             int angle = getNextAngle();
             //app requirement: degrees come between underscores
             String response = "Auto mode switched on, going to_";
             response += angle;
             response += "_degrees.";
             acknowledge(response.c_str());
         }
         else if (strncmp("?panel=manual ", data, 12) == 0){
             acknowledge("Auto mode switched off.");
             autoMode = false;
         }
         else if (strncmp("?degrees=", data, 9) == 0) {
            degrees = receiveDegrees(data);
         }
         else if (strncmp("?update", data, 7) == 0) {
           askedUpdate = receiveUpdate();
         }
         else {
           Serial.println(F("Page not found"));
         }
     }
   ether.httpServerReply(bfill.position()); //send the reply, if there was one
   delay(42); // no delay here causes a bad request for requesting angles from the NAS
   if(autoMode && !askedUpdate) { //then it is switched on, when not just asking for update
      solarPanelAuto();
    }

   if(degrees != -1) { // if degrees were changed by a manual request, set solar panels
     setSolarPanel(degrees*10); //degrees go in times ten for more precision
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

 //convert string to const char, easier than a modifiable char array
 acknowledge(stringDegrees.c_str());
 autoMode = false;

 return degrees;
}

bool receiveUpdate() {
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
  return true;
}

void parseString(char *from) {
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
      if (TABLE_LENGTH != atoi(found)) {
        Serial.println(F("WARNING length of received values does not match local array length"));
      }
    } else if(i==2) {
      strcpy(dateString,found);
      Serial.println(found);
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
  for(int i = 0; i<TABLE_LENGTH; i++) {
    Serial.println(dates[i]);
  }
    Serial.println(F("angles: "));
  for(int i = 0; i<TABLE_LENGTH; i++) {
    Serial.println(angles[i]);
  }

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

void requestNewTable() {
  Serial.println(F("requesting new data"));
  responseReceived = false; // set boolean to 'wait for request'
  ether.browseUrl(PSTR("/index.php"), "", NULL, my_callback);
  tableIndex = 0; //reset global index
}

// called when the client request is complete
static void my_callback (byte status, word off, word len) {
  if (!responseReceived) { //sometimes there come more responses, but we only need one
    Ethernet::buffer[off+TABLE_SIZE] = 0;
    char* result = (char*) Ethernet::buffer + off;
    delay(42); // Make sure the request is sent and received properly, no delay results in a 400
    parseString(result); // fill the arrays with the data
    responseReceived = true;
  }
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
