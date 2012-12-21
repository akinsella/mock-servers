package org.apache.james.mock.server.pop3;

import com.google.common.base.Joiner;
import org.apache.commons.configuration.HierarchicalConfiguration;
import org.apache.commons.io.IOUtils;
import org.apache.james.BasicFileSystem;
import org.apache.james.InMemoryProtocolHandlerLoader;
import org.apache.james.InMemoryUser;
import org.apache.james.InMemoryUsersRepository;
import org.apache.james.adapter.mailbox.store.UserRepositoryAuthenticator;
import org.apache.james.builder.UserWithMessages;
import org.apache.james.builder.UsersWithMessages;
import org.apache.james.filesystem.api.FileSystem;
import org.apache.james.mailbox.MailboxListener;
import org.apache.james.mailbox.MailboxManager;
import org.apache.james.mailbox.MailboxSession;
import org.apache.james.mailbox.MessageManager;
import org.apache.james.mailbox.acl.SimpleGroupMembershipResolver;
import org.apache.james.mailbox.acl.UnionMailboxACLResolver;
import org.apache.james.mailbox.exception.MailboxException;
import org.apache.james.mailbox.inmemory.InMemoryMailboxSessionMapperFactory;
import org.apache.james.mailbox.model.MailboxPath;
import org.apache.james.mailbox.model.MessageMetaData;
import org.apache.james.mailbox.model.UpdatedFlags;
import org.apache.james.mailbox.store.JVMMailboxPathLocker;
import org.apache.james.mailbox.store.RandomMailboxSessionIdGenerator;
import org.apache.james.mailbox.store.StoreMailboxManager;
import org.apache.james.mock.server.pop3.configuration.Pop3ServerXMLConfigurationBuilder;
import org.apache.james.pop3server.netty.POP3ServerFactory;
import org.apache.james.user.api.UsersRepositoryException;
import org.slf4j.Logger;

import javax.mail.Flags;
import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import static com.google.common.base.Preconditions.checkNotNull;
import static java.lang.String.format;
import static java.util.Arrays.asList;
import static org.slf4j.LoggerFactory.getLogger;

public class Pop3ServerRunner {

    private POP3ServerFactory pop3ServerFactory;

    public static Pop3ServerRunner createInstanceAndStart( UsersWithMessages usersWithMessages) throws Exception {
        return new Pop3ServerRunner(usersWithMessages);
    }

    public static Pop3ServerRunner createInstanceAndStart(int port, UsersWithMessages usersWithMessages) throws Exception {
        return new Pop3ServerRunner(port, usersWithMessages);
    }

    public static Pop3ServerRunner createInstanceAndStart(int port, UsersWithMessages usersWithMessages, Logger logger) throws Exception {
        return new Pop3ServerRunner(port, usersWithMessages, logger);
    }

    public static Pop3ServerRunner createInstanceAndStart(HierarchicalConfiguration configuration, UsersWithMessages usersWithMessages, Logger logger) throws Exception {
        return new Pop3ServerRunner(configuration, usersWithMessages, logger);
    }

    public Pop3ServerRunner(UsersWithMessages usersWithMessages) throws Exception {
        this(9110, usersWithMessages);
    }

    public Pop3ServerRunner(int port, UsersWithMessages usersWithMessages) throws Exception {
        this(Pop3ServerXMLConfigurationBuilder.createConfigurationWithPort(port), usersWithMessages, getLogger(Pop3ServerRunner.class));
    }

    public Pop3ServerRunner(int port, UsersWithMessages usersWithMessages, Logger logger) throws Exception {
        this(Pop3ServerXMLConfigurationBuilder.createConfigurationWithPort(port), usersWithMessages, logger);
    }

    public Pop3ServerRunner(HierarchicalConfiguration configuration, UsersWithMessages usersWithMessages) throws Exception {
        this(configuration, usersWithMessages, getLogger(Pop3ServerRunner.class));
    }

    public Pop3ServerRunner(HierarchicalConfiguration configuration, UsersWithMessages usersWithMessages, Logger logger) throws Exception {
        MailboxManager mailboxManager = creatMailBoxManager(usersWithMessages, logger);
        pop3ServerFactory = createPop3ServerFactory(mailboxManager, configuration, logger);

        checkNotNull(logger, "Logger is required");
    }

    private POP3ServerFactory createPop3ServerFactory(MailboxManager mailboxManager, HierarchicalConfiguration configuration, Logger logger) throws Exception {
        POP3ServerFactory pop3ServerFactory = new POP3ServerFactory();
        pop3ServerFactory.setFileSystem(createFileSystem());
        pop3ServerFactory.setProtocolHandlerLoader(createProtocolHanderLoader(mailboxManager));
        pop3ServerFactory.setLog(logger);
        pop3ServerFactory.configure(configuration);

        pop3ServerFactory.init();

        return pop3ServerFactory;
    }

    private InMemoryProtocolHandlerLoader createProtocolHanderLoader(MailboxManager mailboxManager) {
        return new InMemoryProtocolHandlerLoader(mailboxManager);
    }

    private FileSystem createFileSystem() {
        return new BasicFileSystem();
    }

    private void destroyPop3ServerFactory() {
        if (pop3ServerFactory.getServers() != null) {
            try {
                pop3ServerFactory.destroy();
            } catch (Exception e) {
                System.out.println("Exception: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    private MailboxManager creatMailBoxManager(UsersWithMessages usersWithMessages, Logger logger) throws Exception {

        StoreMailboxManager<Long> storeMailboxManager = createStoreMailboxManager(createMailboxSessionMappingFactory(), createUsersRepositoryAuthenticator(usersWithMessages, logger));

        storeMessages(storeMailboxManager, usersWithMessages, logger);

        return storeMailboxManager;
    }

    private InMemoryMailboxSessionMapperFactory createMailboxSessionMappingFactory() {
        return new InMemoryMailboxSessionMapperFactory();
    }

    private UserRepositoryAuthenticator createUsersRepositoryAuthenticator(UsersWithMessages usersWithMessages, Logger logger) throws UsersRepositoryException {
        UserRepositoryAuthenticator userRepositoryAuthenticator = new UserRepositoryAuthenticator();

        userRepositoryAuthenticator.setUsersRepository(createUsersRepository(usersWithMessages));
        userRepositoryAuthenticator.setLog(logger);

        return userRepositoryAuthenticator;
    }

    private InMemoryUsersRepository createUsersRepository(UsersWithMessages usersWithMessages) throws UsersRepositoryException {
        InMemoryUsersRepository usersRepository = new InMemoryUsersRepository();

        for (UserWithMessages userWithMessages : usersWithMessages.getUsersWithMessages()) {
            usersRepository.addUser(userWithMessages.getUser().getUserName(), userWithMessages.getUser().getPassword());
        }

        return usersRepository;
    }

    private StoreMailboxManager<Long> createStoreMailboxManager(InMemoryMailboxSessionMapperFactory inMemoryMailboxSessionMapperFactory, UserRepositoryAuthenticator userRepositoryAuthenticator) throws MailboxException {
        StoreMailboxManager<Long> storeMailboxManager = new StoreMailboxManager<Long>(
                inMemoryMailboxSessionMapperFactory,
                userRepositoryAuthenticator,
                new JVMMailboxPathLocker(),
                new UnionMailboxACLResolver(),
                new SimpleGroupMembershipResolver()
        );

        storeMailboxManager.setMailboxSessionIdGenerator(new RandomMailboxSessionIdGenerator());
        storeMailboxManager.init();
        return storeMailboxManager;
    }

    private void storeMessages(StoreMailboxManager<Long> storeMailboxManager, UsersWithMessages usersWithMessages, Logger logger) throws MailboxException, IOException, MessagingException {
        for (UserWithMessages userWithMessages : usersWithMessages.getUsersWithMessages()) {
            storeUserMessages(storeMailboxManager, userWithMessages, logger);
        }
    }

    private void storeUserMessages(StoreMailboxManager<Long> storeMailboxManager, UserWithMessages userWithMessages, final Logger logger) throws MailboxException, IOException, MessagingException {

        MailboxSession mailboxSession = storeMailboxManager.createSystemSession(userWithMessages.getUser().getUserName(), logger);
        storeMailboxManager.addGlobalListener(new MailboxListener() {
            @Override
            public void event(Event event) {
                if (event instanceof Added) {
                    Added addedEvent = (Added)event;
                    logger.info(format("Add Messages Event[Session: %1$s, Mailbox path: %2$s]", getSessionAsString(event.getSession()), event.getMailboxPath()));
                    for (Long uid : addedEvent.getUids()) {
                        MessageMetaData metaData = addedEvent.getMetaData(uid);
                        logger.info( format("\t - Message [uid: %1$s, date: %2$s, size: %3$s, mod seq: %4$s, flags: %5$s]",
                                metaData.getUid(), formatDateTime(metaData.getInternalDate()), metaData.getSize(), metaData.getModSeq(), getFlagsAsString(metaData.getFlags())) );
                    }
                }
                else if (event instanceof Expunged) {
                    Expunged expungedEvent = (Expunged)event;
                    logger.info(format("Expunged Messages Event[Session: %1$s, Mailbox path: %2$s]", getSessionAsString(event.getSession()), expungedEvent.getMailboxPath()));
                    for (Long uid : expungedEvent.getUids()) {
                        MessageMetaData metaData = expungedEvent.getMetaData(uid);
                        logger.info( format("\t - Message [uid: %1$s, date: %2$s, size: %3$s, mod seq: %4$s, flags: %5$s]",
                                metaData.getUid(), formatDateTime(metaData.getInternalDate()), metaData.getSize(), metaData.getModSeq(), getFlagsAsString(metaData.getFlags())) );
                    }
                }
                else if (event instanceof FlagsUpdated) {
                    FlagsUpdated flagsUpdatedEvent = (FlagsUpdated)event;
                    logger.info(format("Flags updated Messages Event[Session: %1$s, Mailbox path: %2$s]", getSessionAsString(event.getSession()), flagsUpdatedEvent.getMailboxPath()));
                    for (UpdatedFlags updatedFlags : flagsUpdatedEvent.getUpdatedFlags()) {
                        logger.info(format("\t - Message [uid: %1$s, mod seq: %2$s, old flags: %3$s, new flags: %4$s]",
                                updatedFlags.getUid(), updatedFlags.getModSeq(), getFlagsAsString(updatedFlags.getOldFlags()), getFlagsAsString(updatedFlags.getNewFlags())));
                    }
                }
                else if (event instanceof MailboxAdded) {
                    MailboxAdded mailboxAddedEvent = (MailboxAdded)event;
                    logger.info(format("Mailbox addded [Session: %1$s, Mailbox path: %2$s]", getSessionAsString(event.getSession()), mailboxAddedEvent.getMailboxPath()));
                }
                else if (event instanceof MailboxDeletion) {
                    MailboxDeletion mailboxDeletionEvent = (MailboxDeletion)event;
                    logger.info(format("Mailbox deleted [Session: %1$s, Mailbox path: %2$s]", getSessionAsString(event.getSession()), mailboxDeletionEvent.getMailboxPath()));
                }
                else if (event instanceof MailboxRenamed) {
                    MailboxRenamed mailboxRenamedEvent = (MailboxRenamed)event;
                    logger.info(format("Mailbox renamed [Session: %1$s, Mailbox path: %2$s, new path: %3$s]", getSessionAsString(event.getSession()), mailboxRenamedEvent.getMailboxPath(), mailboxRenamedEvent.getNewPath()));
                }
                else {
                    logger.info(format("Default Event[Session: %1$s, Mailbox path: %2$s]", getSessionAsString(event.getSession()), event.getMailboxPath()));
                }
            }
        }, mailboxSession);
        try {
            for (MailboxPath mailboxPath : userWithMessages.getMessages().keySet()) {
                MessageManager messageManager = getOrCreateMailbox(storeMailboxManager, userWithMessages.getUser(), mailboxSession, mailboxPath);
                for (MimeMessage message : userWithMessages.getMessages().get(mailboxPath)) {
                    storeMessage(messageManager, message, mailboxSession);
                }
            }
        }
        finally {
            if (mailboxSession != null) {
                mailboxSession.close();
            }
        }
    }

    private String getSessionAsString(MailboxSession session) {
        return format("[id: %1$s, user: '%2$s', type: %3$s]", session.getSessionId(), session.getUser().getUserName(), session.getType());
    }

    private String formatDateTime(Date date) {
        return new SimpleDateFormat("yyyy/MM/dd, HH:mm:ss").format(date);
    }

    private String getFlagsAsString(Flags flags) {
        return format("[user: %1$s, system: %2$s]", getUserFlagsAsString(flags.getUserFlags()), getSystemFlagsAsString(asList(flags.getSystemFlags())));
    }

    private String getUserFlagsAsString(String[] flags) {
        return "[" + Joiner.on(", ").join(flags) + "]";
    }

    private String getSystemFlagsAsString(List<Flags.Flag> flags) {

        StringBuilder sb = new StringBuilder("[");
        for ( int i = 0 ; i < flags.size() ; i++) {
            sb.append(getFlagName(flags.get(i)));
            if (i + 1 < flags.size()) {
                sb.append(", ");
            }
        }

        return sb.append("]").toString();
    }

    private String getFlagName(Flags.Flag flag) {
        if (flag == Flags.Flag.ANSWERED) {
            return "ANSWERED";
        }
        else if (flag == Flags.Flag.DELETED) {
            return "DELETED";
        }
        else if (flag == Flags.Flag.DRAFT) {
            return "DRAFT";
        }
        else if (flag == Flags.Flag.FLAGGED) {
            return "FLAGGED";
        }
        else if (flag == Flags.Flag.RECENT) {
            return "RECENT";
        }
        else if (flag == Flags.Flag.SEEN) {
            return "SEEN";
        }
        else if (flag == Flags.Flag.USER) {
            return "USER";
        }
        else {
            return "<UNKNOWN_FLAG>";
        }
    }

    private void storeMessage(MessageManager messageManager, MimeMessage message, MailboxSession mailboxSession) throws IOException, MessagingException, MailboxException {
        InputStream messageInputStream = toInputStream(message);

        try {
            messageManager.appendMessage(messageInputStream, new Date(), mailboxSession, true, new Flags());
        } finally {
            IOUtils.closeQuietly(messageInputStream);
        }
    }

    private MessageManager getOrCreateMailbox(StoreMailboxManager<Long> storeMailboxManager, InMemoryUser user, MailboxSession mailboxSession, MailboxPath mailboxPath) throws MailboxException {
        if (!storeMailboxManager.mailboxExists(mailboxPath, mailboxSession)) {
            storeMailboxManager.createMailbox(createDefaultMailboxPath(user.getUserName()), mailboxSession);
        }

        return storeMailboxManager.getMailbox(mailboxPath, mailboxSession);
    }

    private MailboxPath createDefaultMailboxPath(String userName) {
        return new MailboxPath("#private", userName, "INBOX");
    }

    private InputStream toInputStream(MimeMessage mimeMessage) throws IOException, MessagingException {

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try {
            mimeMessage.writeTo(baos);
            return new ByteArrayInputStream(baos.toByteArray());
        } finally {
            IOUtils.closeQuietly(baos);
        }
    }

    public void stopAndDestroy() {
        destroyPop3ServerFactory();
    }

}
