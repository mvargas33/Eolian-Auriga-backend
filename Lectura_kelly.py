import can
import time
import os

# implementar lecturas del notepad, actualizar solo los valores actuales con esto
# voltaje bateria -- CCP_A2D_BATCH_READ2/1
# corriente bateria -- CCP_A2D_BATCH_READ2/1
# temperatura inversor -- CCP_MONITOR1
# rpm -- CCP_MONITOR2
# corriente motor -- CCP_A2D_BATCH_READ2/1
# velocidad -- rpm
channel="can1"
bitrate=1000000
# commands
ID=0x6B
CCP_A2D_BATCH_READ1=can.Message(data=[0x1b], arbitration_id=ID, extended_id=False)
CCP_A2D_BATCH_READ2=can.Message(data=[0x1a], arbitration_id=ID, extended_id=False)
CCP_MONITOR1       =can.Message(data=[0x33], arbitration_id=ID, extended_id=False)
CCP_MONITOR2       =can.Message(data=[0x37], arbitration_id=ID, extended_id=False)
# si el dato es positivo --> 00 00 00 00 + dato leido
# si el dato negativo --> FF FF FF FF - dato

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
    return 2*3.6*3.1415*0.3*RPM/60

def P_in(V_bat, I_bat):
    return V_bat * I_bat    

def P_out(Trq, Rpm):
    rads = 2 * 3.1415 * Rpm / 60
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

def V(volt):
    return volt/1.84

def read_messages(bus,s, i):   
    for msg in bus: 
        data = msg.data
        msg = str(msg)
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

print('\n\rCAN Rx test')
print('Bring up '+channel+'....')
os.system("sudo /sbin/ip link set "+channel+" up type can bitrate "+str(bitrate))
try:
	bus = can.interface.Bus(channel=channel, bustype='socketcan')
except OSError:
	print('Cannot find PiCAN board.')
	exit()
print('Ready')

i = 0
try:
    while True:
        time.sleep(0.3)
        send_message(bus, i)
        # 
        d=read_messages(bus, i)
        # ojo con esto de los mensajes, verificar si funciona o si tengo que poner un awaiit o algo asi
        print(d)
        i = (i+1) % 4
		#message = bus.recv()


except KeyboardInterrupt:
	#Catch keyboard interrupt
	os.system("sudo /sbin/ip link set "+channel" down")
	print('\n\rKeyboard interrtupt')