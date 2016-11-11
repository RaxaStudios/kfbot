package com.twitchbotx.bot;

import java.io.File;
import java.io.IOException;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;
import java.util.logging.Logger;

/**
 * This class acts as a parser for the configuration file.
 */
public final class ConfigParser {

    private final static Logger LOGGER = Logger.getLogger(ConfigParser.class.getSimpleName());
    private TimerManagement timerManagement;

    /**
     * An inner class that holds all the positions of all the elements.
     */
    public final class Elements {

        public Document doc;

        public Element configNode;

        public Element commands;

        public Element counters;

        public NodeList commandNodes;

        public NodeList counterNodes;

    }

    /**
     * An inner class that holds simple configurations, these fields should not
     * change once the bot is up, and nobody should be changing them live
     * anyway.
     */
    public final class Configuration {

        public String account;

        public String clientID;

        public String password;

        public String joinedChannel;

        public String host;

        public int port;

        @Override
        public String toString() {
            return "Configuration{"
                    + "account=" + account
                    + ", password=" + password
                    + ", joinedChannel=" + joinedChannel
                    + ", host=" + host
                    + ", clientID=" + clientID
                    + ", port=" + port + '}';
        }
    }

    /**
     * This method parses the configuration and pulls information
     *
     * @param configNode An XML element for positioning the config node of the
     * XML file
     *
     * @return A class of configurations for unchanged configuration
     */
    public Configuration getConfiguration(final Element configNode) {
        final Configuration configuration = new Configuration();
        configuration.account
                = configNode.getElementsByTagName("botAccount").item(0).getTextContent();

        configuration.password
                = configNode.getElementsByTagName("botOAUTH").item(0).getTextContent();

        configuration.joinedChannel
                = configNode.getElementsByTagName("myChannel").item(0).getTextContent();

        configuration.host
                = configNode.getElementsByTagName("irc").item(0).getTextContent();

        configuration.port
                = Integer.parseInt(configNode.getElementsByTagName("ircPort").item(0).getTextContent());

        configuration.clientID
                = configNode.getElementsByTagName("botClientID").item(0).getTextContent();

        return configuration;
    }

    /**
     * This method will parse the configuration and save off references to each
     * of the parsed XML file parts.
     *
     * @param configFile The configuration file path (.XML file path) from the
     * source code path
     *
     * @return A set of references of the XML file.
     *
     * @throws ParserConfigurationException If the file cannot be parsed (not a
     * valid XML file), this exception is thrown.
     *
     * @throws SAXException If the SAX parser fails to parse an element, this
     * exception is thrown.
     *
     * @throws IOException If the file cannot be read (no file exist, or wrong
     * path), this exception is thrown.
     */
    public Elements parseConfiguration(final String configFile)
            throws ParserConfigurationException, SAXException, IOException {

        // Declare the file, run a document (XML) parser through it
        // Read the entire file into RAM, and return a set of all the element
        // locations
        final File configurationFile = new File(configFile);
        final DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        final DocumentBuilder builder = factory.newDocumentBuilder();

        final Elements elements = new Elements();
        elements.doc = builder.parse(configurationFile);
        elements.doc.getDocumentElement().normalize();

        LOGGER.info("Completed reading the XML file");

        elements.configNode
                = (Element) elements.doc.getElementsByTagName("config").item(0);

        elements.commands = (Element) elements.doc.getElementsByTagName("commands").item(0);
        elements.commandNodes = elements.commands.getElementsByTagName("command");
        elements.counters = (Element) elements.doc.getElementsByTagName("counters").item(0);
        elements.counterNodes = elements.counters.getElementsByTagName("counter");
        return elements;
    }
}
