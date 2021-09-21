package Test.Main;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;

import ApplicationLayer.AppComponents.AppComponent;
import ApplicationLayer.Channel.Channel;
import ApplicationLayer.Channel.I2C;
import Test.ChannelTests.I2CChannelTest;

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

    @Test
    public void channelTest(Channel ch) {
        List<AppComponent> ch_lac = new ArrayList<>();
        // crear los ac para el chanel (ie. bms, etc...)

        // usar los mocks para entregarles datos
        // pasarle los mocks a los channels
        // lanzar el read
        // confirmar las lecturas con algo como 
        //   assertEquals(bms.valores[i] == al que entrego el mock)
        //   ... para cada valor del componente ...

        
    }

    @Test
    public void channelTestEjemplo() {
        //List<AppComponent> lac = {"bms", "bms_volt", "bms_temp", "kelly_der", "kelly_izq", "mppt1", "mppt2", "mppt3", "mppt4"};
        //List<Service> ls = new ArrayList<>();
        //I2C ch = new I2C(lac, ls);
        //I2CMock mock = new I2CMock();
        
        //ch.setUp();
        //ch.readingLoop();
        //assertEquals(mock.getExpected(loop = 1), ch.bms.valoresRealesActuales[0]);
        //assertEquals(mock.getExpected(loop = 1), ch.bms.valoresRealesActuales[1]);
        //assertEquals(mock.getExpected(loop = 1), ch.bms.valoresRealesActuales[2]);
        //assertEquals(mock.getExpected(loop = 1), ch.bms.valoresRealesActuales[3]);
    }
}
