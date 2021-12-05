package entity;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import utils.JsonUtil;

import java.util.*;

public class ConfigInfo {
    public static ConfigInfo instance;

    private SignInfo signInfo;
    private InitExecEnvInfo initExecEnvInfo;
    private List<CustomCommandInfo> customCommandInfoList;
    private SendFileCommand sendFileCommand;
    private RebootCommand rebootCommand;
    private InstallCommand installCommand;
    private LogCommand logCommand;

    private JSONObject jsonObject;

    public static ConfigInfo getInstance() {
        if (instance == null) {
            instance = new ConfigInfo();
            instance.initConfig();
        }
        return instance;
    }

    private ConfigInfo() {
    }

    private void initConfig() {
        jsonObject = JsonUtil.readJsonFile("config.json");
        if (jsonObject == null) {
            return;
        }
        initSignInfo();
        initExecEnv();
        initCustomCommand();
        initSendFileInfo();
        initRebootCommand();
        initInstallCommand();
        initLogCommand();
    }

    private void initLogCommand() {
        JSONObject signObj = jsonObject.getJSONObject("log");
        if (signObj == null) {
            return;
        }
        String adbCommand = signObj.getString("adb");
        String hdcCommand = signObj.getString("hdc");
        logCommand = new LogCommand(adbCommand, hdcCommand);
    }

    private void initInstallCommand() {
        JSONObject signObj = jsonObject.getJSONObject("install");
        if (signObj == null) {
            return;
        }
        String adbCommand = signObj.getString("adb");
        String hdcCommand = signObj.getString("hdc");
        installCommand = new InstallCommand(adbCommand, hdcCommand);
    }

    private void initRebootCommand() {
        JSONObject signObj = jsonObject.getJSONObject("reboot");
        if (signObj == null) {
            return;
        }
        String adbCommand = signObj.getString("adb");
        String hdcCommand = signObj.getString("hdc");
        rebootCommand = new RebootCommand(adbCommand, hdcCommand);
    }

    private void initSendFileInfo() {
        JSONObject signObj = jsonObject.getJSONObject("sendFile");
        if (signObj == null) {
            return;
        }
        String adbCommand = signObj.getString("adb");
        String hdcCommand = signObj.getString("hdc");
        sendFileCommand = new SendFileCommand(adbCommand, hdcCommand);
    }

    private void initCustomCommand() {
        JSONObject customCommand = jsonObject.getJSONObject("customCommand");
        if (customCommand == null) {
            return;
        }
        customCommandInfoList = new ArrayList<>();
        JSONArray commands = customCommand.getJSONArray("commands");
        if (commands != null) {
            for (int idx = 0; idx < commands.size(); idx++) {
                JSONObject commandInfo = commands.getJSONObject(idx);
                String command_str = commandInfo.getString("command_str");
                String command_name = commandInfo.getString("command_name");
                customCommandInfoList.add(new CustomCommandInfo(command_str, command_name));
            }
        }
    }

    private void initExecEnv() {
        JSONObject initExecEnvObj = jsonObject.getJSONObject("initExecEnv");
        if (initExecEnvObj == null) {
            return;
        }
        initExecEnvInfo = new InitExecEnvInfo();
        JSONArray adbInitExecEnvCommands = initExecEnvObj.getJSONArray("adb");
        if (adbInitExecEnvCommands != null) {
            for (int idx = 0; idx < adbInitExecEnvCommands.size(); idx++) {
                String command = adbInitExecEnvCommands.getString(idx);
                initExecEnvInfo.addAdbCommand(command);
            }
        }

        JSONArray hdcInitExecEnvCommands = initExecEnvObj.getJSONArray("hdc");
        if (hdcInitExecEnvCommands != null) {
            for (int idx = 0; idx < hdcInitExecEnvCommands.size(); idx++) {
                String command = hdcInitExecEnvCommands.getString(idx);
                initExecEnvInfo.addHdcCommand(command);
            }
        }
    }

    private void initSignInfo() {
        JSONObject signObj = jsonObject.getJSONObject("sign");
        if (signObj == null) {
            return;
        }
        String jarPath = signObj.getString("jarPath");
        String username = signObj.getString("username");
        String password = signObj.getString("password");
        signInfo = new SignInfo(username, password, jarPath);
    }

    public String getSignJarPath() {
        return signInfo != null ? signInfo.getJarPath() : null;
    }

    public String getSignUserName() {
        return signInfo != null ? signInfo.getUserName() : null;
    }

    public String getSignPasswd() {
        return signInfo != null ? signInfo.getPassword() : null;
    }

    public String getAdbFileSendCommand() {
        return sendFileCommand != null ? sendFileCommand.getAdbCommand() : null;
    }

    public String getHdcFileSendCommand() {
        return sendFileCommand != null ? sendFileCommand.getHdcCommand() : null;
    }

    public String getAdbRebootCommand() {
        return rebootCommand != null ? rebootCommand.getAdbCommand() : null;
    }

    public String getHdcRebootCommand() {
        return rebootCommand != null ? rebootCommand.getHdcCommand() : null;
    }

    public boolean checkSignInfo() {
        return getSignJarPath() != null && getSignUserName() != null && getSignPasswd() != null;
    }

    public List<String> getAdbInitExecInfo() {
        return initExecEnvInfo != null ? initExecEnvInfo.getAdbInitEnvCommands() : null;
    }

    public List<String> getHdcInitExecInfo() {
        return initExecEnvInfo != null ? initExecEnvInfo.getHdcInitEnvCommands() : null;
    }

    public String getAdbInstallCommand() {
        return installCommand != null ? installCommand.getAdbCommand() : null;
    }

    public String getHdcInstallCommand() {
        return installCommand != null ? installCommand.getHdcCommand() : null;
    }

    public String getAdbLogCommand() {
        return logCommand != null ? logCommand.getAdbCommand() : null;
    }

    public String getHdcLogCommand() {
        return logCommand != null ? logCommand.getHdcCommand() : null;
    }

    public List<CustomCommandInfo> getCustomCommandInfoList() {
        return this.customCommandInfoList;
    }

    public void freshConfigInfo() {
        initConfig();
    }

    public boolean checkInitExecInfo() {
        return initExecEnvInfo != null;
    }

    private class SignInfo {
        private String userName;
        private String password;
        private String jarPath;

        public SignInfo(String userName, String password, String jarPath) {
            this.userName = userName;
            this.password = password;
            this.jarPath = jarPath;
        }

        public String getUserName() {
            return userName;
        }

        public void setUserName(String userName) {
            this.userName = userName;
        }

        public String getPassword() {
            return password;
        }

        public void setPassword(String password) {
            this.password = password;
        }

        public String getJarPath() {
            return jarPath;
        }

        public void setJarPath(String jarPath) {
            this.jarPath = jarPath;
        }
    }

    private class InitExecEnvInfo {
        private List<String> adbInitEnvCommands;
        private List<String> hdcInitEnvCommands;

        public InitExecEnvInfo() {
        }

        public void addAdbCommand(String command) {
            if (adbInitEnvCommands == null) {
                adbInitEnvCommands = new ArrayList<>();
            }
            adbInitEnvCommands.add(command);
        }

        public void addHdcCommand(String command) {
            if (hdcInitEnvCommands == null) {
                hdcInitEnvCommands = new ArrayList<>();
            }
            hdcInitEnvCommands.add(command);
        }

        public List<String> getAdbInitEnvCommands() {
            return adbInitEnvCommands;
        }

        public List<String> getHdcInitEnvCommands() {
            return hdcInitEnvCommands;
        }
    }

    public class CustomCommandInfo {
        private String commandStr;
        private String commandName;

        public CustomCommandInfo(String commandStr, String commandName) {
            this.commandStr = commandStr;
            this.commandName = commandName;
        }

        public String getCommandStr() {
            return commandStr;
        }

        public void setCommandStr(String commandStr) {
            this.commandStr = commandStr;
        }

        public String getCommandName() {
            return commandName;
        }

        public void setCommandName(String commandName) {
            this.commandName = commandName;
        }
    }

    private class SendFileCommand {
        private String adbCommand;
        private String hdcCommand;

        public SendFileCommand(String adbCommand, String hdcCommand) {
            this.adbCommand = adbCommand;
            this.hdcCommand = hdcCommand;
        }

        public String getAdbCommand() {
            return adbCommand;
        }

        public void setAdbCommand(String adbCommand) {
            this.adbCommand = adbCommand;
        }

        public String getHdcCommand() {
            return hdcCommand;
        }

        public void setHdcCommand(String hdcCommand) {
            this.hdcCommand = hdcCommand;
        }
    }

    private class RebootCommand {
        private String adbCommand;
        private String hdcCommand;

        public RebootCommand(String adbCommand, String hdcCommand) {
            this.adbCommand = adbCommand;
            this.hdcCommand = hdcCommand;
        }

        public String getAdbCommand() {
            return adbCommand;
        }

        public void setAdbCommand(String adbCommand) {
            this.adbCommand = adbCommand;
        }

        public String getHdcCommand() {
            return hdcCommand;
        }

        public void setHdcCommand(String hdcCommand) {
            this.hdcCommand = hdcCommand;
        }
    }

    private class InstallCommand {
        private String adbCommand;
        private String hdcCommand;

        public InstallCommand(String adbCommand, String hdcCommand) {
            this.adbCommand = adbCommand;
            this.hdcCommand = hdcCommand;
        }

        public String getAdbCommand() {
            return adbCommand;
        }

        public void setAdbCommand(String adbCommand) {
            this.adbCommand = adbCommand;
        }

        public String getHdcCommand() {
            return hdcCommand;
        }

        public void setHdcCommand(String hdcCommand) {
            this.hdcCommand = hdcCommand;
        }
    }

    private class LogCommand {
        private String adbCommand;
        private String hdcCommand;

        public LogCommand(String adbCommand, String hdcCommand) {
            this.adbCommand = adbCommand;
            this.hdcCommand = hdcCommand;
        }

        public String getAdbCommand() {
            return adbCommand;
        }

        public void setAdbCommand(String adbCommand) {
            this.adbCommand = adbCommand;
        }

        public String getHdcCommand() {
            return hdcCommand;
        }

        public void setHdcCommand(String hdcCommand) {
            this.hdcCommand = hdcCommand;
        }
    }
}
