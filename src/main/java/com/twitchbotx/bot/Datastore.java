package com.twitchbotx.bot;

import java.util.List;

/**
 * This class represents a datastore, for where we store the configuration.
 */
public interface Datastore {

    /**
     * This method is used to read from the configurations that shouldn't ever change.
     */
    ConfigParameters.Configuration getConfiguration();

    /**
     * This method is used to get a list of commands.
     *
     * @return
     * A list of commands
     */
    List<String> getCommands();

    /**
     * This method is used to get a list of filters.
     *
     * @return
     * A list of filters
     */
    List<String> getFilters();

    /**
     * This method is used to get a list of counters.
     *
     * @return
     * A list of counters
     */
    List<String> getCounters();

    /**
     * This method updates a list of existing commands.
     *
     * @param commands
     * Update the existing list of commands to this.
     */
    void updateCommands(final List<String> commands);

    /**
     * This method updates a list of existing commands.
     *
     * @param counters
     * Update the existing list of counters to this.
     */
    void updateCounters(final List<String> counters);

    /**
     * This method updates a list of existing filters.
     *
     * @param filters
     * Update the existing list of filters to this.
     */
    void updateFilters(final List<String> filters);
}
