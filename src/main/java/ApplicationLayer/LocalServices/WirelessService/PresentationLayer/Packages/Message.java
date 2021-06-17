package ApplicationLayer.LocalServices.WirelessService.PresentationLayer.Packages;

import ApplicationLayer.LocalServices.WirelessService.PresentationLayer.Packages.State;
import ApplicationLayer.LocalServices.WirelessService.Utilities.BitOperations;

import java.util.LinkedList;

/*
 *  Piezas de bits que se intercambian entre las Xbees para hacer funcionar el protocolo.
 *  Los mensajes tienen un identificador y un largo definido.
 *  Adem&aacute;s le pertenecen a uno o m&aacute;s componentes, ellos saben que bits le corresponen del mensaje.
*/
public class Message {
    char header;
    int largoEnBytes;
    public byte[] bytes; // public to direct modify
    public LinkedList<State> myStates; // States Linked to this Message

    // Para ver que todos los componentes actualizaron el mensaje, después se enva
    int allComponentsUpdated; // Entero que cambia cada vez
    int numOfComponents; // Para generar valor inicial
    int initialValue; // Valor reset de allComponentsUpdated, al estilo 11111111.....11111100000, con 0's numOfComponents

    public Message(char header, int largoEnBytes){
        this.header = header;
        this.largoEnBytes = largoEnBytes;
        this.bytes = new byte[largoEnBytes];
        this.bytes[0] = (byte) header;          // Primer byte es el header
        //this.bytes[largoEnBytes - 1] = 0;       // Último byte es el CRC-8

        this.allComponentsUpdated = 0; // Flag de salida del mensaje, si quedan componentes que no han actualizado mensaje, este no sale
        this.numOfComponents = 0; // Para designar bits en allComponentsUpdated, este es el bit asignado también
        this.initialValue = 0;
        this.myStates = new LinkedList<>();
    }

    public void addState(State state){
        this.myStates.add(state);
        this.numOfComponents++;
        this.initialValue = ~ (((int) Math.pow(2,numOfComponents)) - 1); // Valor reset de allComponentsUpdated, al estilo 11111111.....11111100000, con 0's numOfComponents
        this.allComponentsUpdated = this.initialValue;
    }

    /* Sending */
    /**
     * Marca en allComponentsUpdated con el bit asignado, pasando de 111 ... 000 a 111 ... 010 si el bit asignado fue 1.
     * Cuando todos los componentes hayan invocado este metodo, entonces allComponentsUpdated valdra -1 : 111 ... 111
     * Se usa para que el mensaje sólo este 'listo' cuando todos los componentes invoquen este metodo
     * @param bitAsignado : Bit asignado del componente que invoca este método
     */
    public void marcarActualizacionDeComponente(int bitAsignado){
        int mask = 1 << bitAsignado;
        allComponentsUpdated |= mask; // Se hace OR con el bit señalado, que representa el componente bit-ésimo del mensaje
    }


    /**
     * Retorna true si todos los componentes han actualizado su parte en este mensaje.
     * Si falta un componente por actualizarse, entonces el mensaje no está listo para enviarse
     * Además resetea el valor de allComponentsUpdated al estar en true
     * @return true si todos los componentes actualziaron su bit en allComponentsUpdated, diciendo que estan listos
     */
    public boolean isReadyToSend(){
        if(allComponentsUpdated == -1){ // 111111111111 ... 1111111111111111)
            allComponentsUpdated = initialValue; // Reset
            return true;
        }else{
            return false;
        }
    }

    /*--------------------------------------------------- RESOURCES ---------------------------------------------------*/

    /**
     * Retorna el header del Message
     * @return : Header del message
     */
    public char getHeader() {
        return header;
    }

    /**
     * Retorna los bytes[] del mensaje
     * @return : bytes[] del mensaje
     */
    public byte[] getBytes() {
        return bytes;
    }

    /**
     * Retorna el largo en bytes del mensaje, se usa para calcuo de CRC
     * @return el largo en bytes del mensaje
     */
    public int getLargoEnBytes(){return largoEnBytes;}
    /**
     * Imprime el contenido del mensaje
     * @return : Print del Message
     */
    @Override
    public String toString(){
        StringBuilder sb = new StringBuilder();
        sb.append(this.header);sb.append(" | ");
        sb.append(BitOperations.ArraytoString(this.bytes));
        return sb.toString();
    }
}
