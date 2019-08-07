/*
  SD card read/write

  This code has been confirmed to work on the W5100 Ethernet and SD Card shield.

 This example shows how to read and write data to and from an SD card file
 The circuit:
  
  https://www.instructables.com/id/Arduino-Nano-with-Ethernet-Shield/
  Connect the six ICSP pins such that nano is not underneath the shield (ICSP contains MOSI, MISO, SCK, RESET, VCC, GND and the SPI ones are cross-connected with the Arduino pins)
  Attach all the following pins between Arduino and shield:
  Pins 11, 12, 13 (SPI bus)
  Pin 10 selects the w5100
  Pin 4 selects the SD card

 */

#include <SPI.h>
#include <SD.h>

#define SS_SD_CARD 4 // CS pin

File myFile;

void setup() {
  // Open serial communications and wait for port to open:
  Serial.begin(9600);
  while (!Serial) {
    ; // wait for serial port to connect. Needed for native USB port only
  }

  // According to the Arduino docs, when using a different CS pin than the hardware SS pin (10), pin 10 has to be kept as output for the SD library
  // https://www.arduino.cc/en/Reference/SDbegin
  // PS This example may work without it, may be coincidence.
//  pinMode(10, OUTPUT);
//  digitalWrite(10, HIGH); // disable w5100 SPI before initializing SD
//  
//  pinMode(SS_SD_CARD, OUTPUT);

  Serial.print("Initializing SD card...");

  if (!SD.begin(SS_SD_CARD)) {
    Serial.println("initialization failed!");
    return;
  }
  Serial.println("initialization done.");

  if (SD.exists("test.txt")) {
    Serial.println("test.txt exists.");
  } else {
    Serial.println("test.txt doesn't exist.");
  }

  // open a new file and immediately close it:
  Serial.println("Creating test.txt...");
  myFile = SD.open("test.txt", FILE_WRITE);
  myFile.close();

  // Check to see if the file exists:
  if (SD.exists("test.txt")) {
    Serial.println("test.txt exists.");
  } else {
    Serial.println("test.txt doesn't exist.");
  }
  
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
}

void loop() {
  // nothing happens after setup
}
