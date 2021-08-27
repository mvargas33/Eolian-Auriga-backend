/*
  Arduino Slave for Raspberry Pi Master
  i2c_slave_ard.ino
  Connects to Raspberry Pi via I2C
  
  DroneBot Workshop 2019
  https://dronebotworkshop.com
*/
 
// Include the Wire library for I2C
#include <Wire.h>
 
// LED on pin 13
//const int ledPin = 13; 
 byte I2C_RequestCommand = 0;
 
void setup() {
  // Join I2C bus as slave with address 8
  Wire.begin(0x04);
  
  Wire.onReceive(processI2CEvent);  
  Wire.onRequest(processRequestEvent);
  Serial.begin(9600);

  
  // Setup pin 13 as output and turn LED off
  //pinMode(ledPin, OUTPUT);
  //digitalWrite(ledPin, LOW);
}
 
void processI2CEvent(int howMany){ 
  byte I2C_command = 0;

  while (Wire.available ())
  {
    I2C_command = Wire.read(); // receive byte as a character
  }

  if (I2C_command > 0) { // We have indeed received a valid command
    switch (I2C_command){

    case 0x01:
      Serial.println("0x01 receive");
      // Store the actual request so that the onRequest handler knows
      // what to do
      I2C_RequestCommand = 0x01;
      break;
    case 0x02:
      Serial.println("0x02 receive");
      // Store the actual request so that the onRequest handler knows
      // what to do
      I2C_RequestCommand = 0x02;
      break;

    default:
      break;
    }

    I2C_command = 0;
  }
}

void processRequestEvent(void)
{
  volatile byte* returnPtr;
  byte buffer[10];
  switch (I2C_RequestCommand){
    // Asume that the receiving end uses the same float representation
  case 0x01:
    Serial.println("request 0x01");
    //returnPtr = (byte*) &myValue; 
    //Wire.send((byte *)returnPtr,4);
    
    buffer[0] = 0x01;
    buffer[1] = 0x02;
    buffer[2] = 0x03;
    buffer[3] = 0x04;
    buffer[4] = 0x05;
    buffer[5] = 0x06;
    buffer[6] = 0x07;
    buffer[7] = 0x08;
    buffer[8] = 0x09;
    buffer[9] = 0x0a;
    Wire.write( buffer, 10);
    break;
  case 0x02:
    Serial.println("request 0x02");
    //returnPtr = (byte*) &myValue; 
    //Wire.send((byte *)returnPtr,4);
    buffer[0] = 0x0a;
    buffer[1] = 0x09;
    buffer[2] = 0x08;
    buffer[3] = 0x07;
    buffer[4] = 0x06;
    buffer[5] = 0x05;
    buffer[6] = 0x04;
    buffer[7] = 0x03;
    buffer[8] = 0x02;
    buffer[9] = 0x01;
    Wire.write( buffer, 10);
    break;

  default:
    break;
  }

  I2C_RequestCommand = 0;
}
void loop() {
  //delay(1000);
}
