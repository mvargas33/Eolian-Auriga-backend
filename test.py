import can
import time
import os
import threading
import time
import matplotlib.pyplot as plt
import matplotlib.animation as animation
from datetime import *
import numpy as np



# si el dato es positivo --> 00 00 00 00 + dato leido
# si el dato negativo --> FF FF FF FF - dato
fig = plt.figure()
ax = fig.add_subplot(1, 1, 1)

def m_hex(num):
    return hex(num)[2:]

def parse_msg(data, start, end):
    msg = ""
    end += 1
    for i in range(start, end):
        msg = m_hex(data[i]) + msg
    return int(msg, 16)



def read_messages(bus,TPDO,s):   
   for msg in bus:
        data = msg.data
        msg = str(msg)
        cod_id = int(msg[39:45])
        if cod_id == 100:           
            #attery_V = int(hex(data[1])[2:]+hex(data[0])[2:], 16)*sf["battery_V"]
            battery_V = parse_msg(data, 0, 1)*sf["battery_V"]
            #battery_C = int(hex(data[3])[2:] + hex(data[2])[2:], 16)*sf["battery_C"]
            battery_C = parse_msg(data, 2, 3) * sf["battery_C"]
            if battery_C >= 4095:
                battery_C = 0
            #inverter_temp = int(hex(data[4])[2:], 16)*sf["inverter_I"]
            inverter_temp = parse_msg(data, 4,4)*sf["inverter_I"]
            return 'COB_ID:'+str(cod_id)+','+'Battery_C:'+str(battery_C)+','+'Battery_V:'+str(battery_V)+','+'heatsink:'+str(inverter_temp)+','+','+'PIN:'+str(battery_C*battery_V)+'\n'
        if cod_id == 200:
            #motor_C = int(hex(data[1])[2:] + hex(data[0])[2:], 16)*sf["motor_C"]
            motor_C = parse_msg(data, 0,1)*sf["motor_C"]
            #motor_torque = int(hex(data[3])[2:] + hex(data[2])[2:], 16)*sf["motor_torque"]
            motor_torque = parse_msg(data, 2,3)*sf["motor_torque"]
            if motor_torque >= 85:
                motor_torque = -(int("FFFF",16) - int("FFFF", 16) + 4096 - motor_torque)
            #RPM = int(hex(data[7])[2:] + hex(data[6])[2:] + hex(data[5])[2:] + hex(data[4])[2:], 16)*sf["motor_RPM"]
            RPM = parse_msg(data, 4,7)*sf["motor_RPM"]
            if RPM >= 900:
                RPM = -(int("FFFFFFFF",16) - RPM) # FF FF FF FF - rpm
            return 'COB_ID:'+str(cod_id)+','+'motorC:'+str(motor_C)+','+'torque:'+str(motor_torque)+','+'KM/H:'+str(2*3.6*np.pi*0.3*RPM/60)+','+'RPM:'+str(RPM)+','+'POUT:'+str(motor_torque*RPM*2*np.pi/60)+'\n'
        if cod_id == 400:
        #motor_C = int(hex(data[1])[2:] + hex(data[0])[2:], 16)*sf["motor_C"]
            acelerador = parse_msg(data, 0, 1) * 0.00390625 #*sf["battery_V"]
            status = parse_msg(data, 0, 1) * 1
            freno = parse_msg(data, 4,5)*0.00390625
            return 'COB_ID:'+str(cod_id)+','+'acelerador:'+str(acelerador)+','+'status:'+str(status)+','+'freno:'+str(freno)+'\n'
        if con_id == 300:
            Tiq =  parse_msg(data, 0, 1) *sf["motor_C"] 
            Iq = parse_msg(data, 2, 3)*sf["motor_C"] 
            actual_tq = parse_msg(data, 4, 5) *sf["motor_torque"]
            return 'COB_ID:'+str(cod_id)+','+'Iq_target:'+str(Tiq)+','+'Iq:'+str(Iq)+','+'Torque_actual:'+str(actual_tq)+'\n'

#             return 
#        q16) - int("FFFF", 16) + 4096 - Iq)
#             motor_tq = parse_msg(data, 4,5)*sf["motor_torque"]
#             if motor_tq >= 85:
#                 motor_tq = -(int("FFFF",16) - int("FFFF", 16) + 4096 - motor_tq)
#             return 'COB_ID : ' + str(cod_id) +' , '+' Target_Iq : '+ str(Target_Iq) +' ,'+' Iq : ' + str(Iq)+' ,'+' motor_torque : ' + str(motor_tq)+'\n'

TPDO = {"TPDO1_id": "0100","TPDO2_id": "0200","TPDO3_id": "0300","TPDO4_id": "0400","TPDO5_id": "0500"}
sf = {"motor_RPM": 1,"motor_C": 1,"inverter_I": 1,"battery_V": 0.0625, "battery_C": 0.0625,"motor_torque": 0.0625,"throttle_V": 0.00390625}
sf1 = {"Torque actual value": 0.0625,"Velocity actual value - left motor RPM": 1,"Target Iq (Ia)": 0.0625,"battery_V": 0.0625, "battery_C": 0.0625,"motor_torque": 0.0625,"throttle_V": 0.00390625}
print('\n\rCAN Rx test')
print('Bring up CAN1....')
os.system("sudo /sbin/ip link set can0 up type can bitrate 1000000")
try:
	bus = can.interface.Bus(channel='can0', bustype='socketcan_native')
except OSError:
	print('Cannot find PiCAN board.')
	exit()
print('Ready')
try:
    while True:
        d=read_messages(bus,TPDO,sf)
        print(d)
		#message = bus.recv()


except KeyboardInterrupt:
	#Catch keyboard interrupt
	os.system("sudo /sbin/ip link set can0 down")
	print('\n\rKeyboard interrtupt')