package org.apache.james.configuration;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.XMLConfiguration;
import org.apache.commons.io.IOUtils;
import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;

import java.io.StringReader;

import static org.apache.commons.io.IOUtils.closeQuietly;

public class Pop3ServerXMLConfigurationBuilder {

    public static XMLConfiguration createConfigurationWithPort(int port) throws ConfigurationException {
        Document document = DocumentHelper.createDocument();
        Element pop3serversRootElt = document.addElement("pop3servers");
        Element pop3serverElt = pop3serversRootElt.addElement("pop3server");
        pop3serverElt.addAttribute("enabled", "true");
        pop3serverElt.addElement("jmxName").setText("pop3server");
        pop3serverElt.addElement("bind").setText(String.format("0.0.0.0:%1$s", port));
        pop3serverElt.addElement("connectionBacklog").setText("200");
        Element tlsElt = pop3serverElt.addElement("tls");
        tlsElt.addAttribute("socketTLS", "false").addAttribute("startTLS", "false");
        tlsElt.addElement("keystore").setText("file://conf/keystore");
        tlsElt.addElement("secret").setText("yoursecret");
        tlsElt.addElement("provider").setText("org.bouncycastle.jce.provider.BouncyCastleProvider");
        pop3serverElt.addElement("connectiontimeout").setText("1200");
        pop3serverElt.addElement("connectionLimit").setText("0");
        pop3serverElt.addElement("connectionLimitPerIP").setText("0");
        Element handlerChainElt = pop3serverElt.addElement("handlerchain");
        handlerChainElt.addAttribute("enableJmx", "true");
        Element handlerElt = handlerChainElt.addElement("handler");
        handlerElt.addAttribute("class", "org.apache.james.pop3server.core.CoreCmdHandlerLoader");

        XMLConfiguration xmlConfiguration = new XMLConfiguration();
        StringReader in = new StringReader(document.asXML());
        try {
            xmlConfiguration.load(in);
        } finally {
            closeQuietly(in);
        }

        return xmlConfiguration;
    }

}
