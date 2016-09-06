long dates[10];
int angles[10];
int tableLength;

void setup() {
  // put your setup code here, to run once:

   Serial.begin(9600);
//  parse("HTTP/1.1 200 OK Server: nginx Date: Sun, 04 Sep 2016 11:02:43 GMT Content-Type: text/html; charset=UTF-8 Connection: close Vary: Accept-Encoding _10_1472985855,1472992720,1472999585,1473006450,1473051780,1473058611,1473065442,1473072273,1473079104,1473085935_43.206504993939,33.48797085722,7.0476353122625,0,69.731646222543,56.910196970006,49.713304612523,43.41411150273,33.631148156268,6.7525193195698"
//    );
   parseString("HTTP/1.1 200 OK Server: nginx Date: Sun, 04 Sep 2016 11:02:43 GMT Content-Type: text/html; charset=UTF-8 Connection: close Vary: Accept-Encoding _10_1472985855,1472992720,1472999585,1473006450,1473051780,1473058611,1473065442,1473072273,1473079104,1473085935_432,334,70,0,697,569,497,434,336,67"
    );
}

void loop() {
  // put your main code here, to run repeatedly:

}

void parseString(const char *everything) {
  
  char from [300]; //testing what's needed
  strcpy(from, everything);
  char *found;
  int leng;
  char *times;  
  char dateString[120];
  char angleString[30];

  found = strtok(from, "_");
  int i = 0;
  while(found != NULL){

    if(i==1){
      tableLength = atoi(found);
//      Serial.println(leng);
    } else if(i==2) {
      strcpy(dateString,found);
      Serial.println(found);
//      Serial.println(dateString);
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
//    Serial.println(dates[i]);
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

  Serial.println("strings parsed: ");
  Serial.println("dates: ");
  for(int i = 0; i<tableLength; i++) {
    Serial.println(dates[i]);
  }
    Serial.println("angles: ");
  for(int i = 0; i<tableLength; i++) {
    Serial.println(angles[i]);
  }

}

