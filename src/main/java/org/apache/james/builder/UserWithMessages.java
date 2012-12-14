package org.apache.james.builder;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ListMultimap;
import com.google.common.collect.Multimap;
import com.google.common.collect.Multimaps;
import org.apache.james.InMemoryUser;
import org.apache.james.builder.MimeMessageBuilder;
import org.apache.james.mailbox.model.MailboxPath;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import java.util.Arrays;
import java.util.List;

import static java.util.Arrays.asList;

public class UserWithMessages {

    private InMemoryUser user;

    private ListMultimap<MailboxPath, MimeMessage> messages = ArrayListMultimap.create();

    private UserWithMessages() {
    }

    public InMemoryUser getUser() {
        return user;
    }

    public Multimap<MailboxPath, MimeMessage> getMessages() {
        return Multimaps.unmodifiableListMultimap(messages);
    }

    public static class Builder {

        private UserWithMessages userWithMessages = new UserWithMessages();

        public Builder(String userName, String password) {
            userWithMessages.user = new InMemoryUser(userName, password);
        }

        public Builder withMessage(MimeMessageBuilder messageBuilder) throws MessagingException {
            return withMessage(messageBuilder.build());
        }

        public Builder withMessages(MimeMessageBuilder... messageBuilders) throws MessagingException {
           return withMessages(asList(messageBuilders));
        }

        public Builder withMessages(List<MimeMessageBuilder> messageBuilders) throws MessagingException {
            for (MimeMessageBuilder messageBuilder : messageBuilders) {
                withMessage(messageBuilder.build());
            }

            return this;
        }

        public Builder withMessage(MimeMessage message) {
            userWithMessages.messages.put(new MailboxPath("#private", userWithMessages.user.getUserName(), "INBOX"), message);
            return this;
        }

        public Builder withMessages(String folder, MimeMessageBuilder... messageBuilders) throws MessagingException {
            return withMessages(folder, asList(messageBuilders));
        }

        public Builder withMessages(String folder, List<MimeMessageBuilder> messageBuilders) throws MessagingException {
            for (MimeMessageBuilder messageBuilder : messageBuilders) {
                withMessage(folder, messageBuilder.build());
            }

            return this;
        }

        public Builder withMessage(String folder, MimeMessage message) {
            userWithMessages.messages.put(new MailboxPath("#private", userWithMessages.user.getUserName(), folder), message);
            return this;
        }

        public UserWithMessages build() {
            return userWithMessages;
        }

    }

    public static Builder newBuilder(String userName, String password) {
        return new Builder(userName, password);
    }

}
