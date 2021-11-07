package dao.impl;

import dao.CommandExecutor;
import listener.RuntimeExecListener;

import java.util.ArrayList;
import java.util.List;

public class AdbCommandExecutor extends CommandExecutor {
    @Override
    public void installHap(List<String> hapPaths, String dstPath, boolean sign, RuntimeExecListener listener) {
        worker.pushWork(new Runnable() {
            @Override
            public void run() {
                List<String> execPaths = new ArrayList<>(hapPaths);
                if (sign) {
                    execPaths = signHap(hapPaths, listener);
                    if (execPaths == null) {
                        listener.onFailure("signed error");
                        return;
                    }
                }
                for (String hapPath : execPaths) {
                    String command = "adb install " + hapPath;
                    executeSyncString(command, listener);
                }
            }
        });
    }

    @Override
    public void sendFile(List<String> srcPaths, String dstPath, boolean reboot, RuntimeExecListener listener) {
        worker.pushWork(new Runnable() {
            @Override
            public void run() {
                for (String srcPath : srcPaths) {
                    String command = "adb push " + srcPath + " " + dstPath;
                    executeSyncString(command, listener);
                }
                if (reboot) {
                    executeSyncString("adb reboot", null);
                }
            }
        });
    }
}
