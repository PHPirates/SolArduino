void setPinModes() {
  pinMode(POWER_HIGH,OUTPUT);
  pinMode(DIRECTION_PIN,OUTPUT);
  pinMode(POWER_LOW,OUTPUT);
  pinMode(POTMETERPIN,INPUT);
}

void setupEthernet() {
  //do not forget to add the extra '10' argument because of this ethernet shield
  if (ether.begin(sizeof Ethernet::buffer, mymac, 10) == 0) {
    Serial.println(F("Failed to access Ethernet controller"));
  }

  // run dhcp to find new gw and dns
  ether.dhcpSetup();
  // ether.printIp("IP:  ", ether.myip);
  // ether.printIp("GW:  ", ether.gwip);
  // ether.printIp("DNS: ", ether.dnsip);
  // ether.printIp("SRV: ", ether.hisip);
  ether.staticSetup(myip,gw,dns,mask); //returns true anyway
  //no serial print because ether.myip is a char[] array
  ether.printIp("Address: http://", ether.myip);
}

void setupNTP() {
  //NTP syncing
  //Find ip address of a time server from the pool
   if (!ether.dnsLookup(poolNTP)) {
     Serial.println("DNS failed");
   }
   ether.printIp("Lookup IP   : ", ether.hisip);
   //sync arduino clock, current time in seconds can be found with now();
   //setTime(timeZone.toLocal(getNtpTime())); // why change the time in seconds, as that has nothing to do with timezones?
   setTime(getNtpTime());
   Serial.print("time: ");
   Serial.println(now());
   Serial.print(hour());
   Serial.print(F(":"));
   Serial.println(minute());
}

void setupNAS() {
  // if (!ether.dnsLookup(website))
  //   Serial.println("DNS failed");
   //instead of dns lookup, set hisip (ip of NAS) manually to be used by browseURL
   ether.hisip[0]=91;
   ether.hisip[1]=184;
   ether.hisip[2]=13;
   ether.hisip[3]=53;
   //below not applicable anymore, but could always change
   // the http request needs a different dns, so we set that here and do setup again
   // dns[0] = 192;
   // dns[1] = 168;
   // dns[2] = 8;
   // dns[3] = 1;
   //after doing NTP setup, this helps in avoiding a 400 bad request
   ether.staticSetup(myip,gw,dns,mask);
}

void setupPanels() {
  solarPanelStop(); //just in case, default is stopped
  autoMode = false; // by default, do nothing (safer)
  requestNewTable(); //fill the angles and dates arrays
  while(!responseReceived) { //wait for response before continuing
    ether.packetLoop(ether.packetReceive()); //keep receiving response
  }
}
