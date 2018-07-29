/*
 * NOT WORKING
 * 
 * There can be multiple devices attached to the bus (by definition of a bus) and these are fixed pins.
 * Each device needs a separate Chip Select pin, this can be any pin.
 * 
 *  SD card attached to SPI bus as follows:
 ** MOSI - pin 11
 ** MISO - pin 12
 ** CLK - pin 13
 ** CS SD Card - pin 4
 * 
 * CS Ethernet - pin 10
*/

#include <tinyFAT.h>

#define CS_SD_CARD 4
#define CS_ETHERNET 10

byte res;

void setup() {
  Serial.begin(9600);

  Serial.println(F("Setting up pins"));

  
  // According to the Arduino docs, when using a different CS (Chip Select) pin than the hardware SS (Slave Select) pin (10), pin 10 has to be kept as output for the SD library
  // https://www.arduino.cc/en/Reference/SDbegin
  // PS This example may work without it, may be coincidence.
//  pinMode(CS_ETHERNET, OUTPUT);

  // disable Ethernet chip (10 has to be set to output)
//  digitalWrite(CS_ETHERNET, HIGH);

  // Disable sd card by default
//  pinMode(CS_SD_CARD, OUTPUT);
//  digitalWrite(CS_SD_CARD, HIGH);

  // ------ end of setup


  // -------- sd card code

  // Activate SD card
//  digitalWrite(CS_SD_CARD, LOW);
  
  file.setSSpin(CS_SD_CARD);
  res = file.initFAT();
  if (res != NO_ERROR) {
    Serial.println(F("Failed to initialize SD card"));
    return;
  }
  Serial.println(F("SD card initialized"));
  Serial.print(F("Card size: "));
  Serial.print(file.BS.partitionSize);
  Serial.println(F("MB"));

}

void loop() {
  // put your main code here, to run repeatedly:

}
