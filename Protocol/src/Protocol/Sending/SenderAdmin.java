package Protocol.Sending;

import Protocol.Messages.Message;
import Utilities.BitOperations;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class SenderAdmin implements Runnable{
    private BlockingQueue<Message> messagesToSend;
    private XbeeSender myXbeeSender;

    public SenderAdmin(XbeeSender xbeeSender){
        this.messagesToSend = new LinkedBlockingQueue<>();
        this.myXbeeSender = xbeeSender; // Thread de xbeeSender debe ser creado antes que SenderAdmin
    }

    /**
     * Pone un nuevo Message en la cola de messages. Llamada comienza con lectura de nuevos valores:
     * updateDriectly() -> replaceMyValues() -> updateMsg() -> PutMessageInQueue()
     * CRC lo calcula SenderAdmin para mayor eficiencia (que updateDirectly() pueda retornar antes)
     * @param m : Message nuevo a poner en la Queue (Message actualizado)
     */
    public void putMessageInQueue(Message m){
        this.messagesToSend.add(m);
    }

    /**
     * Saca mensajes de la Queue de Mensajes, saca el byte[] del mensaje y le hace append de CRC.
     * Luego pone el byte[] con CRC en la Queue de envío del XbeeSender
     */
    public void putMessageInByteQueue(){
        Message m = this.messagesToSend.poll(); // Saco mensaje
        byte[] b = m.getBytes(); // Saco sus bytes
        //BitOperations.appendCRC(b); // Append de CRC
        myXbeeSender.putByteInQueue(b); // Lo pongo en la Queue de envío
    }

    /**
     * Retorna cuantos mensajes estan en cola
     * @return : Cantidad de mensajes en cola
     */
    public int messageQueueSize(){
        return messagesToSend.size();
    }

    /**
     * Retorna true si la queue de mensajes está vacía
     * @return : true si cola no tiene mensajes
     */
    public boolean isMessageQueueEmpty(){
        return messagesToSend.isEmpty();
    }

    /**
     * La única tarea de un Thread SenderAdmin es invocar putMessageInByteQueue() en cada instante.
     * Para sacar Message, calcular CRC y ponerlo en la cola de envío lo antes posible.
     */
    @Override
    public void run() {
        while(true){
            putMessageInByteQueue(); // Sacar Message, calcular CRC y ponerlo en cola en evío
        }
    }
}
