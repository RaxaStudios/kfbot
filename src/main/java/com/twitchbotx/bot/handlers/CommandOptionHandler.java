package com.twitchbotx.bot.handlers;

import java.io.BufferedReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import com.twitchbotx.bot.Commands;
import com.twitchbotx.bot.ConfigParameters;
import com.twitchbotx.bot.Datastore;
import com.twitchbotx.bot.TwitchBotX;
import org.w3c.dom.DOMException;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import sun.audio.AudioPlayer;
import sun.audio.AudioStream;
import java.time.LocalDate;
import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;

/**
 * This class is a command handler for most of the common commands in this bot.
 */
public final class CommandOptionHandler {

    /*
        ** If command = !filter-all & username is valid & mod requirement is met per XML & sub requirement is met per XML
        ** Then create a moderationHandler and send "trailing" to the handleTool method
        ** filter, command, set broadcaster only
        ** See XML for other requirements
     */
    private static final Logger LOGGER = Logger.getLogger(TwitchBotX.class.getSimpleName());

    private final Datastore store;

    /**
     * This is a simple constructor for the command handler.
     *
     * It will use elements to write/rewrite the XML as storage.
     *
     * @param store The element references to the XML data
     */
    public CommandOptionHandler(final Datastore store) {
        this.store = store;
    }

    public String parseForUserCommands(String msg, String username, boolean mod, boolean sub) {
        for (int i = 0; i < store.getCommands().size(); i++) {
            try {
                final ConfigParameters.Command command =  store.getCommands().get(i);

                int endOfCmd = msg.indexOf(" ");
                if (endOfCmd == -1) {
                    endOfCmd = msg.length();
                }
                String cmd = msg.substring(0, endOfCmd);
                if (cmd.contentEquals(command.name)) {
                    if (!checkAuthorization(cmd, username, mod, sub)) {
                        return "";
                    }
                    if (command.disabled) {
                        return "";
                    }
                    String sendTxt;
                    if (msg.contains(" ")) {
                        String param = msg.substring(endOfCmd + 1);
                        sendTxt = command.text.replace("%param%", param);
                    } else {
                        sendTxt = command.text;
                    }
                    if (sendTxt.contains("%param%")) {
                        return cmd + " requires a parameter.";
                    }
                    if (!username.contentEquals(store.getConfiguration().joinedChannel)) {
                        Calendar calendar = Calendar.getInstance();
                        Date now = calendar.getTime();
                        Date cdTime = new Date(command.cdUntil);

                        if (now.before(cdTime)) {
                            return "";
                        }
                        cdTime = new Date(now.getTime() + command.cooldownInSec * 1000L);
                        e.setAttribute("cdUntil", Long.toString(cdTime.getTime()));
                    }
                    return sendTxt;
                    if (!command.sound.isEmpty()) {
                        playSound(command.sound);
                    }
                }
            } catch (DOMException | NumberFormatException e) {
                LOGGER.severe(e.toString());
            }
        }
    }

    /**
     * This method will add a new command to the bot.
     *
     * @param msg The message from the user
     */
    public String addCommand(final String msg) {
        try {
            final String parameters = CommonUtility.getInputParameter("!command-add", msg, true);
            final int separator = parameters.indexOf(" ");
            final String cmd = parameters.substring(0, separator);
            final String txt = parameters.substring(separator + 1);

            if (Commands.getInstance().isReservedCommand(cmd)) {
                return "Failed: [" + cmd + "] is a reserved command.";
            }

            if (!cmd.startsWith("!")) {
                return "Commands should start with an !";
            }

            final boolean added = store.addCommand(cmd, txt);
            if(added) {
                return "Added command [" + cmd + "] : [" + txt + "]";
            }

            else {
                return "Command [" + cmd + "] already exists!";
            }

        } catch (IllegalArgumentException e) {
            LOGGER.warning("Unable to add command");
        }
        return "Syntax: !command-add [!command] [text].";
    }

    /**
     * This method deletes an existing command.
     *
     * @param msg The message from the user
     */
    public String deleteCommand(final String msg) {
        final String cmd = CommonUtility.getInputParameter("!command-delete", msg, true);
        if (Commands.getInstance().isReservedCommand(cmd)) {
            return "Failed: [" + cmd + "] is a reserved command.";
        }

        boolean deleted = store.deleteCommand(cmd);
        if(deleted) {
            return "Command [" + cmd + "] deleted.";
        }

        return "Command [" + cmd + "] not found.";
    }

    /**
     * This function will edit a command.
     *
     * @param msg The message from the user
     */
    public String editCommand(final String msg) {
        try {
            final String parameters = CommonUtility.getInputParameter("!command-edit", msg, true);
            int separator = parameters.indexOf(" ");
            String cmd = parameters.substring(0, separator);
            String txt = parameters.substring(separator + 1);
            if (txt.isEmpty()) {
                throw new IllegalArgumentException();
            }
            if (Commands.getInstance().isReservedCommand(cmd)) {
                return "Failed: [" + cmd + "] is a reserved command.";
            }

            final boolean edited = store.editCommand(cmd, txt);
            if(edited) {
                return "Command [" + cmd + "] changed to " + txt;
            }

            return "Command [" + cmd + "] not found.";
        } catch (IllegalArgumentException e) {
            return "Syntax: !command-edit [!command] [text].";
        }
    }

    /**
     * This method will edit the authority of this command. Typically it grants
     * different access to different people for a particular command.
     *
     * @param username The username of the person getting authority over the
     * command
     *
     * @param msg The message from the user
     */
    public String authorizeCommand(String username, String msg) {
        try {
            final String parameters = CommonUtility.getInputParameter("!command-auth", msg, true);
            int separator = parameters.indexOf(" ");
            String cmd = parameters.substring(0, separator);
            String auth = parameters.substring(separator + 1) + " ";
            if (Commands.getInstance().isReservedCommand(cmd)) {
                if (!username.contentEquals(store.getConfiguration().joinedChannel)) {
                    return "Failed: only the channel owner can edit the auth for reserved commands.";
                }
            }
            if (setUserCmdXMLParam(cmd, "auth", auth, true)) {
                return "Command [" + cmd + "] authorization set to [" + auth + "]";
            }
        } catch (IllegalArgumentException e) {
            return "Syntax: !command-auth [!command] [auth list].";
        }

        return "Syntax: !command-auth [!command] [auth list].";
    }

    /**
     * Appends a sounds file to command node
     *
     * @param msg The message from the user
     */
    public String commandSound(String msg) {
        try {
            final String parameters = CommonUtility.getInputParameter("!command-sound", msg, true);
            int separator = parameters.indexOf(" ");
            String cmd = parameters.substring(0, separator);
            String soundFile = parameters.substring(separator + 1);
            if (soundFile.contentEquals("null")) {
                soundFile = "";
            }
            if (setUserCmdXMLParam(cmd, "sound", soundFile, false)) {
                return "Command [" + cmd + "] set to play sound file [" + soundFile + "]";
            }
        } catch (IllegalArgumentException e) {
            LOGGER.warning("Error detected in command sound");
        }

        return "Syntax: !command-sound [!command] [filename.wav]";
    }

    /**
     * Disable a particular command.
     *
     * @param msg
     * The message from the user
     */
    public String commandDisable(final String msg) {
        try {
            String cmd = CommonUtility.getInputParameter("!command-disable", msg, true);
            if (setUserCmdXMLParam(cmd, "disabled", "true", false)) {
                return "Command " + cmd + " disabled.";
            }
        } catch (IllegalArgumentException e) {
            return "Syntax: !command-disable [!command]";
        }
    }

    public String commandEnable(String msg) {
        try {
            String cmd = CommonUtility.getInputParameter("!command-enable", msg, true);
            if (setUserCmdXMLParam(cmd, "disabled", "false", false)) {
                return "Command " + cmd + " enabled.";
            }
        } catch (IllegalArgumentException e) {
            return "Syntax: !command-enable [!command]";
        }
    }

    public boolean checkAuthorization(String userCommand, String username, boolean mod, boolean sub) {
        String auth = "";
        LOGGER.info("COMMAND: " + userCommand + " USERNAME: " + username + " MOD: " + mod + " SUB: " + sub);
        if (username.contentEquals(store.getConfiguration().joinedChannel)) {
            return true;
        }
        for (int i = 0; i < store.getCommands().size(); i++) {
            final ConfigParameters.Command command = store.getCommands().get(i);
            if (userCommand.contentEquals(command.name)) {
                auth = command.authentication;
                break;
            }
        }
        if (auth.isEmpty()) {
            return false;
        }
        if (auth.toLowerCase().contains("-" + username + " ")) {
            return false;
        }
        if (auth.toLowerCase().contains("+" + username + " ")) {
            return true;
        }
        if ((auth.contains("-m ")) && mod) {
            LOGGER.info("MOD FALSE: ");
            return false;
        }
        if ((auth.contains("+m ")) && mod) {
            LOGGER.info("MOD TRUE: ");
            return true;
        }
        if ((auth.contains("-s ")) && sub) {
            return false;
        }
        if ((auth.contains("+s ")) && sub) {
            return true;
        }
        if (auth.contains("-a ")) {
            return false;
        }
        if (auth.contains("+a ")) {
            return true;
        }
        return false;
    }

    /**
     * Plays sound file based on attached .wav to certain commands within
     * sound="" in XML. This may need to be patched out if the sun.audio API
     * becomes unavailable
     *
     * @param file
     */
    private void playSound(String file) {
        try {
            InputStream is = new FileInputStream(file);
            AudioStream audioStream = new AudioStream(is);
            AudioPlayer.player.start(audioStream);
        } catch (Exception e) {
            LOGGER.severe(e.toString());
        }
    }
}
