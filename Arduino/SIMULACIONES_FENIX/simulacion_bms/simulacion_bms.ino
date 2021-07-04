#include <Serial_CAN_Module.h>
#include <SoftwareSerial.h>
Serial_CAN can;
#define can_tx  2           // tx of serial can module connect to D2
#define can_rx  3           // rx of serial can module connect to D3

// 622
byte DATA_622[8];

// State of system
unsigned int fault_state = 0b00000001;
unsigned int K1_contactor = 0b00000010;
unsigned int K2_contactor = 0b00000100;
unsigned int K3_contactor = 0b00001000;
unsigned int relay_fault = 0b00010000;
byte STATE_0 = fault_state | K1_contactor | K2_contactor | K3_contactor | relay_fault;

unsigned int powerup_time = 57;
byte TIMER_1 = powerup_time | 0xFF;
byte TIMER_2 = (powerup_time >> 8) | 0xFF;

// Byte of flags
unsigned int power_from_source = 0b00000001;
unsigned int power_from_load = 0b00000010;
unsigned int interlock_tripped = 0b00000100;
unsigned int hard_wire_contactor_request = 0b00001000;
unsigned int can_contactor_request = 0b00010000;
unsigned int HLIM_set = 0b00100000;
unsigned int LLIM_set = 0b01000000;
unsigned int fan_on = 0b10000000;
byte FLAGS_3 = power_from_source | power_from_load | interlock_tripped | hard_wire_contactor_request | can_contactor_request | HLIM_set | LLIM_set | fan_on;

// Fault code, stored
unsigned int fault_code = 57;
byte FAULT_CODE_4 = fault_code & 0xFF;

// Level fault flags
unsigned int driving_off_while_plugged_in = 0b00000001;
unsigned int interlock_tripped2 = 0b00000010;
unsigned int comm_fault_blank_or_cell = 0b00000100;
unsigned int charge_overcurrent = 0b00001000;
unsigned int discharge_overcurrent = 0b00010000;
unsigned int over_temp = 0b00100000;
unsigned int under_voltage = 0b01000000;
unsigned int over_voltage = 0b10000000;
byte LEVEL_FAULTS_5 = driving_off_while_plugged_in | interlock_tripped2 | comm_fault_blank_or_cell | charge_overcurrent | discharge_overcurrent | over_temp | under_voltage | over_voltage;

// Warning flags
unsigned int low_voltage = 0b00000001;
unsigned int high_voltage = 0b00000010;
unsigned int charge_overcurrent_warning = 0b00000100;
unsigned int discharge_overcurrent_warning = 0b00001000;
unsigned int cold_temp = 0b00010000;
unsigned int hot_temp = 0b00100000;
unsigned int low_SOH = 0b01000000;
unsigned int isolateion_fault = 0b10000000;
byte WARNING_FLAGS_6 = low_voltage | high_voltage | charge_overcurrent_warning | discharge_overcurrent_warning | cold_temp | hot_temp | low_SOH | isolateion_fault;

// 623
byte DATA_623[8];

unsigned int pack_voltage = 65535;
unsigned int min_voltage = 215;
unsigned int min_voltage_id = 28;
unsigned int max_voltage = 215;
unsigned int max_voltage_id = 28;

// 624
byte DATA_624[8];

unsigned int current = 32764;
unsigned int charge_limit = 60000;
unsigned int discharge_limit = 60001;

// 625
byte DATA_625[8];

unsigned long batt_energy_in = 4294967294;
unsigned long batt_energy_out = 3294967294;

// 626
byte DATA_626[8];

unsigned int soc = 57;
unsigned int dod = 65535;
unsigned int capacity = 30001;
unsigned int soh = 11;

// 627
byte DATA_627[8];

unsigned int temperature = 0b10000011; // -125
unsigned int min_temp = 0b11110110; // -10
unsigned int min_temp_id = 28;
unsigned int max_temp = 30;
unsigned int max_temp_id = 27;

// 628
byte DATA_628[8];

unsigned int pack_resistance = 65500;
unsigned int min_resistance = 178;
unsigned int min_resistance_id = 15;
unsigned int max_resistance = 201;
unsigned int max_resistance_id = 16;

// 700
byte DATA_700[8];

unsigned int cell_number = 7;
unsigned int voltage = 201; // 4.01
unsigned int temp_all_time = 50; // -78
unsigned int temp_when_balancing_off = 134; // 6 C
unsigned int resistance = 179; // 17.9 miliOhm
// Status
unsigned int voltage_read_ok = 0b00000001;
unsigned int temp_read_ok = 0b00000010;
unsigned int resistance_read_ok = 0b00000100;
unsigned int load_is_on = 0b00001000;
unsigned int voltage_sensor_fault = 0b00010000;
unsigned int temperature_sensor_fault = 0b00100000;
unsigned int resistance_calculation_fault = 0b01000000;
unsigned int load_fault = 0b10000000;
byte STATUS_5 = voltage_read_ok | temp_read_ok | resistance_read_ok | load_is_on | voltage_sensor_fault | temperature_sensor_fault | resistance_calculation_fault | load_fault;

// 721-724
byte DATA_701[8] = {201, 202, 203, 204, 205, 206, 207, 208};
byte DATA_702[8] = {201, 202, 203, 204, 205, 206, 207, 208};
byte DATA_703[8] = {201, 202, 203, 204, 205, 206, 207, 208};
byte DATA_704[8] = {201, 202, 203, 204, 205, 206, 207, 208};

void setup() {
  //622
  DATA_622[0] = STATE_0;
  DATA_622[1] = TIMER_1;
  DATA_622[2] = TIMER_2;
  DATA_622[3] = FLAGS_3;
  DATA_622[4] = FAULT_CODE_4;
  DATA_622[5] = LEVEL_FAULTS_5;
  DATA_622[6] = WARNING_FLAGS_6;
  //623
  DATA_623[0] = (pack_voltage >> 8) & 0xFF;
  DATA_623[1] = pack_voltage & 0xFF;
  DATA_623[2] = min_voltage & 0xFF;
  DATA_623[3] = min_voltage_id & 0xFF;
  DATA_623[4] = max_voltage & 0xFF;
  DATA_623[5] = max_voltage_id & 0xFF;
  //624
  DATA_624[0] = (current >> 8) & 0xFF;
  DATA_624[1] = current & 0xFF;
  DATA_624[2] = (charge_limit >> 8) & 0xFF;
  DATA_624[3] = charge_limit & 0xFF;
  DATA_624[4] = (discharge_limit >> 8) & 0xFF;
  DATA_624[5] = discharge_limit & 0xFF;
  //625
  DATA_625[0] = (batt_energy_in >> 8*3) & 0xFF;
  DATA_625[1] = (batt_energy_in >> 8*2) & 0xFF;
  DATA_625[2] = (batt_energy_in >> 8*1) & 0xFF;
  DATA_625[3] = (batt_energy_in >> 8*0) & 0xFF;
  DATA_625[4] = (batt_energy_out >> 8*3) & 0xFF;
  DATA_625[5] = (batt_energy_out >> 8*2) & 0xFF;
  DATA_625[6] = (batt_energy_out >> 8*1) & 0xFF;
  DATA_625[7] = (batt_energy_out >> 8*0) & 0xFF;
  //626
  DATA_626[0] = soc & 0xFF;
  DATA_626[1] = (dod >> 8) & 0xFF;
  DATA_626[2] = dod & 0xFF;
  DATA_626[3] = (capacity >> 8) & 0xFF;
  DATA_626[4] = capacity & 0xFF;
  DATA_626[5] = 0x00;
  DATA_626[6] = soh;
  //627
  DATA_627[0] = temperature & 0xFF;
  DATA_627[2] = min_temp & 0xFF;
  DATA_627[3] = min_temp_id & 0xFF;
  DATA_627[4] = max_temp & 0xFF;
  DATA_627[5] = max_temp_id & 0xFF;
  //628
  DATA_628[0] = (pack_resistance >> 8) & 0xFF;
  DATA_628[1] = pack_resistance & 0xFF;
  DATA_628[2] = min_resistance & 0xFF;
  DATA_628[3] = min_resistance_id & 0xFF;
  DATA_628[4] = max_resistance & 0xFF;
  DATA_628[5] = max_resistance_id & 0xFF;
  //700
  DATA_700[0] = cell_number & 0xFF;
  DATA_700[1] = voltage & 0xFF;
  DATA_700[2] = temp_all_time & 0xFF;
  DATA_700[3] = temp_when_balancing_off & 0xFF;
  DATA_700[4] = resistance & 0xFF;
  DATA_700[5] = STATUS_5;

  Serial.begin(9600);
  can.begin(can_tx, can_rx, 9600);      // tx, rx
}

void loop() {
  can.send(0x622, 0, 0, 8, DATA_622);
  can.send(0x623, 0, 0, 8, DATA_623);
  can.send(0x624, 0, 0, 8, DATA_624);
  can.send(0x625, 0, 0, 8, DATA_625);
  can.send(0x626, 0, 0, 8, DATA_626);
  can.send(0x627, 0, 0, 8, DATA_627);
  can.send(0x628, 0, 0, 8, DATA_628);

  can.send(0x700, 0, 0, 8, DATA_700);
  can.send(0x701, 0, 0, 8, DATA_701);
  can.send(0x702, 0, 0, 8, DATA_702);
  can.send(0x703, 0, 0, 8, DATA_703);
  can.send(0x704, 0, 0, 8, DATA_704);

  delay(1000);
}
