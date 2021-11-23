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

    public boolean checkSignInfo() {
        return getSignJarPath() != null && getSignUserName() != null && getSignPasswd() != null;
    }

    public List<String> getAdbInitExecInfo() {
        return initExecEnvInfo != null ? initExecEnvInfo.getAdbInitEnvCommands() : null;
    }

    public List<String> getHdcInitExecInfo() {
        return initExecEnvInfo != null ? initExecEnvInfo.getHdcInitEnvCommands() : null;
    }

    public List<CustomCommandInfo> getCustomCommandInfoList() {
        return this.customCommandInfoList;
    }

    public void freshCustomCommands() {
        if (jsonObject == null || customCommandInfoList == null) {
            return;
        }
        jsonObject = JsonUtil.readJsonFile("config.json");
        JSONObject customCommand = jsonObject.getJSONObject("customCommand");
        if (customCommand == null) {
            return;
        }
        customCommandInfoList.clear();
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
}
