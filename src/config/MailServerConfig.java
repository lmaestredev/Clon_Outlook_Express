package config;

public class MailServerConfig {

    private String host;
    private int port;
    private String username;
    private String password;
    private boolean auth;
    private boolean starttls;

    public MailServerConfig(String host, int port, String username, String password, boolean auth, boolean starttls) {
        this.host = host;
        this.port = port;
        this.username = username;
        this.password = password;
        this.auth = auth;
        this.starttls = starttls;
    }

    public String getHost() {
        return host;
    }

    public int getPort() {
        return port;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public boolean isAuth() {
        return auth;
    }

    public boolean isStarttls() {
        return starttls;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void setAuth(boolean auth) {
        this.auth = auth;
    }

    public void setStarttls(boolean starttls) {
        this.starttls = starttls;
    }
}
