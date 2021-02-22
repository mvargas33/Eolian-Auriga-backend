package Test.Benchmarks;
import ApplicationLayer.LocalServices.WirelessService.Utilities.BitOperations;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import java.util.concurrent.TimeUnit;

public class BenchmarkBitOperation {

    // Results: Intel i59300H       avgt 20.696 +- 0.951 ns/op
    // Results: Intel i59300H(2)    avgt 10.284 +- 0.249 ns/op
    // ponerValorEnArray se llama muchas veces en updateByteArrayFromValues
    // int -> byte[]
    @Benchmark
    @BenchmarkMode(Mode.AverageTime)
    @OutputTimeUnit(TimeUnit.NANOSECONDS)
    public void TestDePonerValorEnArray(){
        byte[] raw = new byte[]{0x00, 0x00};
        int val =  0xFF03; // 1 111 1111 0000 0011
        int bitSig = 15;
        int desdeBit = 1;

        BitOperations.ponerValorEnArray(raw, desdeBit, val, bitSig);
    }

    // Results: Intel i59300H       avgt 54.624 +- 2.081 ns/op
    // Results: Intel i59300H(2)    avgt 52.488 +- 1.873 ns/op
    // int[] -> byte[]
    @Benchmark
    @BenchmarkMode(Mode.AverageTime)
    @OutputTimeUnit(TimeUnit.NANOSECONDS)
    public void TestDeuUpdateByteArrayFromValues(){
        int[] source = {0b00011101, 0xFFFFFFFF, 0b01110000, 0x00FF};
        int[] bitSig = {5, 32, 7, 8};
        //int minBytes = (int) Math.ceil(Arrays.stream(bitSig).sum() / 8.0);
        byte[] rawBytes = {0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00}; // 7*8= 56 bits, tengo 52 bits
        int bitSigInicio = 0;
        int rawBytes_inicio = 0; // Del 0
        int rawBytes_fin = 51; // Al 51 hay 52 bits

        BitOperations.updateByteArrayFromValues(source, rawBytes, bitSig, bitSigInicio, rawBytes_inicio, rawBytes_fin);
    }

    // Results: Intel i59300H       avgt 10.656 +- 0.184 ns/op
    // Results: Intel i59300H(2)    avgt 10.742 +- 0.317 ns/op
    // extraerBits se llama muchas veces en updateValuesFromByteArray
    // byte[] -> int
    @Benchmark
    @BenchmarkMode(Mode.AverageTime)
    @OutputTimeUnit(TimeUnit.NANOSECONDS)
    public void TestDeExtraerBits() throws Exception{
        byte[] raw = {(byte) 0b00110011, (byte) 0b10101010, (byte) 0b00001111, (byte) 0b00100010}; // Array de ejemplo
        int res = BitOperations.extraerBits(raw, 10, 8); // desde segundo indice en byte[]
    }

    // Results: Intel i59300H       avgt 29.884 +- 0.456 ns/op
    // Results: Intel i59300H(2)    avgt 29.489 +- 0.074 ns/op
    // byte[] -> int[]
    @Benchmark
    @BenchmarkMode(Mode.AverageTime)
    @OutputTimeUnit(TimeUnit.NANOSECONDS)
    public void TestDeUpdateValuesFromByteArray(){
        int[] destino = {0,1,2,3};
        byte[] rawBytes = {(byte) 0b00110011, (byte) 0b10101010, (byte) 0b00001111, (byte) 0b00100010}; // Array de ejemplo
        int[] bitSig = {8, 8, 8, 8}; // Sacar ocho cada vez
        int bitSigInicio = 0;
        int rawBytes_inicio = 0;
        int rawBytes_fin = 8*4 - 1;

        BitOperations.updateValuesFromByteArray(destino, rawBytes, bitSig, bitSigInicio, rawBytes_inicio, rawBytes_fin);
    }

    // Results: Intel i59300H       avgt 7.022 +- 0.149 ns/op
    // Results: Intel i59300H(2)    avgt 6.432 +- 0.044 ns/op
    // Se usa harto antes de enviar cada mensaje, para hacer clean de bytes en mensaje antes de insertar valores
    @Benchmark
    @BenchmarkMode(Mode.AverageTime)
    @OutputTimeUnit(TimeUnit.NANOSECONDS)
    public void TestDeResetDeBits(){
        byte[] d = {0b00001000, 0b00001111};
        BitOperations.resetToZeroBitRange(d, 0, 15);
    }
}
