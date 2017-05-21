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
public final class ConfigParameters {

    private final static Logger LOGGER = Logger.getLogger(ConfigParameters.class.getSimpleName());

    /**
     * An inner class that holds all the positions of all the elements.
     */
    public final static class Elements {
        public Document doc;

        public Element configNode;

        public Element commands;

        public Element counters;

        public NodeList commandNodes;

        public NodeList counterNodes;

        public Element filters;

        public NodeList filterNodes;
    }

    /**
     * An inner class that holds simple configurations, these fields should not
     * change once the bot is up, and nobody should be changing them live
     * anyway.
     */
    public final static class Configuration {
        public String account;

        public String clientID;

        public String password;

        public String joinedChannel;

        public String host;

        public int port;
        
        public String pubSub;

        public String youtubeApi;

        public String youtubeTitle;

        @Override
        public String toString() {
            return "Configuration{" +
                    "account='" + account + '\'' +
                    ", clientID='" + clientID + '\'' +
                    ", password='" + password + '\'' +
                    ", joinedChannel='" + joinedChannel + '\'' +
                    ", host='" + host + '\'' +
                    ", port=" + port +
                    ", pubSub='" + pubSub + '\'' +
                    ", youtubeApi='" + youtubeApi + '\'' +
                    ", youtubeTitle='" + youtubeTitle + '\'' +
                    '}';
        }
    }

    /**
     * Represents a single Command
     */
    public final static class Command {

        // A long string that signifies who has the credentials to use this command
        public String authentication;

        // A command name
        public String name;

        // No clue what this is used for
        public String cdUntil;

        // Time in seconds for cooldown
        public String cooldownInSec;

        // Flag for whenever this command is enabled/disabled
        public String disabled;

        // No clue what this is used for
        public String initialDelay;

        // No clue what this is used for
        public String interval;

        // No clue what this is used for
        public String repeating;

        // The sound to play when command is used?
        public String sound;

        // The text to display when this command is hit
        public String text;

        @Override
        public String toString() {
            return "Command{" +
                    "authentication='" + authentication + '\'' +
                    ", name='" + name + '\'' +
                    ", cdUntil='" + cdUntil + '\'' +
                    ", cooldownInSec='" + cooldownInSec + '\'' +
                    ", disabled='" + disabled + '\'' +
                    ", initialDelay='" + initialDelay + '\'' +
                    ", interval='" + interval + '\'' +
                    ", repeating='" + repeating + '\'' +
                    ", sound='" + sound + '\'' +
                    ", text='" + text + '\'' +
                    '}';
        }
    }

    /**
     * Represents a single counter, for counting a list of items
     */
    public final static class Counter {
        // The name of the counter
        public String name;

        // The current count
        public int count;

        @Override
        public String toString() {
            return "Counter{" +
                    "name='" + name + '\'' +
                    ", count=" + count +
                    '}';
        }
    }

    /**
     * Represents a single filter, for counting a single filter
     */
    public final static class Filter {

        public boolean disabled;

        public String name;

        public String reason;

        @Override
        public String toString() {
            return "Filter{" +
                    "disabled='" + disabled + '\'' +
                    ", name='" + name + '\'' +
                    ", reason='" + reason + '\'' +
                    '}';
        }
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

        elements.configNode = (Element) elements.doc.getElementsByTagName("config").item(0);
        elements.commands = (Element) elements.doc.getElementsByTagName("commands").item(0);
        elements.commandNodes = elements.commands.getElementsByTagName("command");
        elements.counters = (Element) elements.doc.getElementsByTagName("counters").item(0);
        elements.counterNodes = elements.counters.getElementsByTagName("counter");
        elements.filters = (Element) elements.doc.getElementsByTagName("filters").item(0);
        elements.filterNodes = elements.filters.getElementsByTagName("filter");
        return elements;
    }
}
