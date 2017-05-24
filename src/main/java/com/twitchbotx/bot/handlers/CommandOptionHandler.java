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

    private String[] reservedCommands = {
        "!uptime",
        "!followage",
        "!command-add",
        "!command-delete",
        "!command-edit",
        "!command-auth",
        "!command-repeat",
        "!command-delay",
        "!command-interval",
        "!command-cooldown",
        "!command-sound",
        "!set-msgCache",
        "!set-pyramidResponse",
        "!command-enable",
        "!command-disable",
        "!commands",
        "!cnt-add",
        "!cnt-delete",
        "!cnt-set",
        "!cnt-current",
        "!count",
        "!filter-all",
        "!filter-add",
        "!filter-delete"
    };

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
            int separator = parameters.indexOf(" ");
            String cmd = parameters.substring(0, separator);
            String txt = parameters.substring(separator + 1);
            if (isReservedCommand(cmd)) {
                return "Failed: [" + cmd + "] is a reserved command.";
            }
            for (int i = 0; i < store.getCommands().size(); i++) {
                final ConfigParameters.Command command = store.getCommands().get(i);
                if (cmd.equals(command.name) {
                    return "Command [" + cmd + "] already exists."
                }
            }
            if (!cmd.startsWith("!")) {
                return "Commands should start with an !";
            }

            Element newNode = this.elements.doc.createElement("command");
            newNode.appendChild(this.elements.doc.createTextNode(txt));
            newNode.setAttribute("name", cmd.toLowerCase());
            newNode.setAttribute("auth", "");
            newNode.setAttribute("repeating", "false");
            newNode.setAttribute("initialDelay", "0");
            newNode.setAttribute("interval", "0");
            newNode.setAttribute("cooldown", "0");
            newNode.setAttribute("cdUntil", "");
            newNode.setAttribute("sound", "");
            newNode.setAttribute("disabled", "false");
            this.elements.commands.appendChild(newNode);
            writeXML();

            String confirmation = "Added command [" + cmd + "] : [" + txt + "]";
            return confirmation;
        } catch (IllegalArgumentException e) {
            return "Syntax: !command-add [!command] [text].";
        }
    }

    /**
     * This method deletes an existing command.
     *
     * @param msg The message from the user
     */
    public String deleteCommand(final String msg) {
        final String cmd = CommonUtility.getInputParameter("!command-delete", msg, true);
        if (isReservedCommand(cmd)) {
            return "Failed: [" + cmd + "] is a reserved command.";
        }
        for (int i = 0; i < store.getCommands().size(); i++) {
            final ConfigParameters.Command command = store.getCommands().get(i);
            if (cmd.contentEquals(command.name)) {
                this.elements.commands.removeChild(n);
                writeXML();
                return "Command [" + cmd + "] deleted.";
            }
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
            if (isReservedCommand(cmd)) {
                return "Failed: [" + cmd + "] is a reserved command.";
            }
            for (int i = 0; i < store.getCommands().size(); i++) {
                final ConfigParameters.Command command = store.getCommands().get(i);
                if (cmd.contentEquals(command.name)) {
                    e.setTextContent(txt);
                    writeXML();
                    return "Command [" + cmd + "] changed to " + txt;
                }
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
            if (isReservedCommand(cmd)) {
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
     * Sets status of whether or not a command should automatically run. Keep
     * separate from commandInterval to allow for on/off repeat function while
     * keeping interval info.
     *
     * @param msg The message from the user
     */
    public String repeatCommand(String msg) {
        try {
            final String parameters = CommonUtility.getInputParameter("!command-repeat", msg, true);
            int separator = parameters.indexOf(" ");
            String cmd = parameters.substring(0, separator);
            String repeat = parameters.substring(separator + 1);
            if ((!repeat.contentEquals("true")) && (!repeat.contentEquals("false"))) {
                throw new IllegalArgumentException();
            }
            if (setUserCmdXMLParam(cmd, "repeating", repeat, false)) {

                /*
                ** TODO: Send changes to XML, need to add catch to start timer with TimerManagement class
                 */
                return "Command [" + cmd + "] repeating set to [" + repeat + "]";
            }
        } catch (IllegalArgumentException e) {
            return "Syntax: !command-repeat [!command] [true|false].";
        }

        return "Syntax: !command-repeat [!command] [true|false].";
    }

    /**
     * Adds delay to commands, helps to offset autocommands Useful for
     * autocommands so they don't show up all at once.
     *
     * @param msg The message from the user
     */
    public String commandDelay(String msg) {
        try {
            final String parameters = CommonUtility.getInputParameter("!command-delay", msg, true);
            int separator = parameters.indexOf(" ");
            String cmd = parameters.substring(0, separator);
            long delay = Long.parseLong(parameters.substring(separator + 1));
            if (setUserCmdXMLParam(cmd, "delay", Long.toString(delay), false)) {
                return "Command [" + cmd + "] set to initial delay of [" + delay + "] seconds.";
            }
        } catch (IllegalArgumentException e) {
            return "Syntax: !command-delay [!command] [seconds]";
        }
    }

    /**
     * Sets repeat timer to specified command.
     *
     * @param msg The message from the user
     */
    public String commandInterval(String msg) {
        try {
            final String parameters = CommonUtility.getInputParameter("!command-interval", msg, true);
            int separator = parameters.indexOf(" ");
            String cmd = parameters.substring(0, separator);
            long interval = Long.parseLong(parameters.substring(separator + 1));
            if (setUserCmdXMLParam(cmd, "interval", Long.toString(interval), false)) {
                return "Command [" + cmd + "] set to repeating interval of [" + interval + "] seconds.";
            }
        } catch (IllegalArgumentException e) {
            return "Syntax: !command-interval [!command] [seconds]";
        }
    }

    /**
     * Appends cooldown to specified command.
     *
     * @param msg The message from the user
     */
    public String commandCooldown(String msg) {
        try {
            final String parameters = CommonUtility.getInputParameter("!command-cooldown", msg, true);
            int separator = parameters.indexOf(" ");
            String cmd = parameters.substring(0, separator);
            long cooldown = Long.parseLong(parameters.substring(separator + 1));
            if (setUserCmdXMLParam(cmd, "cooldown", Long.toString(cooldown), false)) {
                return "Command [" + cmd + "] set to cooldown of [" + cooldown + "] seconds.";
            }
            setUserCmdXMLParam(cmd, "cdUntil", "", false);
        } catch (IllegalArgumentException e) {
            return "Syntax: !command-cooldown [!command] [seconds]";
        }
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
                return("Command [" + cmd + "] set to play sound file [" + soundFile + "]";
            }
        } catch (IllegalArgumentException e) {
            return "Syntax: !command-sound [!command] [filename.wav]";
        }
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

    public String setMessageCacheSize(String msg) {
        try {
            String value = CommonUtility.getInputParameter("!set-msgCache", msg, true);

            int c = Integer.parseInt(value);
            if ((c < 2) || (c > 100)) {
                throw new IllegalArgumentException();
            }
            setConfigXML("recentMessageCacheSize", value);
            return "Cache size set to [" + value + "] messages for pyramid detection.");
        } catch (IllegalArgumentException e) {
            return "Syntax: !set-msgcache [2-100]";
        }
    }

    public String setPyramidResponse(String msg) {
        try {
            String value = CommonUtility.getInputParameter("!set-pyramidResponse", msg, false);
            setConfigXML("pyramidResponse", value);

            return "Pyramid response set to [" + value + "]";
        } catch (IllegalArgumentException e) {
            return "Syntax: !set-pyramidResponse [msg]";
        }
    }

    /*
    ** !commands shows all commands available to user ie mod, sub, username
    ** @param username, mod status, sub status
    ** @return none
     */
    public String commands(String user, boolean mod, boolean sub) {
        String auth = "";
        if (user.contentEquals(store.getConfiguration().joinedChannel)) {
            return "Command list too long for chat, see commands text file in main bot folder.";
            writeCommandFile();
        }
        /*for (int i = 0; i < this.elements.commandNodes.getLength(); i++) {
            Node n = this.elements.commandNodes.item(i);
            Element cmdXmlNode = (Element) n;
            auth = cmdXmlNode.getAttribute("auth");

        }
        try {
            String[] commands = new String[elements.commandNodes.getLength()];
            for (int i = 0; i < this.elements.commandNodes.getLength(); i++) {
                Node n = this.elements.commandNodes.item(i);
                Element e = (Element) n;
                if (checkAuthorization(e.getAttribute("name"), user, mod, sub)) {
                    if (!e.getAttribute("auth").contains("-o ")) {
                        commands[i] = e.getAttribute("name");
                    }
                }
            }
            StringBuilder sb = new StringBuilder();
            for (int j = 0; j < commands.length; j++) {
                if (commands[j] != null) {
                    if (j > 0) {
                        sb.append(", ");
                    }
                    sb.append(commands[j]);
                }
            }
            sendMessage("@" + user + ", commands available to you: " + sb.toString());
        } catch (IllegalArgumentException e) {
            sendMessage("No commands found.");
        }*/
    }

    public void writeCommandFile() {
        try {
            String[] commands = new String[store.getCommands().size()];
            for (int i = 0; i < store.getCommands().size(); i++) {
                final ConfigParameters.Command command = store.getCommands().get(i);
                commands[i] = command.name;
            }
            FileWriter fw = new FileWriter("commands.txt");
            for (int j = 0; j < commands.length; j++) {
                fw.write(commands[j] + "\n");
                LOGGER.info(commands[j]);
            }
            fw.close();
        } catch (IOException e) {
            LOGGER.info(e.toString());
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

    /**
     * This method checks whether a command has already been reserved.
     *
     * @param command The command to check for.
     *
     * @return True - the command has been reserved and we shouldn't override it
     * False - the command has not been reserved and we can override it
     */
    private boolean isReservedCommand(final String command) {
        for (String reservedCommand : reservedCommands) {
            if (command.contentEquals(reservedCommand)) {
                return true;
            }
        }
        return false;
    }

    /**
     * This method writes to a XML file the entire document.
     */
    private void writeXML() {
        try {
            File configFile = new File("kfbot.xml");
            TransformerFactory tFactory = TransformerFactory.newInstance();
            Transformer transformer = tFactory.newTransformer();
            DOMSource source = new DOMSource(this.elements.doc);
            StreamResult result = new StreamResult(configFile);
            transformer.transform(source, result);
        } catch (TransformerException e) {
            LOGGER.severe(e.toString());
        }
    }

    private void setConfigXML(String node, String value) {
        Node n = this.elements.configNode.getElementsByTagName(node).item(0);
        Element el = (Element) n;
        el.setTextContent(value);
        writeXML();
    }

    private boolean setUserCmdXMLParam(
            String cmd, String attrib, String value, boolean allowReservedCmds) {
        if (!allowReservedCmds && isReservedCommand(cmd)) {
            sendMessage("Failed: " + cmd + " is a reserved command.");
            return false;
        }
        for (int i = 0; i < store.getCommands().size(); i++) {
            final ConfigParameters.Command command = store.getCommands().get(i);
            if (cmd.contentEquals(command.name)) {
                el.setAttribute(attrib, value);
                writeXML();
                return true;
            }
        }
        sendMessage("Command " + cmd + " not found.");
        return false;
    }
}
