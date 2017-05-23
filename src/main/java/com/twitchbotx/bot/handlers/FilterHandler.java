package com.twitchbotx.bot.handlers;

import org.w3c.dom.Element;
import org.w3c.dom.Node;

/**
 * Created by Boris on 5/22/2017.
 */
public class FilterHandler {

    /*
** Methods to add and remove moderator filters
**
 */
    public void filterAll(String msg, String user) {
        try {
            String[] filters = new String[store.getFilters().size()];
            for (int i = 0; i < store.getFilters().size(); i++) {
                Node n = this.elements.filterNodes.item(i);
                Element e = (Element) n;
                filters[i] = e.getAttribute("name");
            }
            StringBuilder sb = new StringBuilder();
            for (int j = 0; j < filters.length; j++) {
                if (j > 0) {
                    sb.append("], [");
                }
                sb.append(filters[j]);
            }
            sendWhisper(".w " + user + " Current filters: [" + sb.toString() + "]");

            return;
        } catch (IllegalArgumentException e) {
            LOGGER.info(e.toString());
        }
        sendWhisper(".w " + user + " No filters found.");
    }

    public void filterAdd(String msg, String user) {
        try {
            String parameters = getInputParameter("!filter-add", msg, true);
            int separator = parameters.indexOf(" ");
            String filter = parameters.substring(0, separator);
            String reason = parameters.substring(separator + 1);
            for (int i = 0; i < store.getFilters().size(); i++) {
                Node n = this.elements.filterNodes.item(i);
                Element e = (Element) n;
                if (filter.contentEquals(e.getAttribute("name"))) {
                    sendWhisper(".w " + user + " Filter already exists.");
                    return;
                }
            }
            Element newNode = this.elements.doc.createElement("filter");
            newNode.setAttribute("name", filter);
            newNode.setAttribute("reason", reason);
            newNode.setAttribute("disable", "false");
            this.elements.filters.appendChild(newNode);
            writeXML();
            sendWhisper(".w " + user + " Filter added.");
        } catch (IllegalArgumentException e) {
            LOGGER.info(e.toString());
        }
    }

    public void filterDel(String msg, String user) {
        try {
            String filterName = getInputParameter("!filter-delete", msg, true);
            for (int i = 0; i < store.getFilters().size(); i++) {
                Node n = this.elements.filterNodes.item(i);
                Element e = (Element) n;
                if (filterName.contentEquals(e.getAttribute("name"))) {
                    this.elements.filters.removeChild(n);
                    writeXML();
                    sendWhisper(".w " + user + " Filter deleted.");
                    return;
                }
            }

            sendWhisper(".w " + user + " Filter not found.");
        } catch (IllegalArgumentException e) {
            LOGGER.info(e.toString());
        }
    }
}
