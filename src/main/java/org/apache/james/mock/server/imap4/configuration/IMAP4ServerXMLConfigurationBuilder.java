package org.apache.james.mock.server.imap4.configuration;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.XMLConfiguration;
import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;

import java.io.StringReader;

import static org.apache.commons.io.IOUtils.closeQuietly;

/*

<?xml version="1.0"?>
        <!--
        Licensed to the Apache Software Foundation (ASF) under one
        or more contributor license agreements.  See the NOTICE file
        distributed with this work for additional information
        regarding copyright ownership.  The ASF licenses this file
        to you under the Apache License, Version 2.0 (the
        "License"); you may not use this file except in compliance
        with the License.  You may obtain a copy of the License at

        http://www.apache.org/licenses/LICENSE-2.0

        Unless required by applicable law or agreed to in writing,
        software distributed under the License is distributed on an
        "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
        KIND, either express or implied.  See the License for the
        specific language governing permissions and limitations
        under the License.
        -->

        <!-- See http://james.apache.org/server/3/config.html for usage -->

        <imapservers>
            <imapserver enabled="true">
                <jmxName>imapserver</jmxName>
                <bind>0.0.0.0:143</bind>
                <connectionBacklog>200</connectionBacklog>
                <tls socketTLS="false" startTLS="false">
                    <keystore>file://conf/keystore</keystore>
                    <secret>yoursecret</secret>
                    <provider>org.bouncycastle.jce.provider.BouncyCastleProvider</provider>
                </tls>

                <!-- Disallow plain authenticate / login. So any client will need to STARTTLS before try to login -->
                <!-- or the socket must be using TLS in general -->
                <plainAuthDisallowed>false</plainAuthDisallowed>

                <!-- COMPRESS extension -->
                <compress>false</compress>

                <!-- Maximal allowed line-length before a BAD response will get returned to the client -->
                <!-- This should be set with caution as a to high value can make the server a target for DOS! -->
                <maxLineLength>65536</maxLineLength>

                <!-- 10MB size limit before we will start to stream to a temporary file -->
                <inMemorySizeLimit>10485760</inMemorySizeLimit>
                <handler>
                    <connectionLimit> 0 </connectionLimit>
                    <connectionLimitPerIP> 0 </connectionLimitPerIP>
                </handler>
            </imapserver>
        </imapservers>
*/

public class Imap4ServerXMLConfigurationBuilder {

    public static XMLConfiguration createConfigurationWithPort(int port) throws ConfigurationException {
        Document document = DocumentHelper.createDocument();
        Element imapserversRootElt = document.addElement("imapservers");
        Element imapServerElt = imapserversRootElt.addElement("imapserver");
        imapServerElt.addAttribute("enabled", "true");
        imapServerElt.addElement("jmxName").setText("imapserver");
        imapServerElt.addElement("bind").setText(String.format("0.0.0.0:%1$s", port));
        imapServerElt.addElement("connectionBacklog").setText("200");
        Element tlsElt = imapServerElt.addElement("tls");
        tlsElt.addAttribute("socketTLS", "false").addAttribute("startTLS", "false");
        tlsElt.addElement("keystore").setText("file://conf/keystore");
        tlsElt.addElement("secret").setText("yoursecret");
        tlsElt.addElement("provider").setText("org.bouncycastle.jce.provider.BouncyCastleProvider");
        imapServerElt.addElement("plainAuthDisallowed").setText("false");
        imapServerElt.addElement("compress").setText("false");
        imapServerElt.addElement("maxLineLength").setText("65536");
        imapServerElt.addElement("inMemorySizeLimit").setText("65536");
        Element handlerElt = imapServerElt.addElement("handler");
        handlerElt.addElement("connectionLimit").setText("0");
        handlerElt.addElement("connectionLimitPerIP").setText("0");

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
