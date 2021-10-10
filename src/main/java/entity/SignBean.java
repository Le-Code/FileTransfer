package entity;

public class SignBean {
    private String userName;
    private String password;
    private String jarPath;

    public SignBean(String userName, String password, String jarPath) {
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
