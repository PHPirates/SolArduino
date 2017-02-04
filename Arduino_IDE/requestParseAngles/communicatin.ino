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
        EmergencyState = "Length mismatch";
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

  //if result is e.g. HTTP/1.1 500 Internal Server Error
  //or otherwise malformed, both arrays will contain just zeroes.
  if (dates[0] == 0) {
    EmergencyState = F("Something wrong with NAS, maybe out of angles?");
  }

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
    delay(42); // Make sure the request is sent and received properly, no delay results in a 400 bad request
    Serial.println(result);
    parseString(result); // fill the arrays with the data
    responseReceived = true;
  }
}
