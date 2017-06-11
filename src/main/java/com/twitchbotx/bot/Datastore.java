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
    List<ConfigParameters.Command> getCommands();

    /**
     * This method is used to get a list of filters.
     *
     * @return
     * A list of filters
     */
    List<ConfigParameters.Filter> getFilters();

    /**
     * This method is used to get a list of counters.
     *
     * @return
     * A list of counters
     */
    List<ConfigParameters.Counter> getCounters();

    /**
     * This method is used to directly modify certain startup configurations.
     *
     * @param node
     * The tagged node in the configuration block
     *
     * @param value
     * The value to be set to
     */
    void modifyConfiguration(final String node, final String value);

    /**
     * This method adds a new command to the list of existing commands. If the command already exist, do nothing.
     *
     * @param command
     * The command name to be added.
     *
     * @param text
     * The text that the command associates with
     *
     * @return
     * Whether the command was added.
     */
    boolean addCommand(final String command, final String text);

    /**
     * This method edits or adds an existing command. If the command does not exist, simply add a new one.
     * If it already exists, modify it.
     *
     * @param command
     * The command name to be edited
     *
     * @param text
     * The text that the command associates with
     *
     * @return
     * Whether the command was edited.
     */
    boolean editCommand(final String command, final String text);

    /**
     *
     * @param command
     * @return
     */
    boolean deleteCommand(final String command);

    String listCommand();

    /**
     * This method updates an existing counter. If the counter does not exist, simply fail and do nothing.
     * @param name
     * @param value
     * @return
     */
    boolean updateCounter(final String name, final int value);

    /**
     * This method updates an existing counter. If the counter does not exist, simply fail and do nothing.
     *
     * @param name
     * Update the existing list of counters to this.
     */
    boolean setCounter(final String name, final int value);

    /**
     * This method adds a new counter to a list of existing commands.
     * If the counter already exists, simply fail and do nothing.
     *
     * @param name
     * The name of the counter to be added.
     */
    boolean addCounter(final String name);

    /**
     * This method deletes a counter from a list of existing counters.
     *
     * @param counterName
     *
     *
     * @return
     */
    boolean deleteCounter(final String counterName);

    /**
     * This method updates a list of existing filters. If a filter already exists, simply fail and do nothing.
     *
     * @param filter
     * Update the existing list of filters to this.
     *
     * @return
     * True - A filter was added.
     * False - A filter already exist and could not be added.
     */
    boolean addFilter(final ConfigParameters.Filter filter);

    /**
     * This method deletes an existing filter. If a filter does not exist, simply fail and do nothing.
     *
     * @param filterName
     * The name of the filter to delete
     *
     * @return
     * True - A filter was deleted.
     * False - The filter was not false.
     */
    boolean deleteFilter(final String filterName);

    /**
     * This method will commit all changes to the database.
     */
    void commit();
}

