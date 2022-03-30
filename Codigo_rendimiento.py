import can
import time
import os
import threading
import time
import matplotlib.pyplot as plt
import matplotlib.animation as animation
from datetime import *
import numpy as np
import sys

dev = False
for i in range(len(sys.argv)):
    if sys.argv[i] == "--dev": dev = True

# si el dato es positivo --> 00 00 00 00 + dato leido
# si el dato negativo --> FF FF FF FF - dato
fig = plt.figure()
ax = fig.add_subplot(1, 1, 1)

def twos_comp(val, bits):
    """compute the 2's complement of int value val"""
    if (val & (1 << (bits - 1))) != 0:
        # if sign bit is set e.g., 8bit: 128-255
        val = val - (1 << bits)        # compute negative value
    return val   

def m_hex(num):
    return hex(num)[2:]

def parse_msg(data, start, end):
    msg = ""
    end += 1
    for i in range(start, end):
        msg = m_hex(data[i]) + msg
    return twos_comp(int(msg, 16), (end-start)*8)

def RPM2KMH(RPM):
    return 2*3.6*np.pi*0.3*RPM/60

def P_in(V_bat, I_bat):
    return V_bat * I_bat    

def P_out(Trq, Rpm):
    rads = 2 * np.pi * Rpm / 60
    return Trq * rads

motor_torque = 0

def read_messages(bus,TPDO,s):   
   for msg in bus:
        data = msg.data
        msg = str(msg)
        cod_id = int(msg[39:45])
        if cod_id == 100:           
            battery_V = parse_msg(data, 0, 1) * sf["battery_V"]
            battery_C = parse_msg(data, 2, 3) * sf["battery_C"]
            inverter_temp = parse_msg(data, 4,4)*sf["inverter_I"]
            return 'COD_ID:{},Bat_I:{},Bat_V:{},Inv_temp:{},P_in:{}'.format(cod_id,battery_C,battery_V,inverter_temp, P_in(battery_C,battery_V))  
            
        if cod_id == 200:
            motor_C = parse_msg(data, 0,1)*sf["motor_C"]
            motor_torque = parse_msg(data, 2,3)*sf["motor_torque"]
            RPM = parse_msg(data, 4,7)*sf["motor_RPM"]
#             return 'COB_ID:'+str(cod_id)+','+'motorC:'+str(motor_C)+','+'torque:'+str(motor_torque)+'
#                 ,'+'KM/H:'+str(2*3.6*np.pi*0.3*RPM/60)+','+'RPM:'+str(RPM)+','+'POUT:'+str(motor_torque*RPM*2*np.pi/60)
            return 'COD_ID:{},motor_I:{},torque:{},Km/H:{},RPM:{},POUT:{}'.format(cod_id,motor_C,motor_torque,RPM2KMH(RPM), RPM, P_out(motor_torque,RPM))  
        if cod_id == 400:
            acelerador = parse_msg(data, 0, 1)*sf["battery_V"]
            status = parse_msg(data, 2, 3)*sf["battery_V"]
            freno = parse_msg(data, 4,5)*sf["battery_V"]
            return 'COD_ID:{},acelerador:{},status:{},freno:{}'.format(cod_id,acelerador,status,freno)    
#             return 'COB_ID:'+str(cod_id)+','+'acelerador:
#       '+str(acelerador)+','+'status:'+str(status)+','+'freno:'+str(freno)
        if cod_id == 300:
            Tiq =  parse_msg(data, 0, 1) * sf["motor_torque"]
            Tiq_hex = parse_msg(data, 0, 1)
            Iq = parse_msg(data, 2, 3) * sf["motor_torque"]
            Iq_hex = parse_msg(data, 2, 3)
            actual_tq = parse_msg(data, 4, 5) *sf["motor_torque"]
            return 'COD_ID:{},Iq_target:{},Iq:{},Torque_actual:{},Tiq_hex:{},Iq_hex:{}'.format(cod_id,Tiq,Iq,actual_tq,Tiq_hex,Iq_hex)    
#             return 'COB_ID:'+str(cod_id)+','+'Iq_target:'+str(Tiq)+',
#             '+'Iq:'+str(Iq)+','+'Torque_actual:'+
#             str(actual_tq)+',Tiq_hex:'+str(Tiq_hex)+',Iq_hex:'+str(Iq_hex)
        #slaves
        if cod_id == 101:           
            battery_V = parse_msg(data, 0, 1) * sf["battery_V"]
            battery_C = parse_msg(data, 2, 3) * sf["battery_C"]
            inverter_temp = parse_msg(data, 4,4)*sf["inverter_I"]
            return 'COD_ID:{},Bat_I:{},Bat_V:{},Inv_temp:{},P_in:{}'.format(cod_id,battery_C,battery_V,inverter_temp, P_in(battery_C,battery_V))  
            
        if cod_id == 201:
            motor_C = parse_msg(data, 0,1)*sf["motor_C"]
            motor_torque = parse_msg(data, 2,3)*sf["motor_torque"]
            RPM = parse_msg(data, 4,7)*sf["motor_RPM"]
#             return 'COB_ID:'+str(cod_id)+','+'motorC:'+str(motor_C)+','+'torque:'+str(motor_torque)+'
#                 ,'+'KM/H:'+str(2*3.6*np.pi*0.3*RPM/60)+','+'RPM:'+str(RPM)+','+'POUT:'+str(motor_torque*RPM*2*np.pi/60)
            return 'COD_ID:{},motor_I:{},torque:{},Km/H:{},RPM:{},POUT:{}'.format(cod_id,motor_C,motor_torque,RPM2KMH(RPM), RPM, P_out(motor_torque,RPM))  
        if cod_id == 401:
            acelerador = parse_msg(data, 0, 1)*sf["battery_V"]
            status = parse_msg(data, 2, 3)*sf["battery_V"]
            freno = parse_msg(data, 4,5)*sf["battery_V"]
            return 'COD_ID:{},acelerador:{},status:{},freno:{}'.format(cod_id,acelerador,status,freno)    
#             return 'COB_ID:'+str(cod_id)+','+'acelerador:
#       '+str(acelerador)+','+'status:'+str(status)+','+'freno:'+str(freno)
        if cod_id == 301:
            Tiq =  parse_msg(data, 0, 1) * sf["motor_torque"]
            Tiq_hex = parse_msg(data, 0, 1)
            Iq = parse_msg(data, 2, 3) * sf["motor_torque"]
            Iq_hex = parse_msg(data, 2, 3)
            actual_tq = parse_msg(data, 4, 5) *sf["motor_torque"]
            return 'COD_ID:{},Iq_target:{},Iq:{},Torque_actual:{},Tiq_hex:{},Iq_hex:{}'.format(cod_id,Tiq,Iq,actual_tq,Tiq_hex,Iq_hex)    
#             return 'COB_ID:'+str(cod_id)+','+'Iq_target:'+str(Tiq)+',
#             '+'Iq:'+str(Iq)+','+'Torque_actual:'+
#             str(actual_tq)+',Tiq_hex:'+str(Tiq_hex)+',Iq_hex:'+str(Iq_hex)


TPDO = {"TPDO1_id": "0100","TPDO2_id": "0200","TPDO3_id": "0300","TPDO4_id": "0400","TPDO5_id": "0500"}
sf = {"motor_RPM": 1,"motor_C": 1,"inverter_I": 1,"battery_V": 0.0625, "battery_C": 0.0625,"motor_torque": 0.0625,"throttle_V": 0.00390625}
# sf1 = {"Torque actual value": 0.0625,"Velocity actual value - left motor RPM": 1,"Target Iq (Ia)": 0.0625,"battery_V": 0.0625, "battery_C": 0.0625,"motor_torque": 0.0625,"throttle_V": 0.00390625}
#print('\n\rCAN Rx test')
#print('Bring up CAN0....')
if dev:
    os.system("sudo /sbin/ip link add dev vcan0 type vcan")
    os.system("sudo /sbin/ip link set vcan0 up")
else:
    os.system("sudo /sbin/ip link set can0 up type can bitrate 1000000")
try:
    if dev:
        bus = can.interface.Bus(channel='vcan0', bustype='socketcan')
    else:
        bus = can.interface.Bus(channel='can0', bustype='socketcan_native')
except OSError:
    print('Cannot find PiCAN board.')
    exit()
#print('Ready')
try:
    while True:
        d=read_messages(bus,TPDO,sf)
        print(d)
        #message = bus.recv()


except KeyboardInterrupt:
    #Catch keyboard interrupt
    if dev:
        os.system("sudo /sbin/ip link set vcan0 down")
    else:
        os.system("sudo /sbin/ip link set can0 down")
    print('\n\rKeyboard interrtupt')