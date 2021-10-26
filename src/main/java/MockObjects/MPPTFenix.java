package MockObjects;

public class MPPTFenix {

    public double[][] mpptValues = new double[4][8]; // son 4 mppts, cada uno guarda 8 valores
    public byte[] data = {1, 2, 3, 4, 5, 6, 7, 8};

    public double[] msg(int mppt){
        double BVLR = (data[0] & 0b10000000) >> 7;
        double OVT  = (data[0] & 0b01000000) >> 6;
        double NOC  = (data[0] & 0b00100000) >> 5;
        double UNDV = (data[0] & 0b00010000) >> 4;
        double Uin  = ((int)((int)((data[0] & 0b00000011) << 8) | (int) (data[1] & 0x00FF)))*0.15049;
        double Iin  = ((int)((int)((data[2] & 0b00000011) << 8) | (int) (data[3] & 0x00FF)))*0.00872;
        double Uout = ((int)((int)((data[4] & 0b00000011) << 8) | (int) (data[5] & 0x00FF)))*0.20879;
        double temp = data[6];
        System.out.print("ID________________________________________________");System.out.println("0x77" + (mppt));
        System.out.print("BVLR (1: Uout = Umax, 0: Uout < Umax)_____________");System.out.println(BVLR);
        System.out.print("OVT  (1: T>95°C, 0:T<95°C)________________________");System.out.println(OVT);
        System.out.print("NOC  (1: Batt. desconectada, 0: Batt. conectada)__");System.out.println(NOC);
        System.out.print("UNDV (1: Uin <= 26V, 0: Uin > 26V_________________");System.out.println(UNDV);
        System.out.print("Uin  (Voltage IN)_________________________________");System.out.print(Uin);System.out.println("\t[V]");
        System.out.print("Iin  (Current IN)_________________________________");System.out.print(Iin);System.out.println("\t[A]");
        System.out.print("Potencia generada_________________________________");System.out.print(Uin*Iin);System.out.println("\t[W]");
        System.out.print("Uout (Voltage OUT)________________________________");System.out.print(Uout);System.out.println("[V]");
        System.out.print("Temperature_______________________________________");System.out.print(temp);System.out.println("\t[°C]");
        double[] values = {Uin, Iin, Uout, BVLR, OVT, NOC, UNDV, temp};
        mpptValues[mppt] = values;

        return values;
    }


    public void genData(int offset) {
        byte[] data = {1, 2, 3, 4, 5, 6, 7, 8};
        for(int i = 0; i < 8; i++) {
            data[i] += offset;
        }
        this.data = data;
    }

    public void genData(byte[] data) {
        this.data = data;
    }
}
