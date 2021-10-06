package utils;

import java.io.IOException;

public class CommandHelp {
    public static void Exec(String command) {
        try {
            Process process = Runtime.getRuntime().exec(command);
            process.waitFor();
            process.destroy();
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }
}
