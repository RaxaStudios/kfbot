package com.twitchbotx.bot;

import java.io.IOException;
import java.io.PrintStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.logging.Logger;
import java.util.logging.Level;

/**
 * This class is used to parse all commands flowing through it.
 */
public final class CommandParser {

    private static final Logger LOGGER = Logger.getLogger(CommandParser.class.getSimpleName());

    // For handling all normal commands
    private final CommandHandler commandHandler;

    // A stream for communicating to twitch chat through IRC
    private final PrintStream outstream;

    // For handling all youtube link messaging
    private final YoutubeHandler youtubeHandler;

    // Soon to be added for filter options
    private final ModerationHandler moderationHandler;

    // A simple constructor for this class that takes in the XML elements
    // for quick modification
    public CommandParser(final ConfigParser.Elements elements,
            final PrintStream stream) {
        this.commandHandler = new CommandHandler(elements, stream);
        this.outstream = stream;
        this.youtubeHandler = new YoutubeHandler(elements, stream);
        this.moderationHandler = new ModerationHandler(elements, stream);
    }

    /**
     * This method will start handling all the commands and delegating it to the
     * proper handlers. Uses XML file to determine requirements for commands.
     * Requirements set by !command-auth Command enabled/disabled by
     * !command-enable
     *
     * @param mod A boolean field which indicates whether this is a mod message.
     *
     * @param sub A boolean field which indicates whether this is a subscriber
     * message.
     *
     * @param command A command field for the command that was parsed from the
     * message.
     *
     * @param trailing The trailing message that accompany the command
     *
     * @param prefix A prefix for positioning where the username might reside
     */
    private void handleCommand(
            final String username,
            final boolean mod,
            final boolean sub,
            String trailing) {
        if (trailing.contains("")) {
            trailing = trailing.replaceAll("", "");
            trailing = trailing.replaceFirst("ACTION ", "");
        }

        //commandHandler.pyramidDetection(username, trailing);
        youtubeHandler.handleLinkRequest(trailing);
        moderationHandler.handleTool(username, trailing);

        if (!trailing.startsWith("!")) {
            return;
        }

        if (trailing.startsWith("!uptime")) {
            LOGGER.log(Level.INFO, "{0} {1} {2}", new Object[]{username, mod, sub});
            if (commandHandler.checkAuthorization("!uptime", username, mod, sub)) {
                commandHandler.uptime(trailing);
            }
            return;
        }
        if (trailing.startsWith("!followage")) {
            if (commandHandler.checkAuthorization("!followage", username, mod, sub)) {
                String user = username.toLowerCase();
                commandHandler.followage(user);
            }
            return;
        }

        if (trailing.startsWith("!commands")) {
            if (commandHandler.checkAuthorization("!commands", username, mod, sub)) {
                commandHandler.commands(username, mod, sub);
            }
        }

        if (trailing.startsWith("!command-add")) {
            if (commandHandler.checkAuthorization("!command-add", username, mod, sub)) {
                commandHandler.addCmd(trailing);
            }
            return;
        }
        if (trailing.startsWith("!command-delete")) {
            if (commandHandler.checkAuthorization("!command-delete", username, mod, sub)) {
                commandHandler.delCmd(trailing);
            }
            return;
        }
        if (trailing.startsWith("!command-edit")) {
            if (commandHandler.checkAuthorization("!command-edit", username, mod, sub)) {
                commandHandler.editCmd(trailing);
            }
            return;
        }
        if (trailing.startsWith("!command-auth")) {
            if (commandHandler.checkAuthorization("!command-auth", username, mod, sub)) {
                commandHandler.authCmd(username, trailing);
            }
            return;
        }
        if (trailing.startsWith("!command-enable")) {
            if (commandHandler.checkAuthorization("!command-enable", username, mod, sub)) {
                commandHandler.enableCmd(trailing);
            }
            return;
        }
        if (trailing.startsWith("!command-disable")) {
            if (commandHandler.checkAuthorization("!command-disable", username, mod, sub)) {
                commandHandler.disableCmd(trailing);
            }
            return;
        }
        if (trailing.startsWith("!command-repeat")) {
            if (commandHandler.checkAuthorization("!command-repeat", username, mod, sub)) {
                commandHandler.repeatingCmd(trailing);
            }
            return;
        }
        if (trailing.startsWith("!command-delay")) {
            if (commandHandler.checkAuthorization("!command-delay", username, mod, sub)) {
                commandHandler.cmdDelay(trailing);
            }
            return;
        }
        if (trailing.startsWith("!command-interval")) {
            if (commandHandler.checkAuthorization("!command-interval", username, mod, sub)) {
                commandHandler.cmdInterval(trailing);
            }
            return;
        }
        if (trailing.startsWith("!command-cooldown")) {
            if (commandHandler.checkAuthorization("!command-cooldown", username, mod, sub)) {
                commandHandler.cmdCooldown(trailing);
            }
            return;
        }
        if (trailing.startsWith("!command-sound")) {
            if (commandHandler.checkAuthorization("!command-sound", username, mod, sub)) {
                commandHandler.cmdSound(trailing);
            }
            return;
        }

        if (trailing.startsWith("!filter-all")) {
            if (commandHandler.checkAuthorization("!filter-all", username, mod, sub)) {
                commandHandler.filterAll(trailing, username);
            }
        }
        if (trailing.startsWith("!filter-add")) {
            if (commandHandler.checkAuthorization("!filter-add", username, mod, sub)) {
                commandHandler.filterAdd(trailing, username);
            }
        }
        if (trailing.startsWith("!filter-delete")) {
            if (commandHandler.checkAuthorization("!filter-delete", username, mod, sub)) {
                commandHandler.filterDel(trailing, username);
            }
        }

        if (trailing.startsWith("!set-msgCache")) {
            if (commandHandler.checkAuthorization("!set-msgCache", username, mod, sub)) {
                commandHandler.setMsgCacheSize(trailing);
            }
            return;
        }
        if (trailing.startsWith("!set-pyramidResponse")) {
            if (commandHandler.checkAuthorization("!set-pyramidResponse", username, mod, sub)) {
                commandHandler.setPyramidResponse(trailing);
            }
            return;
        }
        if (trailing.startsWith("!cnt-add")) {
            if (commandHandler.checkAuthorization("!cnt-add", username, mod, sub)) {
                commandHandler.cntAdd(trailing);
            }
            return;
        }
        if (trailing.startsWith("!cnt-delete")) {
            if (commandHandler.checkAuthorization("!cnt-delete", username, mod, sub)) {
                commandHandler.cntDelete(trailing);
            }
            return;
        }
        if (trailing.startsWith("!cnt-set")) {
            if (commandHandler.checkAuthorization("!cnt-set", username, mod, sub)) {
                commandHandler.cntSet(trailing);
            }
            return;
        }
        if (trailing.startsWith("!cnt-current")) {
            if (commandHandler.checkAuthorization("!cnt-current", username, mod, sub)) {
                commandHandler.cntCurrent(trailing);
            }
            return;
        }
        if (trailing.startsWith("!countadd")) {
            if (commandHandler.checkAuthorization("!countadd", username, mod, sub)) {
                commandHandler.count(trailing);

            }
            return;
        }

        if (trailing.startsWith("!totals")) {
            if (commandHandler.checkAuthorization("!totals", username, mod, sub)) {
                commandHandler.totals(trailing);
            }
            return;
        }
        commandHandler.parseForUserCommands(trailing, username, mod, sub);
    }

    static class checkConnection extends Thread {

        long interval = 2000000;
        long initDelay = 2000000;
        String site = "google.com";
        int port = 80;
        private final PrintStream outCheck;
        private final ConfigParser.Elements elements;

        public checkConnection(final ConfigParser.Elements elements, final PrintStream stream) {
            this.outCheck = stream;
            this.elements = elements;
        }

        private void sendJoin(final String msg) {
            this.outCheck.println(msg);
        }

        public void run() {

            try {
                Thread.sleep(initDelay);
            } catch (InterruptedException e) {
                LOGGER.severe("ERROR CHECKING CONNECTION: " + e);
            }
            while (true) {
                try {
                    sendJoin("Join #" + this.elements.configNode.getElementsByTagName("myChannel").item(0).getTextContent());
                    sendJoin("PING");
                    Socket sock = new Socket();
                    InetSocketAddress addr = new InetSocketAddress(site, port);
                    sock.connect(addr, 3000);
                    LOGGER.info("connected to google.com without issue");
                } catch (IOException e) {
                    sendJoin("Join #" + this.elements.configNode.getElementsByTagName("myChannel").item(0).getTextContent());
                    LOGGER.severe("Attemping to reconnect to Twitch chat: " + e);
                }
                try {
                    Thread.sleep(interval);
                } catch (InterruptedException e) {
                    LOGGER.severe("Could not connect: " + e);
                    LOGGER.severe("Please restart app");
                }
            }
        }
    }

    /**
     * This method parses all incoming messages from Twitch IRC.
     *
     * @param msg A string that represents the message type.
     */
    public void parse(String msg) {
        try {
            // If nothing is provided, exit out of here
            if (msg == null || msg.isEmpty()) {
                return;
            }

            // A ping was sent by the twitch server, complete the handshake
            // by sending it back pong with the message.
            if (msg.startsWith("PING")) {
                final int trailingStart = msg.indexOf(" :");
                final String trailing = msg.substring(trailingStart + 2);
                this.outstream.println("PONG " + trailing);
                return;
            }

            boolean isMod = false;
            boolean isSub = false;
            String username = "";

            // This is a message from a user.
            // If it's the broadcaster, he/she is a mod.
            //LOGGER.info(msg);
            if (msg.startsWith("@badges=broadcaster/1")) {
                isMod = true;
            }

            // Find the mod indication
            final int modPosition = msg.indexOf("mod=") + 4;
            if ("1".equals(msg.substring(modPosition, modPosition + 1))) {
                isMod = true;
            }

            // Find the subscriber indication
            final int subPosition = msg.indexOf("subscriber=") + 11;
            if ("1".equals(msg.substring(subPosition, subPosition + 1))) {
                isSub = true;
            }

            // Find the username
            // User-id search for V5 switch
            /*if (msg.contains("user-id=")){
                int usernameStart = msg.indexOf("user-id=", msg.indexOf(";"));
                System.out.println(usernameStart);
            username = msg.substring(msg.indexOf("user-id=") + 8, msg.indexOf(";", msg.indexOf("user-id=")));
            System.out.println(username + " USERNAME");
            }*/
            if (msg.contains("user-type=")) {
                int usernameStart = msg.indexOf(":", msg.indexOf("user-type="));
                int usernameEnd = msg.indexOf("!", usernameStart);
                if (usernameStart != -1 && usernameEnd != -1) {
                    username = msg.substring(usernameStart + 1, usernameEnd).toLowerCase();
                    //System.out.println(username + " USERNAME");
                }
            }

            // Split the message into pieces to find the real message
            final int msgPosition = msg.indexOf("user-type=");

            // No message to be processed
            if (msgPosition == -1) {
                return;
            }
            msg = msg.substring(msgPosition);

            // Find the # for the channel, so we can figured out what type
            // of message this is.
            final int channelPosition = msg.indexOf("#");
            if (msgPosition == -1) {
                return;
            }

            // Ensure we can find "PRIVMSG" as an indication that this is a
            // user message, make sure we only search a limited bound, because
            // somebody can potentially fake a mod by including "PRIVMSG" 
            // in their message
            final String hasPrivMsg = msg.substring(0, channelPosition);
            final int privMsgIndex = hasPrivMsg.indexOf("PRIVMSG");
            if (privMsgIndex == -1) {
                return;
            }

            // Capture the raw message, and find the message used
            final int msgIndex = msg.indexOf(":", channelPosition);

            // No message found, return immediately
            if (msgIndex == -1) {
                return;
            }

            msg = msg.substring(msgIndex + 1);
            

            // Handle the message
            handleCommand(username, isMod, isSub, msg);

        } catch (Exception e) {
            e.printStackTrace();
            LOGGER.log(Level.WARNING, "Error detected in parsing a message: throwing away message ", e.toString());
        }
    }
}
