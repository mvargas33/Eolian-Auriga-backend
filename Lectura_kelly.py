from ast import parse
import can
import time
import os
import threading
import time
import matplotlib.pyplot as plt
import matplotlib.animation as animation
from datetime import *
import numpy as np

# implementar lecturas del notepad, actualizar solo los valores actuales con esto
# voltaje bateria -- CCP_A2D_BATCH_READ2/1
# corriente bateria -- CCP_A2D_BATCH_READ2/1
# temperatura inversor -- CCP_MONITOR1
# rpm -- CCP_MONITOR2
# corriente motor -- CCP_A2D_BATCH_READ2/1
# velocidad -- rpm

# commands
ID=0x6B
CCP_A2D_BATCH_READ1=can.Message(data=[0x1b], arbitration_id=ID)
CCP_A2D_BATCH_READ2=can.Message(data=[0x1a], arbitration_id=ID)
CCP_MONITOR1       =can.Message(data=[0x33], arbitration_id=ID)
CCP_MONITOR2       =can.Message(data=[0x37], arbitration_id=ID)
# si el dato es positivo --> 00 00 00 00 + dato leido
# si el dato negativo --> FF FF FF FF - dato
fig = plt.figure()
ax = fig.add_subplot(1, 1, 1)

def send_message(bus, i):
    msg=0
    if i == 0: # CCP_A2D_BATCH_READ1
        msg=CCP_A2D_BATCH_READ1
    elif i==1: # CCP_A2D_BATCH_READ1
        msg=CCP_A2D_BATCH_READ2
    elif i==2: # CCP_MONITOR1
        msg=CCP_MONITOR1
    elif i==3: # CCP_MONITOR2
        msg=CCP_MONITOR2
    bus.send(msg)

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

# min max, minimo y maximo en valor de unidad de medida
# o_min, o_max, minimo y maximo en bytes
# ejemplo, el voltaje llega como numeros del 0 al 255,
# y los valores reales van de 0 a 5V, para convertir un valor x, se debe llamar
# linear_map(x, 0, 5, 0, 255) # el programa se encarga de parsear reales y enteros
def linear_map(value, min, max, o_min, o_max):
    p = value-o_min
    percentage = 1
    if p == o_max - o_min: #error double
        percentage = 1
    else:
        percentage = p*1.0/(o_max-o_min)
    real_value = percentage*1.0*(max-min)+min
    return real_value


motor_torque = 0
def V(volt):
    return volt/1.84
bat_v = 1
def read_messages(bus,TPDO,s, i):   
    global bat_v
    for msg in bus: 
        data = msg.data
        msg = str(msg)
        cod_id = int(msg[39:45])
        if i == 0:           
            freno_v=linear_map(data[0], 0, 5, 0, 255)
            acc_v=linear_map(data[1], 0, 5, 0, 255)
            bat_v=V(data[2])
            return 'freno_v:{},acc_v:{},bat_v:{}'.format(freno_v, acc_v, bat_v)
        elif i == 1:
            bat_c = data[0] * V(data[3]) + data[1] * V(data[4]) + data[2] * V(data[5])/bat_v
            return 'bat_c:{}'.format(bat_c)
        elif i == 2:
            motor_temp = data[2]
            if motor_temp == 0xFF:
                motor_temp = 0
            kelly_temp=data[3]
            return 'motor_temp:{},kelly_temp:{}'.format(motor_temp, kelly_temp)
        elif i == 3:
            rpm=data[0]<<8 | data[1]
            velocidad=RPM2KMH(rpm)
            return 'rpm:{},velocidad:{}'.format(rpm, velocidad)
    return "N"



TPDO = {"TPDO1_id": "0100","TPDO2_id": "0200","TPDO3_id": "0300","TPDO4_id": "0400","TPDO5_id": "0500"}
sf = {"motor_RPM": 1,"motor_C": 1,"inverter_I": 1,"battery_V": 0.0625, "battery_C": 0.0625,"motor_torque": 0.0625,"throttle_V": 0.00390625}
# sf1 = {"Torque actual value": 0.0625,"Velocity actual value - left motor RPM": 1,"Target Iq (Ia)": 0.0625,"battery_V": 0.0625, "battery_C": 0.0625,"motor_torque": 0.0625,"throttle_V": 0.00390625}
print('\n\rCAN Rx test')
print('Bring up CAN0....')
os.system("sudo /sbin/ip link set can0 up type can bitrate 1000000")
try:
	bus = can.interface.Bus(channel='can0', bustype='socketcan_native')
except OSError:
	print('Cannot find PiCAN board.')
	exit()
print('Ready')
i = 0
try:
    while True:
        send_message(bus, i)
        # 
        d=read_messages(bus,TPDO,sf, i)
        # ojo con esto de los mensajes, verificar si funciona o si tengo que poner un awaiit o algo asi
        print(d)
        i = i+1 % 4
		#message = bus.recv()


except KeyboardInterrupt:
	#Catch keyboard interrupt
	os.system("sudo /sbin/ip link set can0 down")
	print('\n\rKeyboard interrtupt')