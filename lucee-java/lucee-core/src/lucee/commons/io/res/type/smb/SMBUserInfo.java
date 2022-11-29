package lucee.commons.io.res.type.smb;

public class SMBUserInfo {

    private String domain;
    private String username;
    private String password;

    public SMBUserInfo() {
        //default constructor
    }

    public SMBUserInfo(String domain, String username, String password) {
        this.domain = domain;
        this.username = username;
        this.password = password;
    }

    public String getDomain() {
        return this.domain;
    }

    public String getUsername() {
        return this.username;
    }

    public String getPassword() {
        return this.password;
    }
}
