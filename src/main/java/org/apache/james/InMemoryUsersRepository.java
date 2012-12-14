package org.apache.james;

import org.apache.james.user.api.UsersRepository;
import org.apache.james.user.api.UsersRepositoryException;
import org.apache.james.user.api.model.User;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import static java.lang.String.format;
import static java.util.Collections.unmodifiableSet;

public class InMemoryUsersRepository implements UsersRepository {

    Map<String, User> users = new HashMap<String, User>();

    @Override
    public void addUser(String username, String password) throws UsersRepositoryException {
        users.put(username, new InMemoryUser(username, password));
    }

    @Override
    public User getUserByName(String userName) throws UsersRepositoryException {
        if (!users.containsKey(userName)) {
            throw new UsersRepositoryException(format("User: '%1$s' does not exists!", userName));
        }
        return users.get(userName);
    }

    @Override
    public void updateUser(User user) throws UsersRepositoryException {
        if (!users.containsKey(user.getUserName())) {
            throw new UsersRepositoryException(format("User: '%1$s' does not exists!", user.getUserName()));
        }
        users.put(user.getUserName(), user);
    }

    @Override
    public void removeUser(String userName) throws UsersRepositoryException {
        if (!users.containsKey(userName)) {
            throw new UsersRepositoryException(format("User: '%1$s' does not exists!", userName));
        }
        users.remove(userName);
    }

    @Override
    public boolean contains(String userName) throws UsersRepositoryException {
        return users.containsKey(userName);
    }

    @Override
    public boolean test(String userName, String password) throws UsersRepositoryException {
        return getUserByName(userName).verifyPassword(password);
    }

    @Override
    public int countUsers() throws UsersRepositoryException {
        return users.size();
    }

    @Override
    public Iterator<String> list() throws UsersRepositoryException {
        return unmodifiableSet(users.keySet()).iterator();
    }

    @Override
    public boolean supportVirtualHosting() throws UsersRepositoryException {
        return false;
    }

}
