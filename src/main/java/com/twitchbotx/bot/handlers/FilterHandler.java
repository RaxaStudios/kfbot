package com.twitchbotx.bot.handlers;

import com.twitchbotx.bot.ConfigParameters;
import com.twitchbotx.bot.Datastore;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import java.util.logging.Logger;

public final class FilterHandler {

    private static final Logger LOGGER = Logger.getLogger(FilterHandler.class.getSimpleName());

    private final Datastore store;

    public FilterHandler(final Datastore store) {
        this.store = store;
    }

    /**
     * Methods to add and remove moderator filters
     *
     * @param msg
     * @param user
     *
     * @return
     * The message to be sent back to the user in a whisper.
     */
    public String getAllFilters(final String msg, final String user) {
        try {
            String[] filters = new String[store.getFilters().size()];
            for (int i = 0; i < store.getFilters().size(); i++) {
                final ConfigParameters.Filter filter = store.getFilters().get(i);
                filters[i] = filter.name;
            }
            StringBuilder sb = new StringBuilder();
            for (int j = 0; j < filters.length; j++) {
                if (j > 0) {
                    sb.append("], [");
                }
                sb.append(filters[j]);
            }
            return ".w " + user + " Current filters: [" + sb.toString() + "]";
        } catch (IllegalArgumentException e) {
            LOGGER.info(e.toString());
        }
        return ".w " + user + " No filters found.";
    }

    public String addFilter(String msg, String user) {
        try {
            String parameters = CommonUtility.getInputParameter("!filter-add", msg, true);
            int separator = parameters.indexOf(" ");
            String filter = parameters.substring(0, separator);
            String reason = parameters.substring(separator + 1);
            for (int i = 0; i < store.getFilters().size(); i++) {
                final ConfigParameters.Filter configFilter = store.getFilters().get(i);
                if (filter.equals(configFilter.name)) {
                    return ".w " + user + " Filter already exists.";
                }
            }
            Element newNode = this.elements.doc.createElement("filter");
            newNode.setAttribute("name", filter);
            newNode.setAttribute("reason", reason);
            newNode.setAttribute("disable", "false");
            this.elements.filters.appendChild(newNode);
            writeXML();
            return ".w " + user + " Filter added.";

        } catch (IllegalArgumentException e) {
            LOGGER.info(e.toString());
        }

        return ".w " + user + " No filters added.";
    }

    public String deleteFilter(String msg, String user) {
        try {
            String filterName = CommonUtility.getInputParameter("!filter-delete", msg, true);
            for (int i = 0; i < store.getFilters().size(); i++) {
                final ConfigParameters.Filter configFilter = store.getFilters().get(i);
                if (filterName.equals(configFilter.name)) {
                    this.elements.filters.removeChild(n);
                    writeXML();

                    return ".w " + user + " Filter deleted.";
                }
            }

            return ".w " + user + " Filter not found.";
        } catch (IllegalArgumentException e) {
            LOGGER.info(e.toString());
        }
    }
}
