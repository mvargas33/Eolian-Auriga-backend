#include <Serial_CAN_Module.h>
#include <SoftwareSerial.h>
Serial_CAN can;
#define can_tx  2           // tx of serial can module connect to D2
#define can_rx  3           // rx of serial can module connect to D3

// Dummy data
byte bms_100[8]= {0x00, 0b00000001, 0b00000010, 0b10000011, 0b10000100};
byte bms_101[8]= {0x01, 0b00000101, 0b00000110, 0b10000111, 0b10001000};
byte bms_102[8]= {0x02, 0b00000000, 0b00000000, 0b10000000, 0b10000000};
byte bms_081[8]= {0x13, 0b00001001, 0b00001010, 0b10001011, 0b10001100};
byte bms_082[8]= {0x15, 0b00001101, 0b00001110, 0b10001111, 0b10010000};
byte bms_036[8]= {0x10, 0b00010001, 0b00010010, 0b10010011, 0b10010100};

void setup() {
  Serial.begin(9600);
  Serial.println("begin sending");
  can.begin(can_tx, can_rx, 9600);      // tx, rx
}

void loop() {
  Serial.println("Sent message");
  can.send(0x100, 0, 0, 8, bms_100);
  delay(1000);
  can.send(0x101, 0, 0, 8, bms_101);
  delay(1000);
  can.send(0x102, 0, 0, 8, bms_101);
  delay(1000);
  can.send(0x081, 0, 0, 8, bms_081);
  delay(1000);
  can.send(0x082, 0, 0, 8, bms_082);
  delay(1000);
  can.send(0x036, 0, 0, 8, bms_036);
  delay(1000);
}
