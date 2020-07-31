package io.github.shallowinggg.sqlgen.config;

import io.github.shallowinggg.sqlgen.util.StringUtils;

import java.util.Objects;

/**
 * @author ding shimin
 */
public class DbConfig {

    private String url;

    private String driverName;

    private String username;

    private String password;

    public DbConfig(String url, String driverName, String username, String password) {
        this.url = url;
        this.driverName = driverName;
        this.username = username;
        this.password = password;
    }

    public boolean isValid() {
        return StringUtils.hasText(url) && StringUtils.hasText(driverName) &&
                StringUtils.hasText(username) && password != null;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getDriverName() {
        return driverName;
    }

    public void setDriverName(String driverName) {
        this.driverName = driverName;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        DbConfig dbConfig = (DbConfig) o;
        return Objects.equals(url, dbConfig.url) &&
                Objects.equals(driverName, dbConfig.driverName) &&
                Objects.equals(username, dbConfig.username) &&
                Objects.equals(password, dbConfig.password);
    }

    @Override
    public int hashCode() {
        return Objects.hash(url, driverName, username, password);
    }

    @Override
    public String toString() {
        return "DbConfig{" +
                "url='" + url + '\'' +
                ", driverName='" + driverName + '\'' +
                ", username='" + username + '\'' +
                ", password='" + password + '\'' +
                '}';
    }
}
