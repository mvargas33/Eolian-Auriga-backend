package Test.Main;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;

import ApplicationLayer.AppComponents.AppComponent;
import ApplicationLayer.Channel.Channel;
import ApplicationLayer.Channel.I2C;
import Test.ChannelsExec.I2CChannelTest;

public class MainTest {

    @Test
    public void coreTest() {
        // verificar que un componente se actualiza bien (iteracion 1)
        // verificar que un componente se actualiza bien (iteracion n)
        // con un single app component?

        // verificar que los componentes se actualizan
        
        // Se podria crear un componente/servicio dummy, que solo sea para logear el estado del programa e ir
        // checkeando que el flujo sea el correcto
        // flujo --> Iniciando component --> Empezando a leer -> Avisarle a los servicios
        //                               PARALELO
        //                                                    -> Ejecutar los servicios

    }

    /**
     * For now a virtual channel tests means JUST testing the parsing methods of a given channel.
     * Therefore the general structure consists of initializing their components, parsing some messages from
     * a mock object (each channel/interface should have a mock) and then checking that the components were
     * updated correctly, the correctness of a component will have to be checed given the instructions of
     * its respective mock object (for now this part will be hardcoded into the implementation of the mock object).
     * @param ch
     */
    @Test
    public void channelTest(Channel ch) {
        // Init components
        // Init channel
        // Init Mock object
        // Parse messages from mock object.
        // Check results
    }

    /**
     * Example of a test for i2c channel (pseudo code for now)
     */
    @Test
    public void channelTestEjemplo() {
        /*
        List<AppComponent> = ArrayList<>(bms, bms_volt, bms_temp, kelly_der, kelly_izq)''

        */
    }
}
