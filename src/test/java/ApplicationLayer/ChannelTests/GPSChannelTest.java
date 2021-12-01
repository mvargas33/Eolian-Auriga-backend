package ApplicationLayer.ChannelTests;

import ApplicationLayer.AppComponents.AppComponent;
import ApplicationLayer.Channel.GPSChannel;
import MockObjects.GPS;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.ArrayList;


public class GPSChannelTest {

    private AppComponent appSender;
    private GPS gps;
    private GPSChannel gpsReader;

    @BeforeEach
    public void setUp() {
        gps = new GPS();
        appSender = new AppComponent("GPS",
                new double[] {0, 0, -1, 0, 0, -1}, // mins
                new double[] {60, 90, 1, 60, 180, 1}, //maxs
                new String[] {"latitude", "latitude_degree", "latitude_orientation", "longitude", "longitude_degree", "longitude_orientation"});
        ArrayList<AppComponent> lac = new ArrayList<>();
        lac.add(appSender);
        gpsReader = new GPSChannel(lac, new ArrayList<>());
    }

    @Test
    public void readRMC() {
        // mensaje leido $GPRMC,215829.000,A,3526.9451,S,07140.3300,W,0.32,349.15,030221,,,A*64"
        // --> latitud = -35 26.9451, longitud = -71 40.3300
        gpsReader.parseMessage(gps.getRMCMsg());
        // orden de parametros -> lat, lat_minutos, lat_ori, long, long_minutos, long_ori
        assertEquals(gpsReader.gps.valoresRealesActuales[0], 35);
        assertEquals(gpsReader.gps.valoresRealesActuales[1], 26.9451);
        assertEquals(gpsReader.gps.valoresRealesActuales[2], -1);
        assertEquals(gpsReader.gps.valoresRealesActuales[3], 71);
        assertEquals(gpsReader.gps.valoresRealesActuales[4], 40.3300);
        assertEquals(gpsReader.gps.valoresRealesActuales[5], -1);
    }

    @Test
    public void readGGA() {
        // mensaje leido "$GPGGA,215830.000,3526.9450,S,07140.3300,W,1,05,2.35,92.5,M,25.5,M,,*5B"
        // --> latitud = -35 26.9450, longitud = -71 40.3300
        gpsReader.parseMessage(gps.getGGAMsg());
        // orden de parametros -> lat, lat_minutos, lat_ori, long, long_minutos, long_ori
        assertEquals(gpsReader.gps.valoresRealesActuales[0], 35);
        assertEquals(gpsReader.gps.valoresRealesActuales[1], 26.9450);
        assertEquals(gpsReader.gps.valoresRealesActuales[2], -1);
        assertEquals(gpsReader.gps.valoresRealesActuales[3], 71);
        assertEquals(gpsReader.gps.valoresRealesActuales[4], 40.3300);
        assertEquals(gpsReader.gps.valoresRealesActuales[5], -1);
    }


    // Para los 2 siguientes tests se usara el siguiente mensaje:
    // "$GPGGA,215830.000,3526.9450,S,07140.3300,W,1,05,2.35,92.5,M,25.5,M,,*5B"
    // (cambiando el mensaje) donde el checksum 5B, esta correcto
    @Test
    public void checksumPass() {
        String msg = "$GPGGA,215830.000,3526.9450,S,07140.3300,W,1,05,2.35,92.5,M,25.5,M,,*5B";
        assertEquals(gpsReader.checkSum(msg), true);
    }


    @Test
    public void checksumFail() {
        // solo se cambio el 4to (indice 3) parametro, de una S a una N
        String msg = "$GPGGA,215830.000,3526.9450,N,07140.3300,W,1,05,2.35,92.5,M,25.5,M,,*5B";
        assertEquals(gpsReader.checkSum(msg), false);
    }
}
