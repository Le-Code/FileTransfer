package dao;

import entity.ConfigInfo;
import entity.InputStreamEvent;
import listener.RuntimeExecListener;
import utils.CloseUtil;
import utils.FileUtil;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public abstract class CommandExecutor {

    protected ExecWorker worker;
    protected ConfigInfo configInfo;
    protected Thread logThread;

    public CommandExecutor() {
        configInfo = ConfigInfo.getInstance();
    }

    public void setWorker(ExecWorker worker) {
        this.worker = worker;
    }

    public void terminateCommand(RuntimeExecListener listener) {
        try {
            String[] command = {"cmd", "ctrl", "c"};
            Runtime.getRuntime().exec(command);
        } catch (IOException e) {
            e.printStackTrace();
            listener.onFailure(e.getMessage());
        }
    }

    protected void callOnSuccess(RuntimeExecListener listener, String txt) {
        if (listener != null) {
            listener.onSuccess(txt);
        }
    }

    protected void callOnFailure(RuntimeExecListener listener, String txt) {
        if (listener != null) {
            listener.onFailure(txt);
        }
    }

    public void executeSyncString(String command, RuntimeExecListener listener) {
        Process process = null;
        BufferedReader br = null;
        if (command == null || command.isEmpty()) {
            callOnFailure(listener, "command is empty");
            return;
        }
        try {
            callOnSuccess(listener, "start exec: " + command);
            String[] commandArr = new String[] {"cmd", "/C", command};
            process = Runtime.getRuntime().exec(commandArr);

            // handle ErrorStream
            worker.pushInputStreamEvent(
                    new InputStreamEvent(process.getErrorStream(), "ErrorStream", listener));

            br = new BufferedReader(new InputStreamReader(process.getInputStream(), "GBK"));
            process.getOutputStream().flush();
            String line = null;
            while ((line = br.readLine()) != null) {
                callOnSuccess(listener, line);
            }
            callOnSuccess(listener, "over exec: " + command);
        } catch (IOException e) {
            e.printStackTrace();
            callOnFailure(listener, e.getMessage());
        } finally {
            CloseUtil.close(br);
            if (process != null) {
                CloseUtil.close(process.getOutputStream());
            }
        }
    }

    private void executeSyncStringArr(String[] command, RuntimeExecListener listener) {
        Process process = null;
        BufferedReader br = null;
        if (command == null) {
            callOnFailure(listener, "command is empty");
            return;
        }
        try {
            process = Runtime.getRuntime().exec(command);

            // handle ErrorStream
            worker.pushInputStreamEvent(
                    new InputStreamEvent(process.getErrorStream(), "ErrorStream", listener));

            br = new BufferedReader(new InputStreamReader(process.getInputStream(), "GBK"));
            process.getOutputStream().flush();
            String line = null;
            while ((line = br.readLine()) != null) {
                callOnSuccess(listener, line);
            }
        } catch (IOException e) {
            e.printStackTrace();
            callOnFailure(listener, e.getMessage());
        } finally {
            CloseUtil.close(br);
            if (process != null) {
                CloseUtil.close(process.getOutputStream());
            }
        }
    }

    public void executeAsyncCommands(List<String> commands, RuntimeExecListener listener) {
        if (commands.isEmpty()) {
            listener.onFailure("application error");
            return;
        }
        worker.pushWork(new Runnable() {
            @Override
            public void run() {
                String[] commandArr = new String[commands.size() + 2];
                commandArr[0] = "cmd";
                commandArr[1] = "/C";
                for (int idx = 0; idx < commands.size(); idx++) {
                    commandArr[idx + 2] = commands.get(idx);
                }
                executeSyncStringArr(commandArr, listener);
            }
        });
    }

    public void executeAsyncString(String command, RuntimeExecListener listener) {
        if (worker == null) {
            listener.onFailure("application error");
            return;
        }
        worker.pushWork(new Runnable() {
            @Override
            public void run() {
                executeSyncString(command, listener);
            }
        });
    }

    public void executeFile(File file, RuntimeExecListener listener) {
        if (!file.exists()) {
            listener.onFailure("file not exist");
            return;
        }
        worker.pushWork(new Runnable() {
            @Override
            public void run() {
                try {
                    String filePath = file.getAbsolutePath();
                    String[] command = {"cmd.exe","/C", "Start", filePath};
                    Runtime.getRuntime().exec(command);
                } catch (IOException e) {
                    e.printStackTrace();
                    callOnFailure(listener, e.getMessage());
                }
            }
        });
    }

    public void stopLog() {
        if (logThread != null) {
            logThread.interrupt();
        }
    }

    public abstract void installHap(List<String> hapPaths, String dstPath, boolean sign, RuntimeExecListener listener);
    public abstract void sendFile(List<String> srcPaths, String dstPath, boolean reboot, RuntimeExecListener listener);
    public abstract void log(String filter, RuntimeExecListener listener);

    protected class LogRunnable implements Runnable {
        private String command;
        private String filter;
        private RuntimeExecListener listener;
        public LogRunnable(String command, String filter, RuntimeExecListener listener) {
            this.command = command;
            this.filter = filter;
            this.listener = listener;
        }

        @Override
        public void run() {
            Process process = null;
            BufferedReader br = null;
            String[] filters = filter.split("\\s+");
            try {
                String[] commandArr = new String[] {"cmd", "/C", command};
                process = Runtime.getRuntime().exec(commandArr);
                br = new BufferedReader(new InputStreamReader(process.getInputStream(), "GBK"));
                process.getOutputStream().flush();
                String line = null;
                boolean flag = true;
                while ((line = br.readLine()) != null && !Thread.currentThread().isInterrupted()) {
                    flag = true;
                    for (String f : filters) {
                        if (!line.contains(f)) {
                            flag = false;
                            break;
                        }
                    }
                    if (flag) {
                        callOnSuccess(listener, line);
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
                callOnFailure(listener, e.getMessage());
            } finally {
                CloseUtil.close(br);
                if (process != null) {
                    CloseUtil.close(process.getOutputStream());
                }
                callOnSuccess(listener, command + " over");
            }
        }
    }
}
