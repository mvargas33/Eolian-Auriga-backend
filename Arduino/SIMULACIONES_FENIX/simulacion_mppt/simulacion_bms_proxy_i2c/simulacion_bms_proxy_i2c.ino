// DEBUG

//#define serial_print // Usar para ver que los mppt se están leyendo bien

#define debug_i2c
// CANBUS
#include <Serial_CAN_Module.h>
#include <SoftwareSerial.h>
Serial_CAN can;
#define can_tx  1           // tx of serial can module connect to D2
#define can_rx  0           // rx of serial can module connect to D3
unsigned long id = 0;
unsigned long id_aux = 0;// Just to fix a bug with print
unsigned char buff[8];

// Data
unsigned char bms_100[8];
unsigned char bms_101[8];
unsigned char bms_102[8];
unsigned char bms_081[8];
unsigned char bms_082[8];
unsigned char bms_036[8];

// I2C
#include <Wire.h>
byte I2C_RequestCommand = 0;

// When 0x08 is called, we store the request command
// Executed when a slave device receives a transmission from a master. (a.k.a Master wirtes on slave)
void processWriteEvent(int numBytes){
  
  while (Wire.available ()){
    byte I2C_command = Wire.read(); // receive byte as a character
    if (I2C_command > 0) { // Check that its a valid command
      I2C_RequestCommand = I2C_command; // Store globally
      #ifdef debug_i2c
        Serial.print("Write from master: ");Serial.println(I2C_RequestCommand, HEX);
      #endif
    }
  }
}

// Handler given the stored command
// Executed when a master requests data from this slave device. (a.k.a Master request from slave)
void processRequestEvent(void){
  #ifdef debug_i2c
    Serial.print("Request command: ");Serial.println(I2C_RequestCommand, HEX);
  #endif
  switch (I2C_RequestCommand){
    case 0x01:
      Wire.write( bms_100, 8);
      #ifdef debug_i2c
        Serial.println("0x01 -> Sending 100: ");for(int i = 0; i<7; i++){Serial.println(bms_100[i]);};Serial.println("");
      #endif
      break;
    case 0x02:
      Wire.write( bms_101, 8);
      #ifdef debug_i2c
        Serial.println("0x02 -> Sending 101: ");for(int i = 0; i<7; i++){Serial.println(bms_101[i]);};Serial.println("");
      #endif
      break;
    case 0x03:
      Wire.write( bms_102, 8);
      #ifdef debug_i2c
        Serial.println("0x03 -> Sending 102: ");for(int i = 0; i<7; i++){Serial.println(bms_102[i]);};Serial.println("");
      #endif
      break;
    case 0x04:
      Wire.write( bms_081, 8);
      #ifdef debug_i2c
        Serial.println("0x03 -> Sending 81: ");for(int i = 0; i<7; i++){Serial.println(bms_081[i]);};Serial.println("");
      #endif
      break;
    case 0x05:
      Wire.write( bms_082, 8);
      #ifdef debug_i2c
        Serial.println("0x04 -> Sending 82: ");for(int i = 0; i<7; i++){Serial.println(bms_082[i]);};Serial.println("");
      #endif
      break;
    case 0x06:
      Wire.write( bms_036, 8);
      #ifdef debug_i2c
        Serial.println("0x05 -> Sending 36: ");for(int i = 0; i<7; i++){Serial.println(bms_036[i]);};Serial.println("");
      #endif
      break;
    default:
      break;
  }
}


/*///////////////////// SET UP /////////////////////*/
void setup() {
  // Config canbus baudrate to 1000KBS for bms
  //Serial.begin(57600);
  can.begin(can_tx, can_rx, 57600); // CANBUS baudrate is set to 125 KB
  Wire.begin(0x8); // Join I2C bus as slave with address 8
  Wire.setClock(400000); // Set Hz to max RP4 Hz allowed
  Wire.onReceive(processWriteEvent);  
  Wire.onRequest(processRequestEvent);
  
  
  #ifdef serial_print
    Serial.begin(9600);
    Serial.println("Comienza programa de lectura directa de BMS.");
  #endif
  #ifdef debug_i2c
    Serial.begin(9600);
    Serial.println("Comienza programa de debug I2C.");
  #endif
}

/*///////////////////// LOOP /////////////////////*/
void loop() {
  //delay(4); // Delay empírico. Si no está, algo le pasa al canbus y se traba, no actualiza ningún MPPT.
  #ifdef serial_print
    //delay(1000);
  #endif

  if(can.recv(&id, buff)){
    id_aux = id; // Just to fix a bug with print
    #ifdef serial_print
      Serial.print("ID: ");Serial.println(id_aux, HEX);
      for(int i= 0; i<8; i++){
        Serial.print(" ");Serial.print(buff[i], HEX);
      }
      Serial.println("");
    #endif

    if (id_aux == 0x100){
      for(int i=0;i<8;i++){bms_100[i] = buff[i];};
      #ifdef serial_print
        Serial.println("100 updated");
      #endif
    }else if (id_aux == 0x101){
      for(int i=0;i<8;i++){bms_101[i] = buff[i];};
      #ifdef serial_print
        Serial.println("101 updated");
      #endif
    }else if (id_aux == 0x102){
      for(int i=0;i<8;i++){bms_102[i] = buff[i];};
      #ifdef serial_print
        Serial.println("102 updated");
      #endif
    } else if (id_aux == 0x081){
      for(int i=0;i<8;i++){bms_081[i] = buff[i];};
      #ifdef serial_print
        Serial.println("81 updated");
      #endif
    } else if (id_aux == 0x082){
      for(int i=0;i<8;i++){bms_082[i] = buff[i];};
      #ifdef serial_print
        Serial.println("82 updated");
      #endif
    }else if (id_aux == 0x036){
      for(int i=0;i<8;i++){bms_036[i] = buff[i];};
      #ifdef serial_print
        Serial.println("36 updated");
      #endif
    }
    
  }// FIn Check Receive()

  #ifdef serial_print
    Serial.flush();
  #endif
  #ifdef debug_i2c
    Serial.flush();
  #endif
}
