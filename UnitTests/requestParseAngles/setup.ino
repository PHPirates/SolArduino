
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

void setupNAS() {
  // if (!ether.dnsLookup(website))
  //   Serial.println("DNS failed");
   //instead of dns lookup, set hisip (ip of NAS) manually to be used by browseURL
   ether.hisip[0]=192;
   ether.hisip[1]=168;
   ether.hisip[2]=8;
   ether.hisip[3]=200;
}

void setupPanels() {
  requestNewTable(); //fill the angles and dates arrays
  while(!responseReceived) { //wait for response before continuing
    ether.packetLoop(ether.packetReceive()); //keep receiving response
  }
}

