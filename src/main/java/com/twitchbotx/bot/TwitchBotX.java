package com.twitchbotx.bot;

import com.twitchbotx.bot.ConfigParser.Configuration;
import com.twitchbotx.bot.ConfigParser.Elements;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.parsers.ParserConfigurationException;
import org.xml.sax.SAXException;

/**
 * This class is the application for a Twitch Bot.
 */
public final class TwitchBotX {

    private static final Logger LOGGER = Logger.getLogger(TwitchBotX.class.getSimpleName());

    private PrintStream out;
    private BufferedReader in;
    private String dataIn;

    private static final String BOT_VERSION = "v1.09";
    private final ArrayList RecentMessages = new ArrayList();

    private final ConfigParser configParser = new ConfigParser();

    /**
     * This method will attempt to establish a connection to a given host and
     * port.
     *
     * If it fails, it will throw an IOException.
     *
     * @param host A given host, such as irc.chat.twitch.tv
     *
     * @param port A integer indicating the port of the connection
     *
     * @return A socket connection
     *
     * @throws IOException An exception thrown if connection is not established
     * or timed out.
     */
    public void startConnectionTest(final Elements elements) throws IOException {   
        CommandParser.checkConnection c = new CommandParser.checkConnection(elements, out);
        Thread check = new Thread(c);
        check.start();
    }


    /*
    * This method begins listening for PubSub info notices 
    *
    *
     */
   /* public void beginListeningPubSub(final Elements elements, String url, int port) {
        final PubSubHandler pubSub = new PubSubHandler(elements, out, url, port);
    }*/

    /**
     * This method will begin reading for incoming messages from Twitch IRC API.
     *
     * @param elements
     */
    public void beginReadingMessages(final Elements elements) {

        final CommandParser parser = new CommandParser(elements, out);

        try {
            for (;;) {
                dataIn = in.readLine();
                parser.parse(dataIn);
            }
        } catch (IOException e) {
            LOGGER.log(Level.WARNING, "An error occurred with I/O, perhaps with the Twitch API: {0}", e.toString());
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "A general error occurred parsing the message: {0}", e.toString());
        }
    }

    /*
    ** This will start the repeating commands based on what is found in XML
    **
     */
    public void startTimers(final Elements elements) {
        final TimerManagement timers = new TimerManagement(elements, out);
        timers.setupPeriodicBroadcast(elements, out);
    }

    /**
     * This method will start a sequence of events for starting the bot. 1) Load
     * and Read the configuration file. 2) Connect to the Twitch API 3) Start
     * all periodic timers for broadcasting (if there are any) 4) Start a
     * blocking read on the socket for incoming message
     */
    public void start() {
        try {
            LOGGER.info("kfbot for Twitch " + BOT_VERSION + " by Raxa");

            LOGGER.info("Reading configuration XML file");
            final Elements elements
                    = configParser.parseConfiguration("./kfbot.xml");

            final Configuration config
                    = configParser.getConfiguration(elements.configNode);

            LOGGER.info("Attempt to connect to Twitch servers."); 
            final Socket socket = new Socket(config.host, config.port);

            out = new PrintStream(socket.getOutputStream());
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            // Twitch uses IRC protocol to connect, this is how to connect
            // to the Twitch API
            out.println("PASS " + config.password);
            out.println("NICK " + config.account);
            out.println("JOIN #" + config.joinedChannel);
            out.println("CAP REQ :twitch.tv/tags");
            out.println("CAP REQ :twitch.tv/commands");
            out.println("CAP REQ :twitch.tv/membership");
            startConnectionTest(elements);
                
            
            // Begin connecting to and listening to Twitch PubSub 
            // whispers and stream is live function 
            // periodically sends connection updates through Ping/Pong
            //LOGGER.info("Attempt to start reading PUBSUB feed.");
           // beginListeningPubSub(elements, config.pubSub, config.port);

            final String ReadyMessage = "/me > " + BOT_VERSION + " has joined the channel.";
            out.println("PRIVMSG #"
                    + elements.configNode.getElementsByTagName("myChannel").item(0).getTextContent()
                    + " :"
                    + ReadyMessage);

            LOGGER.info("Bot is now ready for service.");

            // start all periodic timers for broadcasting events
            startTimers(elements);

            // start doing a blocking read on the socket
            beginReadingMessages(elements);
        } catch (ParserConfigurationException | SAXException | IOException e) {
            LOGGER.log(Level.SEVERE, "Error caught at start up: {0}", e.toString());
        }
    }

    /**
     * The application starts here.
     */
    public static void main(String[] args) {
        final TwitchBotX app = new TwitchBotX();
        app.start();
    }
}
