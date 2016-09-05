void setup() {
  // put your setup code here, to run once:

   Serial.begin(9600);
  parse("HTTP/1.1 200 OK Server: nginx Date: Sun, 04 Sep 2016 11:02:43 GMT Content-Type: text/html; charset=UTF-8 Connection: close Vary: Accept-Encoding _10_1472985855,1472992720,1472999585,1473006450,1473051780,1473058611,1473065442,1473072273,1473079104,1473085935_43.206504993939,33.48797085722,7.0476353122625,0,69.731646222543,56.910196970006,49.713304612523,43.41411150273,33.631148156268,6.7525193195698"
    );
}

void loop() {
  // put your main code here, to run repeatedly:

}

void parse(const char *everything) {
  
  char from [700];
  strcpy(from, everything);
  char *find;
  int leng;
  char *times;

  find = strtok(from, "_");
  int i = 0;
  while(find != NULL){

    if(i==1){
      leng = atoi(find);
      Serial.println(leng);
    } else if(i==2) {
      Serial.println(find);
    } else if(i==3) {
      Serial.println(find);
       
    }
    find = strtok(NULL, "_");
    i++;
  }

}

