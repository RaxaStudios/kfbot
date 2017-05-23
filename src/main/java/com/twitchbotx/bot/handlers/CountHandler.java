package com.twitchbotx.bot.handlers;

import com.twitchbotx.bot.ConfigParameters;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

/**
 * Created by Boris on 5/22/2017.
 */
public class CountHandler {

    /*
    ** Allows for counters to be added, deleted, set, added to, and all totals calls
    **
    ** return name and value
     */
    public void cntAdd(String msg) {
        try {
            String name = getInputParameter("!cnt-add", msg, true);
            for (int i = 0; i < store.getCounters().size(); i++) {
                Node n = this.elements.counterNodes.item(i);
                Element e = (Element) n;
                if (name.contentEquals(e.getAttribute("name"))) {
                    sendMessage("Counter [" + name + "] already exists.");
                    return;
                }
            }
            Element newNode = this.elements.doc.createElement("counter");
            newNode.appendChild(this.elements.doc.createTextNode("0"));
            newNode.setAttribute("name", name);

            this.elements.counters.appendChild(newNode);
            writeXML();
            String confirmation = "Added counter [" + name + "]";
            sendMessage(confirmation);
        } catch (IllegalArgumentException e) {
            sendMessage("Syntax: !cnt-add [name]");
        }
    }

    public void cntDelete(String msg) {
        String name = getInputParameter("!cnt-delete", msg, true);
        for (int i = 0; i < store.getCounters().size(); i++) {
            Node n = this.elements.counterNodes.item(i);
            Element e = (Element) n;
            if (name.contentEquals(e.getAttribute("name"))) {
                this.elements.counters.removeChild(n);
                writeXML();
                sendMessage("Counter [" + name + "] deleted.");
                return;
            }
        }
        sendMessage("Counter [" + name + "] not found.");
    }

    public void cntSet(String msg) {
        try {
            String parameters = getInputParameter("!cnt-set", msg, true);
            int separator = parameters.indexOf(" ");
            String name = parameters.substring(0, separator);
            int value = Integer.parseInt(parameters.substring(separator + 1));
            for (int i = 0; i < store.getCounters().size(); i++) {
                Node n = this.elements.counterNodes.item(i);
                Element e = (Element) n;
                if (name.contentEquals(e.getAttribute("name"))) {
                    e.setTextContent(Integer.toString(value));
                    writeXML();
                    sendMessage("Counter [" + name + "] set to [" + Integer.toString(value) + "]");
                    return;
                }
            }
            sendMessage("Counter [" + name + "] not found.");
        } catch (IllegalArgumentException e) {
            sendMessage("Syntax: !cnt-set [name] [value]");
        }
    }

    public void cntCurrent(String msg) {
        String name = getInputParameter("!cnt-current", msg, true);
        for (int i = 0; i < store.getCounters().size(); i++) {
            final ConfigParameters.Counter counter = store.getCounters().get(i);
            if (name.contentEquals(counter.name)) {
                sendMessage("Counter [" + name + "] is currently [" + counter.count + "]");
                return;
            }
        }
        sendMessage("Counter [" + name + "] not found.");
    }

    public void count(String msg) {
        try {
            String parameters = getInputParameter("!countadd", msg, true);
            int separator = parameters.indexOf(" ");
            String name = parameters.substring(0, separator);
            int delta = Integer.parseInt(parameters.substring(separator + 1));

            for (int i = 0; i < store.getCounters().size(); i++) {
                Node n = this.elements.counterNodes.item(i);
                Element e = (Element) n;
                if (name.contentEquals(e.getAttribute("name"))) {
                    int value = Integer.parseInt(e.getTextContent()) + delta;
                    e.setTextContent(Integer.toString(value));
                    writeXML();
                    sendMessage(delta + " points added to [" + name + "]");
                    return;
                }
            }
            sendMessage("Counter [" + name + "] not found.");
        } catch (IllegalArgumentException e) {
            sendMessage("Syntax: !countadd [name] [value]");
        }
    }

    public void totals(String msg) {
        try {
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
                    sendMessage("Current totals: [" + counters[0][0] + "]: " + counters[0][1]);
                    break;
                case 2:
                    sendMessage("Current totals: [" + counters[0][0] + "]: " + counters[0][1] + " [" + counters[1][0] + "]: " + counters[1][1]);
                    break;
                case 3:
                    sendMessage("Current totals: [" + counters[0][0] + "]: " + counters[0][1] + " [" + counters[1][0] + "]: " + counters[1][1] + " [" + counters[2][0] + "]: " + counters[2][1]);
                    break;
                case 4:
                    sendMessage("Current totals: [" + counters[0][0] + "]: " + counters[0][1] + " [" + counters[1][0] + "]: " + counters[1][1] + " [" + counters[2][0] + "]: " + counters[2][1] + " " + counters[3][0] + ": " + counters[3][1]);
                    break;
                default:
                    break;
            }
        } catch (IllegalArgumentException e) {
            sendMessage("No counts found.");
        }
    }
}
