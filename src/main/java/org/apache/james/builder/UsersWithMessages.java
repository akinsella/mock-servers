package org.apache.james.builder;

import com.google.common.collect.*;
import org.apache.james.InMemoryUser;
import org.apache.james.mailbox.model.MailboxPath;

import javax.mail.internet.MimeMessage;
import java.util.Arrays;
import java.util.Collections;
import java.util.Set;

import static java.util.Arrays.asList;

public class UsersWithMessages {

    private Set<UserWithMessages> userWithMessages = Sets.newHashSet();

    private UsersWithMessages() {
    }

    public Set<UserWithMessages> getUsersWithMessages() {
        return Collections.unmodifiableSet(userWithMessages);
    }

    public static class Builder {

        private UsersWithMessages usersWithMessages = new UsersWithMessages();

        public Builder() {
        }

        public Builder withUser(UserWithMessages.Builder builder) {
            usersWithMessages.userWithMessages.add(builder.build());
            return this;
        }

        public Builder withUser(UserWithMessages user) {
            usersWithMessages.userWithMessages.add(user);
            return this;
        }

        public Builder withUsers(UserWithMessages.Builder... builders) {
            for (UserWithMessages.Builder builder : builders) {
                usersWithMessages.userWithMessages.add(builder.build());
            }
            return this;
        }

        public Builder withUsers(UserWithMessages... users) {
            usersWithMessages.userWithMessages.addAll(asList(users));

            return this;
        }

        public UsersWithMessages build() {
            return usersWithMessages;
        }

    }

    public static Builder newBuilder() {
        return new Builder();
    }

}
