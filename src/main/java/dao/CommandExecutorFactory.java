package dao;

import dao.impl.AdbCommandExecutor;
import dao.impl.HdcCommandExecutor;

public class CommandExecutorFactory {

    public enum Mode {
        ADB, HDC
    }

    private static CommandExecutor adbCommandExecutor;
    private static CommandExecutor hdcCommandExecutor;

    private CommandExecutorFactory() {}

    public static CommandExecutor chooseExecMode(Mode mode) {
        switch (mode) {
            case ADB:
                if (adbCommandExecutor == null) {
                    adbCommandExecutor = new AdbCommandExecutor();
                }
                return adbCommandExecutor;
            case HDC:
                if (hdcCommandExecutor == null) {
                    hdcCommandExecutor = new HdcCommandExecutor();
                }
                return hdcCommandExecutor;
            default:
                return null;
        }
    }
}
