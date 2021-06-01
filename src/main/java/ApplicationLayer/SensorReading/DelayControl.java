package ApplicationLayer.SensorReading;

import ApplicationLayer.LocalServices.WirelessService.ZigBeeLayer.XbeeSender;

import java.util.List;

public class DelayControl implements Runnable{
    private XbeeSender xbeeSender;
    private List<SensorsReader> sr_list;

    public DelayControl (XbeeSender service, List<SensorsReader> sr){
        this.xbeeSender = service;
        this.sr_list = sr;
    }

    // Adjust delay of SR's readings to stop the growing of services Queues
    @Override
    public void run() {
        int READ_PERIOD = 100*1; // 1x delay

        int queueSizeAnterior = 0;
        int queueSizeActual = 0;
        int threshhold = 0;
        int confirmaciones = 0;
        int estancado = 0;

        while (true) {
            try {
                Thread.sleep(100*3); // 3x Delay
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            queueSizeActual = xbeeSender.queueSize();

            if (queueSizeActual > queueSizeAnterior) {
                confirmaciones += 1;
                if (confirmaciones == 5) { // 5 confirmaciones seguidas
                    threshhold = READ_PERIOD; // "Este read_period no me sirve" "debe ser mayor"
                    READ_PERIOD = (int) ((double) READ_PERIOD * 1.5); // Sube exponencialmente para poder recorrer la cola
                    confirmaciones = 0;
                    xbeeSender.resetQueue();
                }
            } else {
                confirmaciones = 0; // basta 1 vez que baje la cola para reiniciar el contador
                if (READ_PERIOD - 1 > threshhold) { // "Si bajo, estoy sobre el límite inferior?"
                    READ_PERIOD -= 1; // Baja linealmente
                }
            }

            // Si se queda estancado, vaciar la Queue
            if (queueSizeActual > 0 && (queueSizeActual == queueSizeAnterior | queueSizeActual + 1 == queueSizeAnterior | queueSizeActual - 1 == queueSizeAnterior)){
                estancado = estancado + 1;
                //System.out.println("ESTANCADO: " + estancado);
                if(estancado == 10){
                    //System.out.println("ESTANCADO 10");
                    xbeeSender.resetQueue();
                    estancado = 0;
                }
            }

            System.out.println("New period: " + READ_PERIOD + " Last queue size: " + queueSizeActual);
            for (SensorsReader sr : sr_list) {
                sr.setReadDelay(READ_PERIOD);
            }
            queueSizeAnterior = queueSizeActual;
        }
    }
}
