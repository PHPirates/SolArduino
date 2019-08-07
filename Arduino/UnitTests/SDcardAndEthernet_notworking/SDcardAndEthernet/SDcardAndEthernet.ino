/*
 * 
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
 
#include <SPI.h>
#include <SD.h>

File myFile;

#define CS_SD_CARD 4
#define CS_ETHERNET 10

void setup() {
  // Open serial communications and wait for port to open:
  Serial.begin(9600);
  while (!Serial) {
    ; // wait for serial port to connect. Needed for native USB port only
  }

  // According to the Arduino docs, when using a different CS (Chip Select) pin than the hardware SS (Slave Select) pin (10), pin 10 has to be kept as output for the SD library
  // https://www.arduino.cc/en/Reference/SDbegin
  // PS This example may work without it, may be coincidence.
  pinMode(CS_ETHERNET, OUTPUT);

  // disable Ethernet chip (10 has to be set to output)
  digitalWrite(CS_ETHERNET, HIGH);

  // Disable sd card by default
  pinMode(CS_SD_CARD, OUTPUT);
  digitalWrite(CS_SD_CARD, HIGH);

  // ------ end of setup


  // -------- sd card code

  // Activate SD card
//  digitalWrite(CS_SD_CARD, LOW);

  Serial.print("Initializing SD card...");

  if (!SD.begin(CS_SD_CARD)) {
    Serial.println("initialization failed!");
    return;
  }
  Serial.println("initialization done.");

  // open the file. note that only one file can be open at a time,
  // so you have to close this one before opening another.
  myFile = SD.open("test.txt", FILE_WRITE);

  // if the file opened okay, write to it:
  if (myFile) {
    Serial.print("Writing to test.txt...");
    myFile.println("testing 1, 2, 3.");
    // close the file:
    myFile.close();
    Serial.println("done.");
  } else {
    // if the file didn't open, print an error:
    Serial.println("error opening test.txt");
  }

    // re-open the file for reading:
  myFile = SD.open("test.txt");
  if (myFile) {
    Serial.println("test.txt:");

    // read from the file until there's nothing else in it:
    while (myFile.available()) {
      Serial.write(myFile.read());
    }
    // close the file:
    myFile.close();
  } else {
    // if the file didn't open, print an error:
    Serial.println("error opening test.txt");
  }

  // deactivate SD card
  digitalWrite(CS_SD_CARD, HIGH);
  
}

void loop() {
  // put your main code here, to run repeatedly:

}
