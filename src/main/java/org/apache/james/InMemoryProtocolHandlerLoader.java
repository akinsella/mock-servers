package org.apache.james;

import org.apache.commons.configuration.Configuration;
import org.apache.james.mailbox.MailboxManager;
import org.apache.james.pop3server.core.PassCmdHandler;
import org.apache.james.protocols.api.handler.ProtocolHandler;
import org.apache.james.protocols.lib.handler.ProtocolHandlerLoader;

public class InMemoryProtocolHandlerLoader implements ProtocolHandlerLoader {


    private MailboxManager mailboxManager;

    public InMemoryProtocolHandlerLoader(MailboxManager mailboxManager) {
        this.mailboxManager = mailboxManager;
    }

    @Override
    public ProtocolHandler load(String name, Configuration config) throws LoadingException {
        try {
            ProtocolHandler protocoleHandler = (ProtocolHandler)Class.forName(name).newInstance();
            if (protocoleHandler instanceof PassCmdHandler) {
                configurePassCmdHandler((PassCmdHandler)protocoleHandler);
            }

            return protocoleHandler;
        } catch (Exception e) {
            throw new LoadingException(e.getMessage(), e);
        }
    }

    private void configurePassCmdHandler(PassCmdHandler passCmdHandler) throws Exception {
        passCmdHandler.setMailboxManager(mailboxManager);
    }

}
