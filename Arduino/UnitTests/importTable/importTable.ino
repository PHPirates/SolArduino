#include <EtherCard.h>
#include "numbers.h"
const int TABLE_LENGTH = 3;
int tableIndex;
boolean autoMode = true;
byte Ethernet::buffer[700];

//NEW
const char website[] PROGMEM = "http://192.168.2.7";


void setup() {
  Serial.begin(9600);
  Serial.println(angles[1]);
  Serial.println(dates[3]);
  solarPanelAuto();

  if (!ether.dnsLookup(website))
    Serial.println("DNS failed");
    
  ether.printIp("SRV: ", ether.hisip);
}

void loop() {
  ether.packetLoop(ether.packetReceive()); //something to do with http request

  
  long noww = 1471903201;
  if (tableIndex+1 >= TABLE_LENGTH) {
    requestNewTable();
  } else if (autoMode && dates[tableIndex+1]<noww) { //if time walked into next part
    tableIndex++;
      Serial.println(angles[tableIndex]);
//  setSolarPanel(angles[tableIndex]);

  }
}

void solarPanelAuto() {
  long noww = 1471903201;
  int i = 0;
  while(dates[i]<noww && i<TABLE_LENGTH){
    Serial.println(i);
    i++;
  }

  if (i >= TABLE_LENGTH) { //in the case we ran out of angles 
    Serial.print("index too large: ");
    Serial.println(i);
    tableIndex = i;
    requestNewTable();
  } else {
      tableIndex = i-1; //correct for i being one too much after searching to get corresponding angle
      Serial.print("set on auto, going to degrees: ");
  Serial.println(angles[tableIndex]);
//  setSolarPanel(angles[tableIndex]);
  }
}

void requestNewTable() {
  Serial.println("requesting new data");
  dates[0] = 1471903200;
  dates[1] = 1471903210;
  Serial.println();
  Serial.print("<<< REQ ");
  ether.browseUrl(PSTR("/index.php"), "", website, my_callback);
    
  tableIndex = 0; //reset global index
}

// called when the client request is complete
static void my_callback (byte status, word off, word len) {
  Serial.println(">>>");
  Ethernet::buffer[off+300] = 0;
  Serial.print((const char*) Ethernet::buffer + off);
  Serial.println("...");
}

