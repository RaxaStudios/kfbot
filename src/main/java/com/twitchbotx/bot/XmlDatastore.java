package com.twitchbotx.bot;

import org.w3c.dom.Element;
import org.w3c.dom.Node;

import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * This class deals with all the interactions with the database.
 *
 * Primarily, it deals with fetching of information, and updating the information.
 */
public final class XmlDatastore implements Datastore {

    // store off the elements
    private final ConfigParameters.Elements elements;

    /**
     * Constructor for the XML datastore
     *
     * @param parameters
     * A list of fields in the XML file
     */
    public XmlDatastore(final ConfigParameters.Elements parameters) {
        this.elements = parameters;
    }

    @Override
    public ConfigParameters.Configuration getConfiguration() {
        final ConfigParameters.Configuration configuration = new ConfigParameters.Configuration();
        configuration.account
                = this.elements.configNode.getElementsByTagName("botAccount").item(0).getTextContent();

        configuration.password
                = this.elements.configNode.getElementsByTagName("botOAUTH").item(0).getTextContent();

        configuration.clientID
                = this.elements.configNode.getElementsByTagName("botClientID").item(0).getTextContent();

        configuration.joinedChannel
                = this.elements.configNode.getElementsByTagName("myChannel").item(0).getTextContent();

        configuration.host
                = this.elements.configNode.getElementsByTagName("irc").item(0).getTextContent();

        configuration.port
                = Integer.parseInt(this.elements.configNode.getElementsByTagName("ircPort").item(0).getTextContent());

        configuration.pubSub = this.elements.configNode.getElementsByTagName("pubSub").item(0).getTextContent();

        configuration.streamerStatus = this.elements.configNode.getElementsByTagName("twitchStreamerStatus").item(0).getTextContent();

        configuration.followage = this.elements.configNode.getElementsByTagName("twitchFollowage").item(0).getTextContent();

        configuration.youtubeApi = this.elements.configNode.getElementsByTagName("youtubeAPI").item(0).getTextContent();

        configuration.youtubeTitle = this.elements.configNode.getElementsByTagName("youtubeTitle").item(0).getTextContent();

        configuration.onlineCheckTimer = Integer.parseInt(this.elements.configNode.getElementsByTagName("onlineCheckTimer").item(0).getTextContent());

        configuration.recentMessageCacheSize = Integer.parseInt(this.elements.configNode.getElementsByTagName("recentMessageCacheSize").item(0).getTextContent());

        configuration.numCounters = Integer.parseInt(this.elements.configNode.getElementsByTagName("numberOfCounters").item(0).getTextContent());

        configuration.pyramidResponse = this.elements.configNode.getElementsByTagName("pyramidResponse").item(0).getTextContent();

        return configuration;
    }

    @Override
    public List<ConfigParameters.Command> getCommands() {
        final List<ConfigParameters.Command> commands = new ArrayList<>();

        for (int i = 0; i < this.elements.commandNodes.getLength(); i++) {
            Node n = this.elements.commandNodes.item(i);
            Element e = (Element) n;

            final ConfigParameters.Command command = new ConfigParameters.Command();
            command.name = e.getAttribute("name");
            command.disabled = Boolean.parseBoolean(e.getAttribute("disabled"));
            command.text = e.getTextContent();
            command.cdUntil = e.getAttribute("cdUntil");
            command.cooldownInSec = Long.parseLong(e.getAttribute("cooldown"));
            command.sound = e.getAttribute("sound");
            commands.add(command);
        }
        return commands;
    }

    @Override
    public List<ConfigParameters.Filter> getFilters() {
        final List<ConfigParameters.Filter> filters = new ArrayList<>();
        for (int i = 0; i < this.elements.filterNodes.getLength(); i++) {
            Node n = this.elements.filterNodes.item(i);
            Element e = (Element) n;

            final ConfigParameters.Filter filter = new ConfigParameters.Filter();
            filter.name = e.getAttribute("name");
            filter.reason = e.getAttribute("reason");
            filter.disabled = Boolean.parseBoolean(e.getAttribute("disabled"));
            filters.add(filter);
        }

        return filters;
    }

    @Override
    public List<ConfigParameters.Counter> getCounters() {
        List<ConfigParameters.Counter> counters = new ArrayList<>();
        for (int i = 0; i < this.elements.counterNodes.getLength(); i++) {
            Node n = this.elements.commandNodes.item(i);
            Element e = (Element) n;

            final ConfigParameters.Counter counter = new ConfigParameters.Counter();
            counter.name = e.getAttribute("name");
            counter.count = Integer.parseInt(e.getAttribute("count"));
            counters.add(counter);
        }

        return counters;
    }

    @Override
    public void modifyConfiguration(final String node, final String value) {
        final Node n = this.elements.configNode.getElementsByTagName(node).item(0);
        final Element el = (Element) n;
        el.setTextContent(value);
        commit();
    }


    @Override
    public boolean addCommand(final String command, final String text) {
        for (int i = 0; i < this.elements.commandNodes.getLength(); i++) {
            final Node n = this.elements.commandNodes.item(i);
            final Element e = (Element) n;
            if (command.equals(e.getAttribute("name"))) {
                return false;
            }
        }

        Element newNode = this.elements.doc.createElement("command");
        newNode.appendChild(this.elements.doc.createTextNode(text));
        newNode.setAttribute("name", command.toLowerCase());
        newNode.setAttribute("auth", "");
        newNode.setAttribute("repeating", "false");
        newNode.setAttribute("initialDelay", "0");
        newNode.setAttribute("interval", "0");
        newNode.setAttribute("cooldown", "0");
        newNode.setAttribute("cdUntil", "");
        newNode.setAttribute("sound", "");
        newNode.setAttribute("disabled", "false");
        this.elements.commands.appendChild(newNode);
        commit();

        return true;
    }

    @Override
    public boolean editCommand(final String command, final String text) {
        for(int i = 0; i < this.elements.commandNodes.getLength(); i++) {
            final Node n = this.elements.commandNodes.item(i);
            final Element e = (Element) n;
            if(command.equals(e.getAttribute("name"))) {
                e.setTextContent(text);
                commit();
                return true;
            }
        }

        return false;
    }

    @Override
    public boolean deleteCommand(final String command) {
        for(int i = 0; i < this.elements.commandNodes.getLength(); i++) {
            final Node n = this.elements.commandNodes.item(i);
            final Element e = (Element) n;
            if(command.equals(e.getAttribute("name"))) {
                this.elements.commands.removeChild(n);
                commit();
                return true;
            }
        }

        return false;
    }

    @Override
    public String listCommand() {
        return "";
    }

    @Override
    public boolean updateCounter(final String name, final int delta) {
        for (int i = 0; i < this.elements.counterNodes.getLength(); i++) {
            final Node n = this.elements.counterNodes.item(i);
            final Element e = (Element) n;
            if (name.equals(e.getAttribute("name"))) {
                int value = Integer.parseInt(e.getTextContent()) + delta;
                e.setTextContent(Integer.toString(value));
                commit();

                return true;
            }
        }

        return false;
    }

    @Override
    public boolean setCounter(final String name, final int value) {
        for (int i = 0; i < this.elements.counterNodes.getLength(); i++) {
            final Node n = this.elements.counterNodes.item(i);
            final Element e = (Element) n;
            if (name.equals(e.getAttribute("name"))) {
                e.setTextContent(Integer.toString(value));
                commit();

                return true;
            }
        }

        final Element counterNode = this.elements.doc.createElement("counter");
        counterNode.appendChild(this.elements.doc.createTextNode(Integer.toString(value)));
        counterNode.setAttribute("name", name);
        commit();

        return true;
    }

    @Override
    public boolean addCounter(final String name) {
        for (int i = 0; i < this.elements.counterNodes.getLength(); i++) {
            final Node n = this.elements.counterNodes.item(i);
            final Element e = (Element) n;
            if (name.equals(e.getAttribute("name"))) {
                return false;
            }
        }

        final Element newNode = this.elements.doc.createElement("counter");
        newNode.appendChild(this.elements.doc.createTextNode("0"));
        newNode.setAttribute("name", name);

        this.elements.counters.appendChild(newNode);
        commit();

        return true;
    }

    @Override
    public boolean deleteCounter(final String counterName) {
        for (int i = 0; i < this.elements.counterNodes.getLength(); i++) {
            final Node n = this.elements.counterNodes.item(i);
            final Element e = (Element) n;
            if (counterName.equals(e.getAttribute("name"))) {
                this.elements.filters.removeChild(n);
                commit();
                return true;
            }
        }

        return false;
    }

    @Override
    public boolean addFilter(final ConfigParameters.Filter filter) {
        for (int i = 0; i < this.elements.filterNodes.getLength(); i++) {
            final Node n = this.elements.filterNodes.item(i);
            final Element e = (Element) n;
            if (filter.name.equals(e.getAttribute("name"))) {
                return false;
            }
        }

        Element newNode = this.elements.doc.createElement("filter");
        newNode.setAttribute("name", filter.name);
        newNode.setAttribute("reason", filter.reason);
        if(filter.disabled) {
            newNode.setAttribute("disable", "true");
        } else {
            newNode.setAttribute("disable", "false");
        }

        this.elements.filters.appendChild(newNode);
        commit();

        return true;
    }

    @Override
    public boolean deleteFilter(final String filterName) {
        for (int i = 0; i < this.elements.filterNodes.getLength(); i++) {
            final Node n = this.elements.filterNodes.item(i);
            final Element e = (Element) n;
            if (filterName.contentEquals(e.getAttribute("name"))) {
                this.elements.filters.removeChild(n);
                commit();
                return true;
            }
        }

        return false;
    }

    @Override
    public void updateCooldownTimer(final String command, long cooldownUntil) {
        for (int i = 0; i < this.elements.commandNodes.getLength(); i++) {
            Node n = this.elements.commandNodes.item(i);
            Element el = (Element) n;
            if(command.equals(el.getAttribute("name"))) {
                el.setAttribute("cdUntil", Long.toString(cooldownUntil));
            }
        }
    }

    @Override
    public void commit() {
        try {
            File configFile = new File("kfbot.xml");
            TransformerFactory tFactory = TransformerFactory.newInstance();
            Transformer transformer = tFactory.newTransformer();
            DOMSource source = new DOMSource(this.elements.doc);
            StreamResult result = new StreamResult(configFile);
            transformer.transform(source, result);
        } catch (TransformerException e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean setUserCommandAttribute(final String command,
                                           final String attribute,
                                           final String value,
                                           final boolean allowReservedCmds) {
        if (!allowReservedCmds && Commands.getInstance().isReservedCommand(command)) {
            return false;
        }

        for (int i = 0; i < this.elements.commandNodes.getLength(); i++) {
            Node n = this.elements.commandNodes.item(i);
            Element el = (Element) n;
            if (command.contentEquals(el.getAttribute("name"))) {
                el.setAttribute(attribute, value);
                commit();
                return true;
            }
        }

        return false;
    }
}
