package org.apache.james.builder;

import com.google.common.collect.Lists;

import javax.activation.DataHandler;
import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import static com.google.common.collect.Maps.newHashMap;

public class MimeMessageBuilder {

    private MimeMessage mimeMessage;

    private List<Address> replyToList = Lists.newArrayList();
    private Map<MimeMessage.RecipientType, Address> recipients = newHashMap();

    private MimeMessageBuilder(Session session) {
        mimeMessage = new MimeMessage(session);
    }

    public MimeMessageBuilder withFrom(InternetAddress from) throws MessagingException {
        mimeMessage.setFrom(from);
        return this;
    }

    public MimeMessageBuilder withFrom(String from) throws MessagingException {
        return withFrom(new InternetAddress(from));
    }

    public MimeMessageBuilder withReplyTo(InternetAddress replyTo) throws MessagingException {
        replyToList.add(replyTo);
        return this;
    }

    public MimeMessageBuilder withReplyTo(String from) throws MessagingException {
        return withReplyTo(new InternetAddress(from));
    }

    public MimeMessageBuilder withSender(String sender) throws MessagingException {
        return withSender(new InternetAddress(sender));
    }

    public MimeMessageBuilder withSender(InternetAddress sender) throws MessagingException {
        mimeMessage.setSender(sender);
        return this;
    }

    public MimeMessageBuilder withContent(Multipart multipart) throws MessagingException {
        mimeMessage.setContent(multipart);
        return this;
    }

    public MimeMessageBuilder withContent(Object object, String type) throws MessagingException {
        mimeMessage.setContent(object, type);
        return this;
    }

    public MimeMessageBuilder withContentID(String cid) throws MessagingException {
        mimeMessage.setContentID(cid);
        return this;
    }

    public MimeMessageBuilder withContentLanguage(String[] langages) throws MessagingException {
        mimeMessage.setContentLanguage(langages);
        return this;
    }

    public MimeMessageBuilder withContentMD5(String contentMD5) throws MessagingException {
        mimeMessage.setContentMD5(contentMD5);
        return this;
    }

    public MimeMessageBuilder withDataHandler(DataHandler dataHandler) throws MessagingException {
        mimeMessage.setDataHandler(dataHandler);
        return this;
    }

    public MimeMessageBuilder withDescription(String description) throws MessagingException {
        mimeMessage.setDescription(description);
        return this;
    }

    public MimeMessageBuilder withDescription(String description, String charset) throws MessagingException {
        mimeMessage.setDescription(description, charset);
        return this;
    }

    public MimeMessageBuilder withDisposition(String disposition) throws MessagingException {
        mimeMessage.setDisposition(disposition);
        return this;
    }

    public MimeMessageBuilder withFileName(String filename) throws MessagingException {
        mimeMessage.setFileName(filename);
        return this;
    }

    public MimeMessageBuilder withHeader(String headerName, String headerValue) throws MessagingException {
        mimeMessage.setHeader(headerName, headerValue);
        return this;
    }

    public MimeMessageBuilder withSubject(String subject) throws MessagingException {
        mimeMessage.setSubject(subject);
        return this;
    }

    public MimeMessageBuilder withSubject(String subject, String charset) throws MessagingException {
        mimeMessage.setSubject(subject, charset);
        return this;
    }

    public MimeMessageBuilder withText(String text) throws MessagingException {
        mimeMessage.setText(text);
        return this;
    }

    public MimeMessageBuilder withText(String text, String charset) throws MessagingException {
        mimeMessage.setText(text, charset);
        return this;
    }

    public MimeMessageBuilder withText(String text, String charset, String subType) throws MessagingException {
        mimeMessage.setText(text, charset, subType);
        return this;
    }

    public MimeMessageBuilder withRecipient(MimeMessage.RecipientType recipientType, String recipient) throws MessagingException {
        return withRecipient(recipientType, new InternetAddress(recipient));
    }

    public MimeMessageBuilder withRecipient(MimeMessage.RecipientType recipientType, Address recipient) throws MessagingException {
        recipients.put(recipientType, recipient);
        return this;
    }

    public MimeMessageBuilder withFlags(Flags flags) throws MessagingException {
        mimeMessage.setFlags(flags, true);
        return this;
    }

    public MimeMessageBuilder withSentDate(Date date) throws MessagingException {
        mimeMessage.setSentDate(date);
        return this;
    }


    public MimeMessage build() throws MessagingException {
        mimeMessage.setReplyTo(replyToList.toArray(new Address[replyToList.size()]));
        return mimeMessage;
    }

    public static MimeMessageBuilder newBuilder() {
        return new MimeMessageBuilder(Session.getInstance(new Properties()));
    }

    public static MimeMessageBuilder newBuilder(Session session) {
        return new MimeMessageBuilder(session);
    }

}
