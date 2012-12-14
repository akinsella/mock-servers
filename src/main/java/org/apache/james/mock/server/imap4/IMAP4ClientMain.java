package org.apache.james.mock.server.imap4;

import com.google.common.collect.Ranges;
import org.apache.james.builder.MimeMessageBuilder;
import org.apache.james.builder.UserWithMessages;
import org.apache.james.builder.UsersWithMessages;
import org.apache.james.mock.server.pop3.POP3ServerRunner;

import java.util.Date;
import java.util.List;

import static com.google.common.collect.DiscreteDomains.integers;
import static com.google.common.collect.Lists.newArrayList;


/**
 * http://blog.codejava.net/nam/receive-e-mails-via-pop3-using-javamail/
 */
public abstract class IMAP4ClientMain {

    public static void main(String[] args) {

        IMAP4ServerRunner imap4ServerRunner = null;
        try {
            List<MimeMessageBuilder> mimeMessages = newArrayList();

            for (int id : Ranges.closed(1, 100).asSet(integers())) {
                mimeMessages.add(MimeMessageBuilder.newBuilder()
                        .withFrom("no-reply@example.org")
                        .withSubject("Some Subject " + id)
                        .withText("Some Text " + id)
                        .withSentDate(new Date()));
            }

            UsersWithMessages usersWithMessages = UsersWithMessages.newBuilder()
                    .withUser(
                            UserWithMessages.newBuilder("jdoe", "Password123")
                                    .withMessages(mimeMessages)
                    )
                    .build();

            imap4ServerRunner = IMAP4ServerRunner.createInstanceAndStart(usersWithMessages);

            Thread.sleep(60 * 60 * 1000);
        }
        catch(Exception e) {
            System.out.println("Message: " + e.getMessage());
            e.printStackTrace();
        }
        finally {
            if (imap4ServerRunner != null) {
                imap4ServerRunner.stopAndDestroy();
            }
        }

    }

}
