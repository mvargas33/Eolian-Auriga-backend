package ApplicationLayer.Utils;

import org.junit.jupiter.api.Test;
import ApplicationLayer.Utils.Utils;
import static org.junit.jupiter.api.Assertions.*;

class UtilsTest {

    @Test
    public void testSplit() {
        String s = " hOLA    {234}    2";
        String[] result = Utils.split(s, " ");
        int i = 0;
        for (String a: result
             ) {
            System.out.print(i);
            i++;
            System.out.println(a);
        }
    }

    @Test
    public void testSplitTradicional() {
        String s = " hOLA    {234}    2";
        String[] result = s.split(" ");
        int i = 0;
        for (String a: result
        ) {
            System.out.print(i);
            i++;
            System.out.println(a);
        }
    }

    @Test
    public void testSplitRegex() {
        String s = " hOLA    {234}    2";
        String[] result = s.split("\\s+");
        int i = 0;
        for (String a: result
        ) {
            System.out.print(i);
            i++;
            System.out.println(a);
        }
    }
}