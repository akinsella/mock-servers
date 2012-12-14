package org.apache.james;

import org.apache.james.user.api.model.User;

public class InMemoryUser implements User {

    private String userName;
    private String password;

    public InMemoryUser(String userName, String password) {
        this.userName = userName;
        this.password = password;
    }

    @Override
    public String getUserName() {
        return userName;
    }

    public String getPassword() {
        return password;
    }

    @Override
    public boolean verifyPassword(String password) {
        return this.password.equals(password);
    }

    @Override
    public boolean setPassword(String newPassword) {
        this.password = newPassword;
        return true;
    }

}