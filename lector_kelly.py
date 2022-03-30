import can
import os
import time
import matplotlib.pyplot as plt
import matplotlib.animation as animation
import numpy as np

# implementar lecturas del notepad, actualizar solo los valores actuales con esto
# voltaje bateria -- CCP_A2D_BATCH_READ2/1
# corriente bateria -- CCP_A2D_BATCH_READ2/1
# temperatura inversor -- CCP_MONITOR1
# rpm -- CCP_MONITOR2
# corriente motor -- CCP_A2D_BATCH_READ2/1
# velocidad -- rpm
def RPM2KMH(RPM):
    return 2*3.6*np.pi*0.3*RPM/60

def P_in(V_bat, I_bat):
    return V_bat * I_bat    

def P_out(Trq, Rpm):
    rads = 2 * np.pi * Rpm / 60
    return Trq * rads
    
# commands
IZQ = 0xc8
DER = 0x64
CCP_A2D_BATCH_READ1_DER=can.Message(data=[0x1b], arbitration_id=DER, extended_id=False)
CCP_A2D_BATCH_READ2_DER=can.Message(data=[0x1a], arbitration_id=DER,extended_id=False)
CCP_MONITOR1_DER       =can.Message(data=[0x33], arbitration_id=DER,extended_id=False)
CCP_MONITOR2_DER       =can.Message(data=[0x37], arbitration_id=DER,extended_id=False)
CCP_A2D_BATCH_READ1_IZQ=can.Message(data=[0x1b], arbitration_id=IZQ,extended_id=False)
CCP_A2D_BATCH_READ2_IZQ=can.Message(data=[0x1a], arbitration_id=IZQ,extended_id=False)
CCP_MONITOR1_IZQ       =can.Message(data=[0x33], arbitration_id=IZQ,extended_id=False)
CCP_MONITOR2_IZQ       =can.Message(data=[0x37], arbitration_id=IZQ,extended_id=False)
cmds = [CCP_MONITOR1_DER, CCP_MONITOR2_DER, CCP_MONITOR1_IZQ, CCP_MONITOR2_IZQ]
i = 0

#print('\n\rCAN Rx test')
#print('Bring up CAN0....')
os.system("sudo /sbin/ip link set can0 up type can bitrate 1000000")
try:
	bus = can.interface.Bus(channel='can0', bustype='socketcan_native', bitrate=1000000)
except OSError:
	#print('Cannot find PiCAN board.')
	exit()
#print('Ready')

def request(i,bus):
    data = []
    # ver si esta cosa recoje el sender o el recv y como se comporta en el while loop (capaz necesita un delay entre cada mensaje)
    for msg in bus:
        data = msg.data
        break
    cod_id = [100, 200, 101, 201][i]
    if i == 0:
        battery_C =0
        battery_V =0
        inverter_temp =data[3]
        return 'COD_ID:{},Bat_I:{},Bat_V:{},Inv_temp:{},P_in:{}'.format(cod_id,battery_C,battery_V,inverter_temp, P_in(battery_C,battery_V))  
    elif i == 1:
        motor_C=0
        motor_torque=0
        RPM=(data[0] << 8) | data[1]
        return 'COD_ID:{},motor_I:{},torque:{},Km/H:{},RPM:{},POUT:{}'.format(cod_id,motor_C,motor_torque,RPM2KMH(RPM), RPM, P_out(motor_torque,RPM))      
    elif i == 2:
        battery_C=0
        battery_V=0
        inverter_temp=data[3]
        return 'COD_ID:{},Bat_I:{},Bat_V:{},Inv_temp:{},P_in:{}'.format(cod_id,battery_C,battery_V,inverter_temp, P_in(battery_C,battery_V))  
    elif i == 3:
        motor_C=0
        motor_torque=0
        RPM=(data[0] << 8) | data[1]
        return 'COD_ID:{},motor_I:{},torque:{},Km/H:{},RPM:{},POUT:{}'.format(cod_id,motor_C,motor_torque,RPM2KMH(RPM), RPM, P_out(motor_torque,RPM))  

i=0
try:
    while True:
        #
        bus.send(cmds[i])
        print(request(i,bus))
        # ojo con esto de los mensajes, verificar si funciona o si tengo que poner un awaiit o algo asi
        i = i+1
        if i == 4:
            i = 0
            time.sleep(0.2)
		#message = bus.recv()


except KeyboardInterrupt:
	#Catch keyboard interrupt
	os.system("sudo /sbin/ip link set can0 down")
	#print('\n\rKeyboard interrtupt')
