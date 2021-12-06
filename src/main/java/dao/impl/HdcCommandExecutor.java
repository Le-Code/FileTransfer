package dao.impl;

import dao.CommandExecutor;
import listener.RuntimeExecListener;

import java.util.ArrayList;
import java.util.List;

public class HdcCommandExecutor extends CommandExecutor {
    @Override
    public void installHap(List<String> hapPaths, String dstPath, boolean sign, RuntimeExecListener listener) {
        if (worker == null) {
            listener.onFailure("application error");
            return;
        }
        worker.pushWork(new Runnable() {
            @Override
            public void run() {
                List<String> execPaths = new ArrayList<>(hapPaths);
                String installCommand = configInfo.getHdcInstallCommand();
                if (installCommand == null) {
                    callOnFailure(listener, "hdc install command not config");
                    return;
                }
                for (String hapPath : execPaths) {
                    String command = installCommand + " " + hapPath;
                    executeSyncString(command, listener);
                }
            }
        });
    }

    @Override
    public void sendFile(List<String> srcPaths, String dstPath, boolean reboot, RuntimeExecListener listener) {
        if (worker == null) {
            callOnFailure(listener, "application error");
            return;
        }
        worker.pushWork(new Runnable() {
            @Override
            public void run() {
                String pushCommand = configInfo.getHdcFileSendCommand();
                if (pushCommand == null) {
                    callOnFailure(listener, "hdc file send command not config");
                    return;
                }
                for (String srcPath : srcPaths) {
                    String command = pushCommand + " " + srcPath + " " + dstPath;
                    executeSyncString(command, listener);
                }
                if (reboot) {
                    String rebootCommand = configInfo.getHdcRebootCommand();
                    if (rebootCommand == null) {
                        callOnFailure(listener, "adb reboot command not config");
                        return;
                    }
                    executeSyncString(rebootCommand, null);
                }
            }
        });
    }

    @Override
    public void log(String filter, RuntimeExecListener listener) {
        String command = configInfo.getHdcLogCommand();
        if (command == null) {
            callOnFailure(listener, "hdc command is not config");
            return;
        }
        Runnable logRunnable = new LogRunnable(command, filter, listener);
        logThread = new Thread(logRunnable);
        logThread.start();
    }
}
