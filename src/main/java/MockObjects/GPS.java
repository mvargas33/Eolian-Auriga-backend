package MockObjects;

/** Simulates a GPS that returns some messages using NMEA format.
 */
public class GPS {

    /**
     * Static message is implemented, ideally it would be a random generated one.
     *  Usage example:
     *  <pre>{@code
     *      GPS MockGPS = new GPS();
     *      String RMCmsg = MockGPS.getRMCMsg();
     *      System.out.println(RMCmsg); // Use the messages freely.
     *  }</pre>
     * @return RMC message.
     */
    public String getRMCMsg() {
        return "$GPRMC,215829.000,A,3526.9451,S,07140.3300,W,0.32,349.15,030221,,,A*64";
    }

    /**
     * Static message is implemented, ideally it would be a random generated one.
     *  Usage example:
     *  <pre>{@code
     *      GPS MockGPS = new GPS();
     *      String GGAmsg = MockGPS.getGGAMsg();
     *      System.out.println(GGAmsg); // Use the messages freely.
     *  }</pre>
     * @return GGA message
     */
    public String getGGAMsg() {
        return "$GPGGA,215830.000,3526.9450,S,07140.3300,W,1,05,2.35,92.5,M,25.5,M,,*5B";
    }
}
