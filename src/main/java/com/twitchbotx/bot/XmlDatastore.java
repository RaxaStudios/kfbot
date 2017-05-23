package com.twitchbotx.bot;

import org.w3c.dom.Element;
import org.w3c.dom.Node;

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
    public void updateCommands(final List<String> commands) {

    }

    @Override
    public void updateCounters(final List<String> counters) {

    }

    @Override
    public void updateFilters(final List<String> filters) {

    }
}
