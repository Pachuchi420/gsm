package tools;

import java.io.PrintStream;

public class printTools {
    private static final PrintStream originalOut = System.out;

    public static void disablePrints() {
        System.setOut(new PrintStream(new java.io.OutputStream() {
            public void write(int b) {
                // Do nothing, effectively silencing prints
            }
        }));
    }

    public static void enablePrints() {
        System.setOut(originalOut);
    }
}

