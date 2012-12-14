package org.helyx.pop3.mock.server;

import com.sun.mail.pop3.POP3Store;
import org.apache.james.builder.MimeMessageBuilder;
import org.apache.james.builder.UserWithMessages;
import org.apache.james.builder.UsersWithMessages;
import org.junit.*;

import javax.mail.*;
import javax.mail.internet.MimeMessage;
import java.util.Date;
import java.util.List;
import java.util.Properties;


/**
 * http://blog.codejava.net/nam/receive-e-mails-via-pop3-using-javamail/
 */
public abstract class Pop3ClientTest {

    Pop3ServerRunner pop3ServerRunner;

    @Before
    public void beforeClass() throws Exception {

        UsersWithMessages usersWithMessages = UsersWithMessages.newBuilder()
            .withUser(
                    UserWithMessages.newBuilder("jdoe", "Password123").withMessages(
                            MimeMessageBuilder.newBuilder()
                                    .withFrom("no-reply@example.org")
                                    .withSubject("Some Subject 1")
                                    .withText("Some Text 1")
                                    .withSentDate(new Date()),
                            MimeMessageBuilder.newBuilder()
                                    .withFrom("no-reply@example.org")
                                    .withSubject("Some Subject 2")
                                    .withText("Some Text 2")
                                    .withSentDate(new Date()),
                            MimeMessageBuilder.newBuilder()
                                    .withFrom("no-reply@example.org")
                                    .withSubject("Some Subject 3")
                                    .withText("Some Text 3")
                                    .withSentDate(new Date())
                    )
            )
            .build();

        pop3ServerRunner = Pop3ServerRunner.createInstanceAndStart(usersWithMessages);
    }

    @After
    public void afterClass() {
        if (pop3ServerRunner != null) {
            pop3ServerRunner.stopAndDestroy();
        }
    }

    @Test
    public void shouldReadMessages() throws Exception {
        receiveEmails("localhost", "9110", "jdoe", "Password123");
    }

    /**
     * Connects to a POP3 server and get new e-mails
     * @param host
     * @param port
     * @param user
     * @param pass
     * @throws Exception
     */
    public static void receiveEmails(String host, String port, String user, String pass) throws Exception {
        // set POP3 server settings
        Properties properties = new Properties();
        properties.put("mail.pop3.host", host);
        properties.put("mail.pop3.port", port);

        // connects to the server
        Session session = Session.getDefaultInstance(properties);
        POP3Store store = (POP3Store) session.getStore("pop3");
        store.connect(user, pass);

        // opens inbox
        Folder inbox = store.getFolder("INBOX");
        inbox.open(Folder.READ_ONLY);

        // retrieves messages
        Message[] messages = inbox.getMessages();

        // iterates over a list of messages
        for (int i = 0; i < messages.length; i++) {
            Message msg = messages[i];
            String from = msg.getFrom()[0].toString();
            String subject = msg.getSubject();
            String toList = parseAddresses(msg.getRecipients(Message.RecipientType.TO));
            String ccList = parseAddresses(msg.getRecipients(Message.RecipientType.CC));
            String sentDate = msg.getSentDate().toString();

            String contentType = msg.getContentType();
            String textMessage = "";

            if (contentType.contains("text/plain") || contentType.contains("text/html")) {
                textMessage = msg.getContent() != null ? msg.getContent().toString() : "";
            }

            // print out details of each e-mail
            System.out.println("Message #" + (i+1) + ":");
            System.out.println("\t From: " + from);
            System.out.println("\t To: " + toList);
            System.out.println("\t CC: " + ccList);
            System.out.println("\t Subject: " + subject);
            System.out.println("\t Sent Date: " + sentDate);
            System.out.println("\t Message: " + textMessage);
        }

        // disconnect from server
        inbox.close(false);
        store.close();
    }

    /**
     * Returns a list of addresses separated by comma
     * @param address an array of Address objects
     * @return a string represents a list of addresses
     */
    private static String parseAddresses(Address[] address) {
        String listAddress = "";

        if (address != null) {
            for (int i = 0; i < address.length; i++) {
                listAddress += address[i].toString() + ", ";
            }
        }
        if (listAddress.length() > 1) {
            listAddress = listAddress.substring(0, listAddress.length() - 2);
        }

        return listAddress;
    }

}
