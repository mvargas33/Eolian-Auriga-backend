#include <Serial_CAN_Module.h>
#include <SoftwareSerial.h>
Serial_CAN can;
#define can_tx  2           // tx of serial can module connect to D2
#define can_rx  3           // rx of serial can module connect to D3
unsigned long id = 0;
unsigned long id_aux = 0;// Just to fix a bug with print
unsigned char buff[8];

byte dta_actual[7] = {0,0,0,0,0,0,0};
unsigned int roulette = 0;

unsigned int BLVR[5] = {0b10000000, 0b00000000, 0b00000000, 0b10000000, 0b10000000};
unsigned int OVT[5]  = {0b01000000, 0b01000000, 0b00000000, 0b01000000, 0b00000000};
unsigned int NOC[5]  = {0b00100000, 0b00100000, 0b00100000, 0b00000000, 0b00000000};
unsigned int UNDV[5] = {0b00010000, 0b00000000, 0b00010000, 0b00010000, 0b00010000};
unsigned int Uin[5]  = {493,        500,        512,        480,        469}; // 493*150.49 = 74191.57 = 74.19157 [V] ~ Voltage real paneles
unsigned int Iin[5]  = {114,        80,         117,        120,        130}; // 114*8.72 = 943.008 = 0.943008 [A] ~ Corriente real paneles
unsigned int Uout[5] = {546,        576,        546,        532,        540}; // 546*208.79 = 11399.34 = 113.9934 [V] ~ Voltage banco cargado
unsigned int Temp[5] = {20,         25,         30,         35 ,        40};

byte MPPT1[7] = {(BLVR[0] & 0xFF)| (OVT[0] & 0xFF)| (NOC[0] & 0xFF)| (UNDV[0] & 0xFF)| ((Uin[0] >> 8) & 0x03) & 0xFF, Uin[0] & 0xFF, ((Iin[0] >> 8) & 0x03) & 0xFF, Iin[0] & 0xFF, ((Uout[0] >> 8) & 0x03)& 0xFF, Uout[0] & 0xFF, Temp[0] & 0xFF};
byte MPPT2[7] = {(BLVR[1] & 0xFF)| (OVT[1] & 0xFF)| (NOC[1] & 0xFF)| (UNDV[1] & 0xFF)| ((Uin[1] >> 8) & 0x03) & 0xFF, Uin[1] & 0xFF, ((Iin[1] >> 8) & 0x03) & 0xFF, Iin[1] & 0xFF, ((Uout[1] >> 8) & 0x03)& 0xFF, Uout[1] & 0xFF, Temp[1] & 0xFF};
byte MPPT3[7] = {(BLVR[2] & 0xFF)| (OVT[2] & 0xFF)| (NOC[2] & 0xFF)| (UNDV[2] & 0xFF)| ((Uin[2] >> 8) & 0x03) & 0xFF, Uin[2] & 0xFF, ((Iin[2] >> 8) & 0x03) & 0xFF, Iin[2] & 0xFF, ((Uout[2] >> 8) & 0x03)& 0xFF, Uout[2] & 0xFF, Temp[2] & 0xFF};
byte MPPT4[7] = {(BLVR[3] & 0xFF)| (OVT[3] & 0xFF)| (NOC[3] & 0xFF)| (UNDV[3] & 0xFF)| ((Uin[3] >> 8) & 0x03) & 0xFF, Uin[3] & 0xFF, ((Iin[3] >> 8) & 0x03) & 0xFF, Iin[3] & 0xFF, ((Uout[3] >> 8) & 0x03)& 0xFF, Uout[3] & 0xFF, Temp[3] & 0xFF};
byte MPPT5[7] = {(BLVR[4] & 0xFF)| (OVT[4] & 0xFF)| (NOC[4] & 0xFF)| (UNDV[4] & 0xFF)| ((Uin[4] >> 8) & 0x03) & 0xFF, Uin[4] & 0xFF, ((Iin[4] >> 8) & 0x03) & 0xFF, Iin[4] & 0xFF, ((Uout[4] >> 8) & 0x03)& 0xFF, Uout[4] & 0xFF, Temp[4] & 0xFF};

//#define serial_print // Descomentar para visualizar solicitudes de direcciones 0x711-0x715 (vacías)

void setup() {
  for(int i=0;i<7;i++){dta_actual[i] = MPPT1[i];};
  can.begin(can_tx, can_rx, 57600);      // tx, rx
  
  #ifdef serial_print
    Serial.begin(9600);
    Serial.println("Comienza programa de simulación de comportamiento de MPPT.");
  #endif
}

void loop() {
  if(can.recv(&id, buff)){
    id_aux = id; // Just to fix a bug with print
    #ifdef serial_print
      Serial.print("GET DATA FROM ID: ");Serial.println(id_aux, HEX);
      for(int i=0; i<8; i++){Serial.print("0x");Serial.print(buff[i], HEX);Serial.print('\t');}
      Serial.println();
    #endif
    
    if (id == 0x711){
      can.send(0x771, 0, 0, 7, dta_actual);   // send(unsigned long id, byte ext, byte rtrBit, byte len, const byte *buf);
    }else if (id == 0x712){
      can.send(0x772, 0, 0, 7, dta_actual);
    }else if (id == 0x713){
      can.send(0x773, 0, 0, 7, dta_actual);
    }else if (id == 0x714){
      can.send(0x774, 0, 0, 7, dta_actual);
    }else if (id == 0x715){
      can.send(0x775, 0, 0, 7, dta_actual);
    }
  }
  // Permutar contenido entre MPPT para simular un poco de aleatoriedad
  switch (roulette) {
    case 0:
      for(int i=0;i<7;i++){dta_actual[i] = MPPT1[i];};
      break;
    case 1:
      for(int i=0;i<7;i++){dta_actual[i] = MPPT2[i];};
      break;
    case 2:
      for(int i=0;i<7;i++){dta_actual[i] = MPPT3[i];};
      break;
    case 3:
      for(int i=0;i<7;i++){dta_actual[i] = MPPT4[i];};
      break;
    case 4:
      for(int i=0;i<7;i++){dta_actual[i] = MPPT5[i];};
      break;
    default:
      roulette = 1;
  }
  roulette = (roulette + 1) % 5;
}
