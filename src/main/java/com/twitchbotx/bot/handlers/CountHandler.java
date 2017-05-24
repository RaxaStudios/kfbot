package com.twitchbotx.bot.handlers;

import com.twitchbotx.bot.ConfigParameters;
import com.twitchbotx.bot.Datastore;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import java.util.logging.Logger;

public final class CountHandler {

    private static final Logger LOGGER = Logger.getLogger(CountHandler.class.getSimpleName());

    private final Datastore store;

    public CountHandler(final Datastore store) {
        this.store = store;
    }

    /*
    ** Allows for counters to be added, deleted, set, added to, and all totals calls
    **
    ** return name and value
     */
    public String addCounter(String msg) {
        try {
            String name = CommonUtility.getInputParameter("!cnt-add", msg, true);
            for (int i = 0; i < store.getCounters().size(); i++) {
                final ConfigParameters.Counter counter = store.getCounters().get(i);
                if (name.contentEquals(counter.name)) {
                    return "Counter [" + name + "] already exists.";
                }
            }
            Element newNode = this.elements.doc.createElement("counter");
            newNode.appendChild(this.elements.doc.createTextNode("0"));
            newNode.setAttribute("name", name);

            this.elements.counters.appendChild(newNode);
            writeXML();
            String confirmation = "Added counter [" + name + "]";
            return confirmation;
        } catch (IllegalArgumentException e) {
            return "Syntax: !cnt-add [name]";
        }
    }

    public String updateCount(String msg) {
        try {
            String parameters = CommonUtility.getInputParameter("!countadd", msg, true);
            int separator = parameters.indexOf(" ");
            String name = parameters.substring(0, separator);
            int delta = Integer.parseInt(parameters.substring(separator + 1));

            for (int i = 0; i < store.getCounters().size(); i++) {
                final ConfigParameters.Counter counter = store.getCounters().get(i);
                if (name.contentEquals(counter.name)) {
                    int value = counter.count + delta;
                    e.setTextContent(Integer.toString(value));
                    writeXML();
                    return delta + " points added to [" + name + "]";
                }
            }
            return "Counter [" + name + "] not found.";
        } catch (IllegalArgumentException e) {
            return "Syntax: !countadd [name] [value]";
        }
    }

    public String deleteCounter(String msg) {
        String name = CommonUtility.getInputParameter("!cnt-delete", msg, true);
        for (int i = 0; i < store.getCounters().size(); i++) {
            final ConfigParameters.Counter counter = store.getCounters().get(i);
            if (name.contentEquals(counter.name)) {
                this.elements.counters.removeChild(n);
                writeXML();
                return "Counter [" + name + "] deleted.";
                return;
            }
        }
        return "Counter [" + name + "] not found.";
    }

    public String setCounter(String msg) {
        try {
            String parameters = CommonUtility.getInputParameter("!cnt-set", msg, true);
            int separator = parameters.indexOf(" ");
            String name = parameters.substring(0, separator);
            int value = Integer.parseInt(parameters.substring(separator + 1));
            for (int i = 0; i < store.getCounters().size(); i++) {
                final ConfigParameters.Counter counter = store.getCounters().get(i);
                if (name.contentEquals(counter.name)) {
                    e.setTextContent(Integer.toString(value));
                    writeXML();
                    return "Counter [" + name + "] set to [" + Integer.toString(value) + "]";
                }
            }
            return "Counter [" + name + "] not found.";
        } catch (IllegalArgumentException e) {
            return "Syntax: !cnt-set [name] [value]";
        }
    }

    public String getCurrentCount(String msg) {
        String name = CommonUtility.getInputParameter("!cnt-current", msg, true);
        for (int i = 0; i < store.getCounters().size(); i++) {
            final ConfigParameters.Counter counter = store.getCounters().get(i);
            if (name.contentEquals(counter.name)) {
                return "Counter [" + name + "] is currently [" + counter.count + "]";
            }
        }
        return "Counter [" + name + "] not found.";
    }

    /**
     * This method will return the total number of counters and their respective count.
     *
     * @return
     * A string as the message to be sent out for all the counters.
     */
    public String totals() {
        String[][] counters = new String[store.getConfiguration().numCounters][2];
        int count = 0;
        for (int i = 0; i < store.getCounters().size(); i++) {
            final ConfigParameters.Command command = store.getCommands().get(i);
            counters[i][0] = command.name;
            counters[i][1] = command.text;
            count++;
        }
        switch (count) {
            case 1:
                return "Current totals: [" + counters[0][0] + "]: " + counters[0][1];
            case 2:
                return "Current totals: [" + counters[0][0] + "]: " + counters[0][1] + " [" + counters[1][0] + "]: " + counters[1][1];
            case 3:
                return "Current totals: [" + counters[0][0] + "]: " + counters[0][1] + " [" + counters[1][0] + "]: " + counters[1][1] + " [" + counters[2][0] + "]: " + counters[2][1];
            case 4:
                return "Current totals: [" + counters[0][0] + "]: " + counters[0][1] + " [" + counters[1][0] + "]: " + counters[1][1] + " [" + counters[2][0] + "]: " + counters[2][1] + " " + counters[3][0] + ": " + counters[3][1];
            default:
                return "No counters available";
        }
    }
}
