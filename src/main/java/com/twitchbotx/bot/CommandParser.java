package com.twitchbotx.bot;

import com.twitchbotx.bot.handlers.*;

import java.io.PrintStream;
import java.util.logging.Logger;
import java.util.logging.Level;

/**
 * This class is used to parse all commands flowing through it.
 */
public final class CommandParser {

    private static final Logger LOGGER = Logger.getLogger(CommandParser.class.getSimpleName());

    // For handling all normal commands
    private final CommandOptionHandler commandOptionsHandler;

    // A stream for communicating to twitch chat through IRC
    private final PrintStream outstream;

    // For handling all youtube link messaging
    private final YoutubeHandler youtubeHandler;

    // For moderation filtering options
    private final ModerationHandler moderationHandler;

    // For pyramid detection
    private final PyramidDetector pyramidDetector;

    // For Twitch statuses
    private final TwitchStatusHandler twitchStatusHandler;

    // For counter handling
    private final CountHandler countHandler;

    // For filter handling
    private final FilterHandler filterHandler;

    // A simple constructor for this class that takes in the XML elements
    // for quick modification
    public CommandParser(final Datastore store, final PrintStream stream) {

        // all the handlers for different messages
        this.commandOptionsHandler = new CommandOptionHandler(store);
        this.pyramidDetector = new PyramidDetector(store);
        this.twitchStatusHandler = new TwitchStatusHandler(store);
        this.countHandler = new CountHandler(store);
        this.filterHandler = new FilterHandler(store);
        this.youtubeHandler = new YoutubeHandler(store, stream);
        this.moderationHandler = new ModerationHandler(store, stream);

        this.outstream = stream;
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
     * @param trailing The trailing message that accompany the command
     */
    private void handleCommand(final String username, final boolean mod, final boolean sub, String trailing) {
        if (trailing.contains("")) {
            trailing = trailing.replaceAll("", "");
            trailing = trailing.replaceFirst("ACTION ", "");
        }
        if (trailing.startsWith("!")) {
            String cmd;
            int cmdEnd = trailing.indexOf(" ");
            if (cmdEnd == -1) {
                trailing = trailing.toLowerCase();
                System.out.println("TRAIL: " + trailing);
            } else {
                cmd = trailing.substring(trailing.indexOf("!"), trailing.indexOf(" "));
                System.out.println(cmd + " COMMAND");
            }
        }

        //
//        final boolean detected = pyramidDetector.pyramidDetection(username, trailing);
//        if(detected) {
//            twitchMessenger.sendMessage(store.getConfiguration().pyramidResponse);
//        }

        youtubeHandler.handleLinkRequest(trailing);
        moderationHandler.handleTool(username, trailing);

        if (!trailing.startsWith("!")) {
            return;
        }

        if (trailing.startsWith("!uptime")) {
            LOGGER.log(Level.INFO, "{0} {1} {2}", new Object[]{username, mod, sub});
            if (commandOptionsHandler.checkAuthorization("!uptime", username, mod, sub)) {
                twitchStatusHandler.uptime(trailing);
            }
            return;
        }

        if (trailing.startsWith("!followage")) {
            if (commandOptionsHandler.checkAuthorization("!followage", username, mod, sub)) {
                String user = username.toLowerCase();
                twitchStatusHandler.followage(user);
            }
            return;
        }

//        if (trailing.startsWith("!commands")) {
//            if (commandOptionsHandler.checkAuthorization("!commands", username, mod, sub)) {
//                commandOptionsHandler.commands(username, mod, sub);
//            }
//        }

        if (trailing.startsWith("!command-add")) {
            if (commandOptionsHandler.checkAuthorization("!command-add", username, mod, sub)) {
                commandOptionsHandler.addCommand(trailing);
            }
            return;
        }
        if (trailing.startsWith("!command-delete")) {
            if (commandOptionsHandler.checkAuthorization("!command-delete", username, mod, sub)) {
                commandOptionsHandler.deleteCommand(trailing);
            }
            return;
        }
        if (trailing.startsWith("!command-edit")) {
            if (commandOptionsHandler.checkAuthorization("!command-edit", username, mod, sub)) {
                commandOptionsHandler.editCommand(trailing);
            }
            return;
        }
        if (trailing.startsWith("!command-auth")) {
            if (commandOptionsHandler.checkAuthorization("!command-auth", username, mod, sub)) {
                commandOptionsHandler.authorizeCommand(username, trailing);
            }
            return;
        }
        if (trailing.startsWith("!command-enable")) {
            if (commandOptionsHandler.checkAuthorization("!command-enable", username, mod, sub)) {
                commandOptionsHandler.commandEnable(trailing);
            }
            return;
        }
        if (trailing.startsWith("!command-disable")) {
            if (commandOptionsHandler.checkAuthorization("!command-disable", username, mod, sub)) {
                commandOptionsHandler.commandDisable(trailing);
            }
            return;
        }
        if (trailing.startsWith("!command-sound")) {
            if (commandOptionsHandler.checkAuthorization("!command-sound", username, mod, sub)) {
                commandOptionsHandler.commandSound(trailing);
            }
            return;
        }

        if (trailing.startsWith("!filter-all")) {
            if (commandOptionsHandler.checkAuthorization("!filter-all", username, mod, sub)) {
                filterHandler.getAllFilters(trailing, username);
            }
        }
        if (trailing.startsWith("!filter-add")) {
            if (commandOptionsHandler.checkAuthorization("!filter-add", username, mod, sub)) {
                filterHandler.addFilter(trailing, username);
            }
        }
        if (trailing.startsWith("!filter-delete")) {
            if (commandOptionsHandler.checkAuthorization("!filter-delete", username, mod, sub)) {
                filterHandler.deleteFilter(trailing, username);
            }
        }

        if (trailing.startsWith("!set-msgCache")) {
            if (commandOptionsHandler.checkAuthorization("!set-msgCache", username, mod, sub)) {
                pyramidDetector.setMessageCacheSize(trailing);
            }
            return;
        }
        if (trailing.startsWith("!set-pyramidResponse")) {
            if (commandOptionsHandler.checkAuthorization("!set-pyramidResponse", username, mod, sub)) {
                pyramidDetector.setPyramidResponse(trailing);
            }
            return;
        }
        if (trailing.startsWith("!cnt-add")) {
            if (commandOptionsHandler.checkAuthorization("!cnt-add", username, mod, sub)) {
                countHandler.addCounter(trailing);
            }
            return;
        }
        if (trailing.startsWith("!cnt-delete")) {
            if (commandOptionsHandler.checkAuthorization("!cnt-delete", username, mod, sub)) {
                countHandler.deleteCounter(trailing);
            }
            return;
        }
        if (trailing.startsWith("!cnt-set")) {
            if (commandOptionsHandler.checkAuthorization("!cnt-set", username, mod, sub)) {
                countHandler.setCounter(trailing);
            }
            return;
        }
        if (trailing.startsWith("!cnt-current")) {
            if (commandOptionsHandler.checkAuthorization("!cnt-current", username, mod, sub)) {
                countHandler.getCurrentCount(trailing);
            }
            return;
        }
        if (trailing.startsWith("!countadd")) {
            if (commandOptionsHandler.checkAuthorization("!countadd", username, mod, sub)) {
                countHandler.updateCount(trailing);
            }
            return;
        }

        if (trailing.startsWith("!totals")) {
            if (commandOptionsHandler.checkAuthorization("!totals", username, mod, sub)) {
                countHandler.totals();
            }
            return;
        }
        commandOptionsHandler.parseForUserCommands(trailing, username, mod, sub);
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
            LOGGER.info(msg);
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
            
            if (msg.contains("user-type=")){
                int usernameStart = msg.indexOf(":", msg.indexOf("user-type="));
                int usernameEnd = msg.indexOf("!", usernameStart);
                if (usernameStart != -1 && usernameEnd != -1) {
                    username = msg.substring(usernameStart + 1, usernameEnd).toLowerCase();
                    System.out.println(username + " USERNAME");
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
