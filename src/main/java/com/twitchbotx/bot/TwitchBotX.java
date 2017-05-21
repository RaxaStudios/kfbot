package com.twitchbotx.bot;

import com.twitchbotx.bot.ConfigParameters.Elements;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.Socket;
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

    private final ConfigParameters configuration = new ConfigParameters();

    /**
     * This method will begin reading for incoming messages from Twitch IRC API.
     *
     * @param store
     * The database utility for accessing and updating.
     */
    public void beginReadingMessages(final Datastore store) {

        final CommandParser parser = new CommandParser(store, out);

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

    /**
     * This method will start a sequence of events for starting the bot.
     *
     * 1) Load and Read the configuration file.
     * 2) Connect to the Twitch API
     * 3) Start all periodic timers for broadcasting (if there are any)
     * 4) Start a blocking read on the socket for incoming message
     */
    public void start() {
        try {
            LOGGER.info("NecoBot for Twitch " + BOT_VERSION + " by Raxa");

            LOGGER.info("Reading configuration XML file");
            final Elements elements
                    = configuration.parseConfiguration("./kfbot.xml");

            final Datastore store = new XmlDatastore(elements);
            final ConfigParameters.Configuration config
                    = store.getConfiguration();

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

            final String ReadyMessage = "/me > " + BOT_VERSION + " has joined the channel.";
            out.println("PRIVMSG #"
                    + elements.configNode.getElementsByTagName("myChannel").item(0).getTextContent()
                    + " :"
                    + ReadyMessage);

            LOGGER.info("Bot is now ready for service.");

            // start doing a blocking read on the socket
            beginReadingMessages(store);
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
