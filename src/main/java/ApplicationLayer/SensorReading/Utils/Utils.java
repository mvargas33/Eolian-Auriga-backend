package ApplicationLayer.SensorReading.Utils;

public class Utils {

    public static String[] split(String s, String delimiter){
        if (s == null) {
            return null;
        }
        int delimiterLength;
        int stringLength = s.length();
        if (delimiter == null || (delimiterLength = delimiter.length()) == 0){
            return new String[] {s};
        }

        // a two pass solution is used because a one pass solution would
        // require the possible resizing and copying of memory structures
        // In the worst case it would have to be resized n times with each
        // resize having a O(n) copy leading to an O(n^2) algorithm.

        int count;
        int start;
        int end;

        // Scan s and count the tokens.
        count = 0;
        start = 0;
        while((end = s.indexOf(delimiter, start)) != -1){
            count++;
            start = end + delimiterLength;
        }
        count++;

        // allocate an array to return the tokens,
        // we now know how big it should be
        String[] result = new String[count];

        // Scan s again, but this time pick out the tokens
        count = 0;
        start = 0;
        while((end = s.indexOf(delimiter, start)) != -1){
            result[count] = (s.substring(start, end));
            count++;
            start = end + delimiterLength;
        }
        end = stringLength;
        result[count] = s.substring(start, end);

        return (result);
    }

    public byte[] hexStringToByteArray(String s) {
        byte[] data = new byte[s.length()/2];
        for (int i = 0; i < data.length; i ++) {
            data[i] = (byte) ((Character.digit(s.charAt(i*2), 16) << 4)
                    + Character.digit(s.charAt(i*2 + 1), 16));
        }
        return data;
    }
}
