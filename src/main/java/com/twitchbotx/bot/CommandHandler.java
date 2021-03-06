package com.twitchbotx.bot;

import java.io.BufferedReader;
import java.io.BufferedWriter;
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
import org.w3c.dom.DOMException;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import sun.audio.AudioPlayer;
import sun.audio.AudioStream;
import javax.sound.sampled.*;
import java.time.LocalDate;
import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import javax.xml.transform.OutputKeys;

/**
 * This class is a command handler for most of the common commands in this bot.
 */
public final class CommandHandler {

    /*
        ** If command = !filter-all & username is valid & mod requirement is met per XML & sub requirement is met per XML
        ** Then create a moderationHandler and send "trailing" to the handleTool method
        ** filter, command, set broadcaster only
        ** See XML for other requirements
     */
    private static final Logger LOGGER = Logger.getLogger(TwitchBotX.class.getSimpleName());

    private final ConfigParser.Elements elements;

    private final PrintStream outstream;

    private final List<CachedMessage> recentMessages = new ArrayList<>();

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
        "!command-add-sub",
        "!command-edit-sub",
        "!command-delete-sub",
        "!command-cooldown-sub",
        "!cooldown-auth-sub",
        "!cnt-add",
        "!cnt-delete",
        "!cnt-set",
        "!cnt-current",
        "!count",
        "!highlight",
        "!filter-all",
        "!filter-add",
        "!filter-delete",
        "!filter-reason"};

    /**
     * A simple inner class for storing cached messages.
     *
     * It simply stores the username and the message to prevent pyramids.
     */
    public static class CachedMessage {

        public String user;
        public String msg;

        public CachedMessage(final String username, final String message) {
            this.user = username;
            this.msg = message;
            //System.out.println(this.user + this.msg + " CACHED MESSAGES");
        }

        public String getUser() {
            return user;
        }

        public String getMsg() {
            return msg;
        }

        @Override
        public String toString() {
            return "CachedMessage{" + "user=" + user + ", msg=" + msg + '}';
        }
    }

    /**
     * This is a simple constructor for the command handler.
     *
     * It will use elements to write/rewrite the XML as storage.
     *
     * @param elements The element references to the XML data
     *
     * @param stream The output stream to the Twitch API
     */
    public CommandHandler(final ConfigParser.Elements elements,
            final PrintStream stream) {
        this.elements = elements;
        this.outstream = stream;
    }

    public void parseForUserCommands(String msg,
            String username,
            boolean mod,
            boolean sub) {

        for (int i = 0; i < this.elements.commandNodes.getLength(); i++) {
            try {
                Node n = this.elements.commandNodes.item(i);
                Element e = (Element) n;

                int endOfCmd = msg.indexOf(" ");
                if (endOfCmd == -1) {
                    endOfCmd = msg.length();
                }
                String cmd = msg.substring(0, endOfCmd);
                cmd = cmd.toLowerCase();
                if (cmd.contentEquals(e.getAttribute("name"))) {
                    if (!checkAuthorization(cmd, username, mod, sub)) {
                        return;
                    }
                    if (Boolean.parseBoolean(e.getAttribute("disabled"))) {
                        return;
                    }
                    String sendTxt;
                    if (msg.contains(" ")) {
                        String param = msg.substring(endOfCmd + 1);
                        sendTxt = e.getTextContent().replace("%param%", param);
                    } else {
                        sendTxt = e.getTextContent();
                    }
                    if (sendTxt.contains("%param%")) {
                        sendMessage(cmd + " requires a parameter.");
                        return;
                    }
                    if (!username.contentEquals(this.elements.configNode.getElementsByTagName("myChannel").item(0).getTextContent())) {
                        Calendar calendar = Calendar.getInstance();
                        Date now = calendar.getTime();
                        Date cdTime = new Date(0L);
                        if (!e.getAttribute("cdUntil").isEmpty()) {
                            cdTime = new Date(Long.parseLong(e.getAttribute("cdUntil")));
                        }
                        if (now.before(cdTime)) {
                            return;
                        }
                        cdTime = new Date(now.getTime() + Long.parseLong(e.getAttribute("cooldown")) * 1000L);
                        e.setAttribute("cdUntil", Long.toString(cdTime.getTime()));
                    }
                    sendMessage(sendTxt);
                    if (!e.getAttribute("sound").isEmpty()) {
                        playSound(e.getAttribute("sound"));
                    }
                }
            } catch (DOMException | NumberFormatException e) {
                LOGGER.severe(e.toString());
            }
        }
        // Begin checking for subcommands if regular commands fail
        for (int i = 0; i < this.elements.subCommandNodes.getLength(); i++) {
            try {
                Node n = this.elements.subCommandNodes.item(i);
                Element e = (Element) n;

                int endOfCmd = msg.indexOf(" ");
                if (endOfCmd == -1) {
                    endOfCmd = msg.length();
                }
                String cmd = msg.substring(0, endOfCmd);
                cmd = cmd.toLowerCase();
                if (cmd.contentEquals(e.getAttribute("name"))) {
                    if (!checkAuthorization(cmd, username, mod, sub)) {
                        return;
                    }
                    if (Boolean.parseBoolean(e.getAttribute("disabled"))) {
                        return;
                    }
                    String sendTxt;
                    if (msg.contains(" ")) {
                        String param = msg.substring(endOfCmd + 1);
                        sendTxt = e.getTextContent().replace("%param%", param);
                    } else {
                        sendTxt = e.getTextContent();
                    }
                    if (sendTxt.contains("%param%")) {
                        sendMessage(cmd + " requires a parameter.");
                        return;
                    }
                    if (!username.contentEquals(this.elements.configNode.getElementsByTagName("myChannel").item(0).getTextContent())) {
                        Calendar calendar = Calendar.getInstance();
                        Date now = calendar.getTime();
                        Date cdTime = new Date(0L);
                        if (!e.getAttribute("cdUntil").isEmpty()) {
                            cdTime = new Date(Long.parseLong(e.getAttribute("cdUntil")));
                        }
                        if (now.before(cdTime)) {
                            return;
                        }
                        cdTime = new Date(now.getTime() + Long.parseLong(e.getAttribute("cooldown")) * 1000L);
                        e.setAttribute("cdUntil", Long.toString(cdTime.getTime()));
                    }
                    sendMessage(sendTxt);
                    if (!e.getAttribute("sound").isEmpty()) {
                        playSound(e.getAttribute("sound"));
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
    public void addCmd(final String msg) {
        try {
            String parameters = getInputParameter("!command-add", msg, true);
            int separator = parameters.indexOf(" ");
            String cmd = parameters.substring(0, separator);
            String txt = parameters.substring(separator + 1);
            if (isReservedCommand(cmd)) {
                sendMessage("Failed: [" + cmd + "] is a reserved command.");
                return;
            }
            // Check for duplicate regular commands
            for (int i = 0; i < this.elements.commandNodes.getLength(); i++) {
                Node n = this.elements.commandNodes.item(i);
                Element e = (Element) n;
                if (cmd.contentEquals(e.getAttribute("name"))) {
                    sendMessage("Command [" + cmd + "] already exists.");
                    return;
                }
            }
            // Check for duplicate sub commands
            for (int i = 0; i < this.elements.subCommandNodes.getLength(); i++) {
                Node n = this.elements.subCommandNodes.item(i);
                Element e = (Element) n;
                if (cmd.contentEquals(e.getAttribute("name"))) {
                    sendMessage("Command [" + cmd + "] already exists.");
                    return;
                }
            }

            if (!cmd.startsWith("!")) {
                sendMessage("Commands should start with an !");
                return;
            }

            Element newNode = this.elements.doc.createElement("command");
            newNode.appendChild(this.elements.doc.createTextNode(txt));
            newNode.setAttribute("name", cmd.toLowerCase());
            newNode.setAttribute("auth", " ");
            newNode.setAttribute("repeating", "false");
            newNode.setAttribute("initialDelay", "0");
            newNode.setAttribute("interval", "0");
            newNode.setAttribute("cooldown", "10");
            newNode.setAttribute("cdUntil", "");
            newNode.setAttribute("sound", "");
            newNode.setAttribute("disabled", "false");

            this.elements.commands.appendChild(newNode);
            writeXML();
            String confirmation = "Added command [" + cmd + "] : [" + txt + "]";
            sendMessage(confirmation);
        } catch (IllegalArgumentException e) {
            sendMessage("Syntax: !command-add [!command] [text].");
        }
    }

    /**
     * This method deletes an existing command.
     *
     * @param msg The message from the user
     */
    public void delCmd(final String msg) {
        String cmd = getInputParameter("!command-delete", msg, true);
        cmd = cmd.toLowerCase();
        if (isReservedCommand(cmd)) {
            sendMessage("Failed: [" + cmd + "] is a reserved command.");
            return;
        }
        for (int i = 0; i < this.elements.commandNodes.getLength(); i++) {
            Node n = this.elements.commandNodes.item(i);
            Element e = (Element) n;
            if (cmd.contentEquals(e.getAttribute("name"))) {
                this.elements.commands.removeChild(n);
                writeXML();
                sendMessage("Command [" + cmd + "] deleted.");
                return;
            }
        }
        sendMessage("Command [" + cmd + "] not found.");
    }

    /**
     * This function will edit a command.
     *
     * @param msg The message from the user
     */
    public void editCmd(final String msg) {
        try {
            String parameters = getInputParameter("!command-edit", msg, true);
            int separator = parameters.indexOf(" ");
            String cmd = parameters.substring(0, separator);
            cmd = cmd.toLowerCase();
            String txt = parameters.substring(separator + 1);
            if (txt.isEmpty()) {
                throw new IllegalArgumentException();
            }
            if (isReservedCommand(cmd)) {
                sendMessage("Failed: [" + cmd + "] is a reserved command.");
                return;
            }
            for (int i = 0; i < this.elements.commandNodes.getLength(); i++) {
                Node n = this.elements.commandNodes.item(i);
                Element e = (Element) n;
                if (cmd.contentEquals(e.getAttribute("name"))) {
                    e.setTextContent(txt);
                    writeXML();
                    sendMessage("Command [" + cmd + "] changed to " + txt);
                    return;
                }
            }
            sendMessage("Command [" + cmd + "] not found.");
        } catch (IllegalArgumentException e) {
            sendMessage("Syntax: !command-edit [!command] [text].");
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
    public void authCmd(String username, String msg) {
        try {
            String parameters = getInputParameter("!command-auth", msg, true);
            int separator = parameters.indexOf(" ");
            String cmd = parameters.substring(0, separator);
            cmd = cmd.toLowerCase();
            String auth = parameters.substring(separator + 1) + " ";
            if (isReservedCommand(cmd)) {
                if (!username.contentEquals(this.elements.configNode.getElementsByTagName("myChannel").item(0).getTextContent())) {
                    sendMessage("Failed: only the channel owner can edit the auth for reserved commands.");
                    return;
                }
            }
            if (setUserCmdXMLParam(cmd, "auth", auth, true)) {
                sendMessage("Command [" + cmd + "] authorization set to [" + auth + "]");
            }
        } catch (IllegalArgumentException e) {
            sendMessage("Syntax: !command-auth [!command] [auth list].");
        }
    }

    /**
     * Sets status of whether or not a command should automatically run. Keep
     * separate from cmdInterval to allow for on/off repeat function while
     * keeping interval info.
     *
     * @param msg The message from the user
     */
    public void repeatingCmd(String msg) {
        try {
            String parameters = getInputParameter("!command-repeat", msg, true);
            int separator = parameters.indexOf(" ");
            String cmd = parameters.substring(0, separator);
            cmd = cmd.toLowerCase();
            String repeat = parameters.substring(separator + 1);
            if ((!repeat.contentEquals("true")) && (!repeat.contentEquals("false"))) {
                throw new IllegalArgumentException();
            }
            if (setUserCmdXMLParam(cmd, "repeating", repeat, false)) {
                /*
                ** TODO: Send changes to XML, need to add catch to start timer with TimerManagement class
                 */
                sendMessage("Command [" + cmd + "] repeating set to [" + repeat + "]");
            }
        } catch (IllegalArgumentException e) {
            sendMessage("Syntax: !command-repeat [!command] [true|false].");
        }
    }

    /**
     * Adds delay to commands, helps to offset autocommands Useful for
     * autocommands so they don't show up all at once.
     *
     * @param msg The message from the user
     */
    public void cmdDelay(String msg) {
        try {
            String parameters = getInputParameter("!command-delay", msg, true);
            int separator = parameters.indexOf(" ");
            String cmd = parameters.substring(0, separator);
            cmd = cmd.toLowerCase();
            long delay = Long.parseLong(parameters.substring(separator + 1));
            if (setUserCmdXMLParam(cmd, "delay", Long.toString(delay), false)) {
                sendMessage("Command [" + cmd + "] set to initial delay of [" + delay + "] seconds.");
            }
        } catch (IllegalArgumentException e) {
            sendMessage("Syntax: !command-delay [!command] [seconds]");
        }
    }

    /**
     * Sets repeat timer to specified command.
     *
     * @param msg The message from the user
     */
    public void cmdInterval(String msg) {
        try {
            String parameters = getInputParameter("!command-interval", msg, true);
            int separator = parameters.indexOf(" ");
            String cmd = parameters.substring(0, separator);
            cmd = cmd.toLowerCase();
            long interval = Long.parseLong(parameters.substring(separator + 1));
            if (setUserCmdXMLParam(cmd, "interval", Long.toString(interval), false)) {
                sendMessage("Command [" + cmd + "] set to repeating interval of [" + interval + "] seconds.");
            }
        } catch (IllegalArgumentException e) {
            sendMessage("Syntax: !command-interval [!command] [seconds]");
        }
    }

    /**
     * Appends cooldown to specified command.
     *
     * @param msg The message from the user
     */
    public void cmdCooldown(String msg) {
        try {
            String parameters = getInputParameter("!command-cooldown", msg, true);
            int separator = parameters.indexOf(" ");
            String cmd = parameters.substring(0, separator);
            cmd = cmd.toLowerCase();
            long cooldown = Long.parseLong(parameters.substring(separator + 1));
            if (setUserCmdXMLParam(cmd, "cooldown", Long.toString(cooldown), false)) {
                sendMessage("Command [" + cmd + "] set to cooldown of [" + cooldown + "] seconds.");
            }
            setUserCmdXMLParam(cmd, "cdUntil", "", false);
        } catch (IllegalArgumentException e) {
            sendMessage("Syntax: !command-cooldown [!command] [seconds]");
        }
    }

    /**
     * Appends a sounds file to command node
     *
     * @param msg The message from the user
     */
    public void cmdSound(String msg) {
        try {
            String parameters = getInputParameter("!command-sound", msg, true);
            int separator = parameters.indexOf(" ");
            String cmd = parameters.substring(0, separator);
            cmd = cmd.toLowerCase();
            String soundFile = parameters.substring(separator + 1);
            if (soundFile.contentEquals("null")) {
                soundFile = "";
            }
            if (setUserCmdXMLParam(cmd, "sound", soundFile, false)) {
                sendMessage("Command [" + cmd + "] set to play sound file [" + soundFile + "]");
            }
        } catch (IllegalArgumentException e) {
            sendMessage("Syntax: !command-sound [!command] [filename.wav]");
        }
    }

    /*
    ** Start sub benefit commands
    ** utilizes !command-add-sub !command-edit-sub !command-delete-sub !command-auth-sub !command-cooldown-sub
    ** all checks for duplicate commands must cross between sub specific and regular commands
     */
    public void addSubCmd(String msg) {
        try {
            String parameters = getInputParameter("!command-add-sub", msg, true);
            int separator = parameters.indexOf(" ");
            String subscriber = parameters.substring(0, separator);
            int cmdEnd = parameters.indexOf(" ", separator + 1);
            String cmd = parameters.substring(separator + 1, cmdEnd);
            int txtSeparator = parameters.indexOf(" ", separator + 1);
            String txt = parameters.substring(txtSeparator);
            String subAuth = ("+" + subscriber + " ");
            if (isReservedCommand(cmd)) {
                sendMessage("Failed: [" + cmd + "] is a reserved command.");
                return;
            }

            // Check for duplicate sub commands
            for (int i = 0; i < this.elements.subCommandNodes.getLength(); i++) {
                Node n = this.elements.subCommandNodes.item(i);
                Element e = (Element) n;
                if (cmd.contentEquals(e.getAttribute("name"))) {
                    sendMessage("Command [" + cmd + "] already exists.");
                    return;
                } else if (subAuth.contentEquals(e.getAttribute("auth"))) {
                    sendMessage("Subscriber " + subscriber + " already has command [" + cmd + "]");
                    return;
                }
            }
            // Check to make sure new command doesn't overlap with regular commands
            for (int i = 0; i < this.elements.commandNodes.getLength(); i++) {
                Node n = this.elements.commandNodes.item(i);
                Element e = (Element) n;
                if (cmd.contentEquals(e.getAttribute("name"))) {
                    sendMessage("Command [" + cmd + "] already exists.");
                    return;
                }
            }
            if (!cmd.contains("!")) {
                sendMessage("Commands should start with an !");
                return;
            }

            Element newNode = this.elements.doc.createElement("subcommand");
            newNode.appendChild(this.elements.doc.createTextNode(txt));
            newNode.setAttribute("name", cmd.toLowerCase());
            newNode.setAttribute("auth", subAuth);
            newNode.setAttribute("repeating", "false");
            newNode.setAttribute("initialDelay", "0");
            newNode.setAttribute("interval", "0");
            newNode.setAttribute("cooldown", "10");
            newNode.setAttribute("cdUntil", "");
            newNode.setAttribute("sound", "");
            newNode.setAttribute("disabled", "false");

            this.elements.subCommands.appendChild(newNode);
            writeXML();
            String confirmation = "Added subcommand [" + cmd + "] for subscriber: [" + subscriber + "] : [" + txt + "]";
            sendMessage(confirmation);
        } catch (IllegalArgumentException e) {
            sendMessage("Syntax: !command-add-sub [subscriber] [!command] [text].");
        } catch (StringIndexOutOfBoundsException e) {
            sendMessage("Syntax: !command-add-sub [subscriber] [!command] [text].");
        }
    }

    /**
     * This method deletes an existing command.
     *
     * @param msg The message from the user
     */
    public void subDelCmd(final String msg) {
        String cmd = getInputParameter("!command-delete-sub", msg, true);
        cmd = cmd.toLowerCase();
        if (isReservedCommand(cmd)) {
            sendMessage("Failed: [" + cmd + "] is a reserved command.");
            return;
        }
        for (int i = 0; i < this.elements.subCommandNodes.getLength(); i++) {
            Node n = this.elements.subCommandNodes.item(i);
            Element e = (Element) n;
            if (cmd.contentEquals(e.getAttribute("name"))) {
                this.elements.subCommands.removeChild(n);
                writeXML();
                sendMessage("Command [" + cmd + "] deleted.");
                return;
            }
        }
        sendMessage("Command [" + cmd + "] not found.");
    }

    /**
     * This function will edit a command.
     *
     * @param msg The message from the user
     */
    public void subEditCmd(final String msg) {
        try {
            String parameters = getInputParameter("!command-edit-sub", msg, true);
            int separator = parameters.indexOf(" ");
            String cmd = parameters.substring(0, separator);
            cmd = cmd.toLowerCase();
            String txt = parameters.substring(separator + 1);
            if (txt.isEmpty()) {
                throw new IllegalArgumentException();
            }
            if (isReservedCommand(cmd)) {
                sendMessage("Failed: [" + cmd + "] is a reserved command.");
                return;
            }
            for (int i = 0; i < this.elements.subCommandNodes.getLength(); i++) {
                Node n = this.elements.subCommandNodes.item(i);
                Element e = (Element) n;
                if (cmd.contentEquals(e.getAttribute("name"))) {
                    e.setTextContent(txt);
                    writeXML();
                    sendMessage("Command [" + cmd + "] changed to " + txt);
                    return;
                }
            }
            sendMessage("Command [" + cmd + "] not found.");
        } catch (IllegalArgumentException e) {
            sendMessage("Syntax: !command-edit [!command] [text].");
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
    public void subAuthCmd(String username, String msg) {
        try {
            String parameters = getInputParameter("!command-auth-sub", msg, true);
            int separator = parameters.indexOf(" ");
            String cmd = parameters.substring(0, separator);
            cmd = cmd.toLowerCase();
            String auth = parameters.substring(separator + 1) + " ";
            if (isReservedCommand(cmd)) {
                if (!username.contentEquals(this.elements.configNode.getElementsByTagName("myChannel").item(0).getTextContent())) {
                    sendMessage("Failed: only the channel owner can edit the auth for reserved commands.");
                    return;
                }
            }
            if (setSubCmdXMLParam(cmd, "auth", auth, true)) {
                sendMessage("Command [" + cmd + "] authorization set to [" + auth + "]");
            }
        } catch (IllegalArgumentException e) {
            sendMessage("Syntax: !command-auth [!command] [auth list].");
        }
    }

    /**
     * Appends cooldown to specified command.
     *
     * @param msg The message from the user
     */
    public void subCmdCooldown(String msg) {
        try {
            String parameters = getInputParameter("!command-cooldown-sub", msg, true);
            int separator = parameters.indexOf(" ");
            String cmd = parameters.substring(0, separator);
            cmd = cmd.toLowerCase();
            long cooldown = Long.parseLong(parameters.substring(separator + 1));
            if (setSubCmdXMLParam(cmd, "cooldown", Long.toString(cooldown), false)) {
                sendMessage("Command [" + cmd + "] set to cooldown of [" + cooldown + "] seconds.");
            }
            setSubCmdXMLParam(cmd, "cdUntil", "", false);
        } catch (IllegalArgumentException e) {
            sendMessage("Syntax: !command-cooldown [!command] [seconds]");
        }
    }

        public void cmdSubSound(String msg) {
        try {
            String parameters = getInputParameter("!command-sound-sub", msg, true);
            int separator = parameters.indexOf(" ");
            String cmd = parameters.substring(0, separator);
            cmd = cmd.toLowerCase();
            String soundFile = parameters.substring(separator + 1);
            if (soundFile.contentEquals("null")) {
                soundFile = "";
            }
            if (setSubCmdXMLParam(cmd, "sound", soundFile, false)) {
                sendMessage("Command [" + cmd + "] set to play sound file [" + soundFile + "]");
            }
        } catch (IllegalArgumentException e) {
            sendMessage("Syntax: !command-sound-sub [!command] [filename.wav]");
        }
    }
    
    
    
    
    /*
** This creates the URL = api.twitch.tv/kraken with desired streamer name("myChannel") from kfbot1.0.xml
** Opens a connection, begins reading using BufferedReader brin, builds a String response based on API reply
** Once response is done building, checks for "stream\:null" response - this means stream is not live
** Creates Strings to hold content placed between int "bi" and int "ei" as per their defined index
** 
     */
    public void uptimeHandler() {
        if (!uptime().equals("0")) {
            sendMessage("Stream have been up for " + uptime() + ".");
        } else {
            sendMessage("Stream is not currently live.");
        }
    }

    public String uptime() {
        try {
            String statusURL = this.elements.configNode.getElementsByTagName("twitchStreamerStatus").item(0).getTextContent();
            statusURL = statusURL.replaceAll("#streamer", this.elements.configNode.getElementsByTagName("myChannel").item(0).getTextContent());
            URL url = new URL(statusURL);
            URLConnection con = (URLConnection) url.openConnection();
            con.setRequestProperty("Accept", "application/vnd.twitchtv.v3+json");
            con.setRequestProperty("Authorization", this.elements.configNode.getElementsByTagName("botOAUTH").item(0).getTextContent());
            con.setRequestProperty("Client-ID", this.elements.configNode.getElementsByTagName("botClientID").item(0).getTextContent());
            BufferedReader brin = new BufferedReader(new InputStreamReader(con.getInputStream()));
            StringBuilder response = new StringBuilder();
            String inputLine;
            while ((inputLine = brin.readLine()) != null) {
                response.append(inputLine);
            }
            brin.close();
            if (response.toString().contains("\"stream\":null")) {
                return "0";
            } else {
                int bi = response.toString().indexOf("\"created_at\":") + 14;
                int ei = response.toString().indexOf("\",", bi);
                String s = response.toString().substring(bi, ei);
                Instant start = Instant.parse(s);
                Instant current = Instant.now();
                long gap = ChronoUnit.MILLIS.between(start, current);
                String upT = String.format("%d hours, %d minutes, %d seconds", new Object[]{
                    TimeUnit.MILLISECONDS.toHours(gap),
                    TimeUnit.MILLISECONDS.toMinutes(gap) - TimeUnit.HOURS.toMinutes(TimeUnit.MILLISECONDS.toHours(gap)),
                    TimeUnit.MILLISECONDS.toSeconds(gap) - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(gap))
                });
                return upT;
            }
        } catch (Exception e) {
            LOGGER.severe(e.toString());
        }
        /* Timer coolDown = new Timer();
        
    }
        else {
            ;
        }*/
        return "0";
    }

    public void highlight() {

        String uptime = uptime();
        if (!uptime.equals("0")) {
            sendMessage("Highlight marked suggested added at " + uptime);
            try {
                String googleSheetID = this.elements.configNode.getElementsByTagName("googleSheetID").item(0).getTextContent();
                String sheetAPI = "https://sheets.googleapis.com/v4/spreadsheets/" + googleSheetID + "/values/{range}:append";
                URL url = new URL(sheetAPI);
                URLConnection con = (URLConnection) url.openConnection();
                con.setRequestProperty("range", "M6:M20");
                con.setRequestProperty("majorDimension", "COLUMNS");
                BufferedReader sheetIn = new BufferedReader(new InputStreamReader(con.getInputStream()));
                StringBuilder values = new StringBuilder();
                String valueLines;
                while ((valueLines = sheetIn.readLine()) != null) {
                    values.append(valueLines);
                }
                sheetIn.close();
            } catch (IOException e) {
                LOGGER.severe(e.toString());
            }
        } else {
            sendMessage("Stream is not currently live.");
        }
    }

    /*
** This delivers the original follow date 
**
** @param user
** @return formated date of created_at per https://api.twitch.tv/kraken/users/test_user1/follows/channels/test_channel
**
     */
    public void followage(String user) {
        try {
            String followURL = this.elements.configNode.getElementsByTagName("twitchFollowage").item(0).getTextContent();
            followURL = followURL.replaceAll("#user", user);
            followURL = followURL.replaceAll("#streamer", this.elements.configNode.getElementsByTagName("myChannel").item(0).getTextContent());
            URL url = new URL(followURL);
            URLConnection con = (URLConnection) url.openConnection();
            con.setRequestProperty("Accept", "application/vnd.twitchtv.v3+json");
            con.setRequestProperty("Authorization", this.elements.configNode.getElementsByTagName("botOAUTH").item(0).getTextContent());
            con.setRequestProperty("Client-ID", this.elements.configNode.getElementsByTagName("botClientID").item(0).getTextContent());
            BufferedReader brin = new BufferedReader(new InputStreamReader(con.getInputStream()));
            StringBuilder response = new StringBuilder();
            String inputLine;
            while ((inputLine = brin.readLine()) != null) {
                response.append(inputLine);
            }

            int bi = response.toString().indexOf("\"created_at\":") + 14;
            int ei = response.toString().indexOf("T", bi);

            String s = response.toString().substring(bi, ei);
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
            LocalDate begin = LocalDate.parse(s, formatter);
            LocalDate today = LocalDate.now();
            long gap = ChronoUnit.DAYS.between(begin, today);
            if (gap != -1) {
                sendMessage(user + " has been following for " + gap + " days. Starting on " + begin + ".");

                brin.close();
            } else if (gap < 0) {
                sendMessage(user + " just started following today, " + today + "!");
                brin.close();
            }
        } catch (FileNotFoundException e) {
            if (user.equalsIgnoreCase(this.elements.configNode.getElementsByTagName("myChannel").item(0).getTextContent())) {
                sendMessage("Broadcasters cannot follow their own stream.");
            } else {
                sendMessage("User " + user + "  is not following " + this.elements.configNode.getElementsByTagName("myChannel").item(0).getTextContent());
            }
        } catch (Exception e) {
            LOGGER.severe(e.toString());
        }
    }

    public void setMsgCacheSize(String msg) {
        try {
            String value = getInputParameter("!set-msgCache", msg, true);

            int c = Integer.parseInt(value);
            if ((c < 2) || (c > 100)) {
                throw new IllegalArgumentException();
            }
            setConfigXML("recentMessageCacheSize", value);

            sendMessage("Cache size set to [" + value + "] messages for pyramid detection.");
        } catch (IllegalArgumentException e) {
            sendMessage("Syntax: !set-msgcache [2-100]");
        }
    }

    public void setPyramidResponse(String msg) {
        try {
            String value = getInputParameter("!set-pyramidResponse", msg, false);

            setConfigXML("pyramidResponse", value);

            sendMessage("Pyramid response set to [" + value + "]");
        } catch (IllegalArgumentException e) {
            sendMessage("Syntax: !set-pyramidResponse [msg]");
        }
    }

    public void disableCmd(String msg) {
        try {
            String cmd = getInputParameter("!command-disable", msg, true);
            if (setUserCmdXMLParam(cmd, "disabled", "true", false)) {
                sendMessage("Command " + cmd + " disabled.");
            }
        } catch (IllegalArgumentException e) {
            sendMessage("Syntax: !command-disable [!command]");
        }
    }

    public void enableCmd(String msg) {
        try {
            String cmd = getInputParameter("!command-enable", msg, true);
            if (setUserCmdXMLParam(cmd, "disabled", "false", false)) {
                sendMessage("Command " + cmd + " enabled.");
            }
        } catch (IllegalArgumentException e) {
            sendMessage("Syntax: !command-enable [!command]");
        }
    }

    /*
    ** !commands shows all commands available to user ie mod, sub, username
    ** @param username, mod status, sub status
    ** @return none
     */
    public void commands(String user, boolean mod, boolean sub) {
        String auth = "";
        if (user.contentEquals(this.elements.configNode.getElementsByTagName("myChannel").item(0).getTextContent())) {
            sendMessage("Command list too long for chat, see commands text file in main bot folder.");
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
            String[] commands = new String[elements.commandNodes.getLength()];
            for (int i = 0; i < this.elements.commandNodes.getLength(); i++) {
                Node n = this.elements.commandNodes.item(i);
                Element e = (Element) n;
                commands[i] = e.getAttribute("name");
            }
            FileWriter fw = new FileWriter("commands.txt");
            for (int j = 0; j < commands.length; j++) {
                fw.write(commands[j] + "\n");
                //LOGGER.info(commands[j]);
            }
            fw.close();
        } catch (IOException e) {
            LOGGER.info(e.toString());
        }
    }

    /* 
    ** Allows for counters to be added, deleted, set, added to, and all totals calls
    **
    ** return name and value   
     */
    public void cntAdd(String msg) {
        try {
            String name = getInputParameter("!cnt-add", msg, true);
            for (int i = 0; i < this.elements.counterNodes.getLength(); i++) {
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
        for (int i = 0; i < this.elements.counterNodes.getLength(); i++) {
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
            for (int i = 0; i < this.elements.counterNodes.getLength(); i++) {
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
        for (int i = 0; i < this.elements.counterNodes.getLength(); i++) {
            Node n = this.elements.counterNodes.item(i);
            Element e = (Element) n;
            if (name.contentEquals(e.getAttribute("name"))) {
                sendMessage("Counter [" + name + "] is currently [" + e.getTextContent() + "]");
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
            for (int i = 0; i < this.elements.counterNodes.getLength(); i++) {
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
            String[][] counters = new String[Integer.decode(elements.configNode.getElementsByTagName("numberOfCounters").item(0).getTextContent())][2];
            int count = 0;
            for (int i = 0; i < this.elements.counterNodes.getLength(); i++) {
                Node n = this.elements.counterNodes.item(i);
                Element e = (Element) n;
                counters[i][0] = e.getAttribute("name");
                counters[i][1] = e.getTextContent();
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

    /*
    ** Methods to add and remove moderator filters
    ** 
     */
    public void filterAll(String msg, String user) {
        try {
            String[] filters = new String[elements.filterNodes.getLength()];
            for (int i = 0; i < this.elements.filterNodes.getLength(); i++) {
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
            String filter = "";
            int filterNameStart = msg.indexOf("!fitler-add") + 13;
            int filterNameEnd;
            String reason;
            if (msg.indexOf(" ", filterNameStart) == -1) {
                filterNameEnd = msg.length();
                filter = msg.substring(filterNameStart, filterNameEnd);
                reason = "";
            } else {
                filterNameEnd = msg.indexOf(" ", filterNameStart);
                filter = msg.substring(filterNameStart, filterNameEnd);
                int reasonStart = msg.indexOf(" ", filterNameEnd) + 1;
                int reasonEnd = (msg.length());
                reason = msg.substring(reasonStart, reasonEnd);
            }
            for (int i = 0; i < this.elements.filterNodes.getLength(); i++) {
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
            for (int i = 0; i < this.elements.filterNodes.getLength(); i++) {
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

    public void filterReason(String msg, String user) {
        try {
            String filterName = "";
            int filterNameStart = msg.indexOf("!fitler-add") + 16;
            int filterNameEnd;
            String reason;
            if (msg.indexOf(" ", filterNameStart) == -1) {
                filterNameEnd = msg.length();
                filterName = msg.substring(filterNameStart, filterNameEnd);
                reason = "";
            } else {
                filterNameEnd = msg.indexOf(" ", filterNameStart);
                filterName = msg.substring(filterNameStart, filterNameEnd);
                int reasonStart = msg.indexOf(" ", filterNameEnd) + 1;
                int reasonEnd = (msg.length());
                reason = msg.substring(reasonStart, reasonEnd);
            }
            for (int i = 0; i < this.elements.filterNodes.getLength(); i++) {
                Node n = this.elements.filterNodes.item(i);
                Element e = (Element) n;
                if (filterName.contentEquals(e.getAttribute("name"))) {
                    e.setAttribute("reason", reason);
                    writeXML();
                    sendWhisper(".w " + user + " Filter reason updated.");
                    return;
                }
            }
            sendWhisper(".w " + user + " Filter not found.");
        } catch (IllegalArgumentException e) {
            LOGGER.info(e.toString());
        }
    }

    private void sendWhisper(final String msg) {
        final String message = msg;
        this.outstream.println("PRIVMSG #"
                + this.elements.configNode.getElementsByTagName("myChannel").item(0).getTextContent()
                + " "
                + ":"
                + message);
    }

    public boolean checkAuthorization(String command, String username, boolean mod, boolean sub) {
        String auth = "";
        //LOGGER.info("COMMAND: " + command + " USERNAME: " + username + " MOD: " + mod + " SUB: " + sub);
        if (username.contentEquals(this.elements.configNode.getElementsByTagName("myChannel").item(0).getTextContent())) {
            return true;
        }
        for (int i = 0; i < this.elements.commandNodes.getLength(); i++) {
            Node n = this.elements.commandNodes.item(i);
            Element cmdXmlNode = (Element) n;
            if (command.contentEquals(cmdXmlNode.getAttribute("name"))) {
                auth = cmdXmlNode.getAttribute("auth");
                break;
            }
        }
        for (int i = 0; i < this.elements.subCommandNodes.getLength(); i++) {
            Node n = this.elements.subCommandNodes.item(i);
            Element cmdXmlNode = (Element) n;
            if (command.contentEquals(cmdXmlNode.getAttribute("name"))) {
                auth = cmdXmlNode.getAttribute("auth");
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
            //LOGGER.info("MOD FALSE: ");
            return false;
        }
        if ((auth.contains("+m ")) && mod) {
            // LOGGER.info("MOD TRUE: ");
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
     * This method is a quick and dirty solution for pyramid detection.
     *
     * It basically caches the last 15 messages, and user mapping, and does a
     * check on whether or not the user is doing a pattern of 3.
     *
     * @param user A given user that say a given message
     *
     * @param msg A message provided by the user
     */
    public void pyramidDetection(final String user, String msg) {
        recentMessages.add(new CachedMessage(user, msg));
        if (recentMessages.size() > Integer.parseInt(
                this.elements.configNode.getElementsByTagName("recentMessageCacheSize").item(0).getTextContent())) {
            recentMessages.remove(0);
        }
        int patternEnd = msg.indexOf(" ");
        String pattern;
        if (patternEnd == -1) {
            pattern = msg;
            //System.out.println(pattern + " PATTERN1");
        } else {
            pattern = msg.substring(0, msg.indexOf(" "));
            //System.out.println(pattern + " PATTERN2");
        }
        if (!msg.contentEquals(pattern + " " + pattern + " " + pattern)) {
            //System.out.println(msg + " IF MSG DOES NOT TEST");
            return;
        }
        int patternCount = 3;
        for (int i = recentMessages.size() - 2; i >= 0; i--) {
            CachedMessage cm = (CachedMessage) recentMessages.get(i);
            if ((patternCount == 3) && (cm.getMsg().contentEquals(pattern + " " + pattern)) && (cm.getUser().contentEquals(user))) {
                //t.println(cm.getMsg() + " CACHED MESSAGE PATTERN 2");
                patternCount = 2;
            } else if ((patternCount == 2) && (cm.getMsg().contentEquals(pattern)) && (cm.getUser().contentEquals(user))) {
                sendMessage(this.elements.configNode.getElementsByTagName("pyramidResponse").item(0).getTextContent());
                return;
            }
        }
    }

    /**
     * This command will send a message out to a specific Twitch channel.
     *
     * It will also wrap the message in pretty text (> /me) before sending it
     * out.
     *
     * @param msg The message to be sent out to the channel
     */
    private void sendMessage(final String msg) {
        final String message = "/me > " + msg;
        this.outstream.println("PRIVMSG #"
                + this.elements.configNode.getElementsByTagName("myChannel").item(0).getTextContent()
                + " "
                + ":"
                + message);
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
            System.out.println(file);
            AudioStream audioStream = new AudioStream(is);
            AudioPlayer.player.start(audioStream);
        } catch (Exception e) {
            LOGGER.severe(e.toString());
        }
    }

    /**
     * This method writes to a XML file the entire document.
     */
    private void writeXML() {
        try {
            File configFile = new File("kfbot.xml");
            TransformerFactory tFactory = TransformerFactory.newInstance();
            Transformer transformer = tFactory.newTransformer();
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");
            DOMSource source = new DOMSource(this.elements.doc);
            StreamResult result = new StreamResult(configFile);
            transformer.transform(source, result);
        } catch (TransformerException e) {
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
        for (int i = 0; i < this.elements.commandNodes.getLength(); i++) {
            Node n = this.elements.commandNodes.item(i);
            Element el = (Element) n;
            if (cmd.contentEquals(el.getAttribute("name"))) {
                el.setAttribute(attrib, value);
                writeXML();
                return true;
            }
        }
        sendMessage("Command " + cmd + " not found.");
        return false;
    }

    private boolean setSubCmdXMLParam(
            String cmd, String attrib, String value, boolean allowReservedCmds) {
        if (!allowReservedCmds && isReservedCommand(cmd)) {
            sendMessage("Failed: " + cmd + " is a reserved command.");
            return false;
        }
        for (int i = 0; i < this.elements.subCommandNodes.getLength(); i++) {
            Node n = this.elements.subCommandNodes.item(i);
            Element el = (Element) n;
            if (cmd.contentEquals(el.getAttribute("name"))) {
                el.setAttribute(attrib, value);
                writeXML();
                return true;
            }
        }
        sendMessage("Command " + cmd + " not found.");
        return false;
    }

    /**
     * This method checks the arguments for a particular command and the
     * arguments needed and returns the parameters.
     *
     * It simply throws an exception if the arguments defined are not the ones
     * that are needed.
     *
     * @param cmd The command for the request
     *
     * @param input A given input command for the request
     *
     * @param paramRequired True - additional parameters are needed False - no
     * additional parameters are needed
     *
     * @return The additional parameters
     *
     * @throws IllegalArgumentException An exception if the user never input all
     * the parameters
     */
    private String getInputParameter(String cmd, String input, boolean paramRequired)
            throws IllegalArgumentException {
        if (input.length() == cmd.length()) {
            if (paramRequired) {
                throw new IllegalArgumentException();
            }
            return "";
        }
        return input.substring(cmd.length() + 1);
    }
}
