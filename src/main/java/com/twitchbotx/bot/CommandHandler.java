package com.twitchbotx.bot;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
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
import java.time.LocalDate;
import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.Timer;
import java.util.TimerTask;
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
        "!cnt-add",
        "!cnt-delete",
        "!cnt-set",
        "!cnt-current",
        "count",
        "!filter-all",
        "!filter-add",
        "!filter-delete",
    };

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
            for (int i = 0; i < this.elements.commandNodes.getLength(); i++) {
                Node n = this.elements.commandNodes.item(i);
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
            newNode.setAttribute("name", cmd);
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
     * Sets status of whether or not a command should automatically run.
     * Keep separate from cmdInterval to allow for on/off repeat function while keeping interval info.
     * @param msg The message from the user
     */
    public void repeatingCmd(String msg) {
        try {
            String parameters = getInputParameter("!command-repeat", msg, true);
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
                sendMessage("Command [" + cmd + "] repeating set to [" + repeat + "]");
            }
        } catch (IllegalArgumentException e) {
            sendMessage("Syntax: !command-repeat [!command] [true|false].");
        }
    }

    /**
     * Adds delay to commands, helps to offset autocommands 
     * Useful for autocommands so they don't show up all at once.
     * @param msg The message from the user
     */
    public void cmdDelay(String msg) {
        try {
            String parameters = getInputParameter("!command-delay", msg, true);
            int separator = parameters.indexOf(" ");
            String cmd = parameters.substring(0, separator);
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
** This creates the URL = api.twitch.tv/kraken with desired streamer name("myChannel") from kfbot1.0.xml
** Opens a connection, begins reading using BufferedReader brin, builds a String response based on API reply
** Once response is done building, checks for "stream\:null" response - this means stream is not live
** Creates Strings to hold content placed between int "bi" and int "ei" as per their defined index
** 
*/
    
    public void uptime(String msg) {
        
        /*
        ** Cooldown for uptime command
        ** 
        */
        /*Boolean upCooldown = false;
        if(!upCooldown){
            sendMessage("Nope");
        }
        else {
            sendMessage("Yep");
        }
        
      
       
        ConfigParser.Elements uptime = (ConfigParser.Elements) this.elements;
        for(int i = 0; i < elements.commandNodes.getLength(); i++){
        Element coolDown = (Element) uptime.commandNodes.item(i);
        
        System.out.println(coolDown);
        }
        Calendar calendar = Calendar.getInstance();
        Date now = calendar.getTime();
        Date cdTime = new Date();
        
        if (cdTime.before(coolDown)){
            return;
        }
        else  {
            Date coolDown = new Date(now.getTime() + Long.parseLong("30") * 1000L);
        }
        if (!this.elements.commands.getAttribute("cdUntil").isEmpty()) {
                            cdTime = new Date(Long.parseLong(this.elements.commands.getAttribute("cdUntil")));
                        }
        if(now.before(cdTime)){
            return;
        }
        cdTime = new Date(now.getTime() + Long.parseLong(this.elements.commands.getAttribute("cooldown")) * 1000L);
        this.elements.commands.setAttribute("cdUntil", Long.toString(cdTime.getTime()));
        */
        
        
        
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
                sendMessage("Stream is not currently live.");
            } 
            else {
                int bi = response.toString().indexOf("\"created_at\":") + 14;
                int ei = response.toString().indexOf("\",", bi);
                String s = response.toString().substring(bi, ei);
                Instant start = Instant.parse(s);
                Instant current = Instant.now();
                long gap = ChronoUnit.MILLIS.between(start,current);
                String upT = String.format("%d hours, %d minutes, %d seconds", new Object[]{
                    TimeUnit.MILLISECONDS.toHours(gap),
                    TimeUnit.MILLISECONDS.toMinutes(gap) - TimeUnit.HOURS.toMinutes(TimeUnit.MILLISECONDS.toHours(gap)),
                    TimeUnit.MILLISECONDS.toSeconds(gap) - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(gap))
                });
                sendMessage("Stream has been up for " + upT + ".");
                
            }
        } catch (Exception e) {
            LOGGER.severe(e.toString());
        }
        /* Timer coolDown = new Timer();
        
    }
        else {
            ;
        }*/
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
            System.out.println(inputLine);
        }
        brin.close();
        if (response.toString().contains("404")) {
                ;
            } 
            else {
                int bi = response.toString().indexOf("\"created_at\":") + 14;
                int ei = response.toString().indexOf("T", bi);
                
                String s = response.toString().substring(bi, ei);
                System.out.println(s);
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
                LocalDate begin = LocalDate.parse(s, formatter);
                LocalDate today = LocalDate.now();
                long gap = ChronoUnit.DAYS.between(begin,today);
                sendMessage(user + " has been following for " + gap + " days. Starting on " + begin + ".");
            }
    }catch (Exception e){
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
    
/* This section for adding/removing/setting counts needs to be rearranged.
 * Configure system to work with objective count names/amounts, not hard-coded   
 *
 * return name and value   
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
            String parameters = getInputParameter("!count", msg, true);
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
            sendMessage("Syntax: !count [name] [value]");
        }
    }
   /*
    Need to rework to function as a call for all current counters with totals
    Add/Delete via commands, data stored in XML
    */
    
    public void scoreBoard(String msg)
    {
      String scoreMsg = "";

      int highScore = -1;
      int highestScore = 0;
      String hsGame = "";
     /* for (int i = 0; i < .getLength(); i++)
      {
        Element xmlNode = (Element)counterNodes.item(i);
        switch (xmlNode.getAttribute("name"))
        {
        
      sendMessage(scoreMsg);
        }
    }*/
    }
    
    public boolean checkAuthorization(String command, String username, boolean mod, boolean sub) {
        String auth = "";
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
        if (auth.isEmpty()) {
            return false;
        }
        if (auth.contains("-" + username + " ")) {
            return false;
        }
        if (auth.contains("+" + username + " ")) {
            return true;
        }
        if ((auth.contains("-m ")) && mod) {
            return false;
        }
        if ((auth.contains("+m ")) && mod) {
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
    public void pyramidDetection(final String user, final String msg) {
        recentMessages.add(new CachedMessage(user, msg));
        if (recentMessages.size() > Integer.parseInt(
                this.elements.configNode.getElementsByTagName("recentMessageCacheSize").item(0).getTextContent())) {
            recentMessages.remove(0);
        }
        int patternEnd = msg.indexOf(" ");
        String pattern;
        if (patternEnd == -1) {
            pattern = msg;
        } else {
            pattern = msg.substring(0, msg.indexOf(" "));
        }
        if (!msg.contentEquals(pattern + " " + pattern + " " + pattern)) {
            return;
        }
        int patternCount = 3;
        for (int i = recentMessages.size() - 2; i >= 0; i--) {
            CachedMessage cm = (CachedMessage) recentMessages.get(i);
            if ((patternCount == 3) && (cm.getMsg().contentEquals(pattern + " " + pattern)) && (cm.getUser().contentEquals(user))) {
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
     * Plays sound file based on attached .wav to certain commands within sound="" in XML. 
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
     * This method writes to a XML file the entire document.
     */
    private void writeXML() {
        try {
            File configFile = new File("kfbotv1.0.xml");
            TransformerFactory tFactory = TransformerFactory.newInstance();
            Transformer transformer = tFactory.newTransformer();
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
     * @param command 
     * The command to check for.
     *
     * @return 
     * True - the command has been reserved and we shouldn't override it
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

    /**
     * This method checks the arguments for a particular command and the 
     * arguments needed and returns the parameters.
     * 
     * It simply throws an exception if the arguments defined are not the ones
     * that are needed.
     * 
     * @param cmd
     * The command for the request
     * 
     * @param input
     * A given input command for the request
     * 
     * @param paramRequired
     * True - additional parameters are needed
     * False - no additional parameters are needed
     * 
     * @return
     * The additional parameters 
     * 
     * @throws IllegalArgumentException 
     * An exception if the user never input all the parameters
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
