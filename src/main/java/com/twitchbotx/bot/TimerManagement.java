package com.twitchbotx.bot;

import java.io.PrintStream;
import java.util.logging.Logger;
import org.w3c.dom.Element;

/*
** Roughly 15 minute intervals between command sendMessage, 
** OR based on number of messages between sendMessage
**
** RE-ADD online check timer foro online-only timer functionality 
** as well as for future discord stream is live function
 */
/**
 * This class is responsible for timer management.
 */
public final class TimerManagement extends Thread {


    private final ConfigParser.Elements elements;


    /*
** This takes the information parsed in the start of the program under 
** elements "repeating" and "interval"
** "repeating" = True commands need to start when the bot starts
** all commands set to repeat need to start (including created/edited commands)
     */
    public TimerManagement(final ConfigParser.Elements elements,
            final PrintStream stream) {
        this.elements = elements;
    }

    public void setupPeriodicBroadcast(final ConfigParser.Elements repeating, final PrintStream stream) {

        ConfigParser.Elements ce = (ConfigParser.Elements) repeating;
        for (int i = 0; i < elements.commandNodes.getLength(); i++) {
            Element ca = (Element) ce.commandNodes.item(i);

            if (Boolean.parseBoolean(ca.getAttribute("repeating"))) {
                long d = Long.parseLong(ca.getAttribute("initialDelay")) * 1000L;
                Long l = Long.parseLong(ca.getAttribute("interval")) * 1000L;
                if (l < 60000L) {
                    System.out.println("Repeating interval too short for command " + ca.getAttribute("name"));
                } else {
                    rTimer t = new rTimer(ca.getTextContent(), l, elements, stream, d);
                    Thread r = new Thread(t);
                    r.start();
                    System.out.println("Starting repeating command " + ca.getTextContent());
                }
            }
        }
    }

    static class rTimer extends Thread {
        
        private final PrintStream outstream;
        private final ConfigParser.Elements elements;
        private static final Logger LOGGER = Logger.getLogger(TwitchBotX.class.getSimpleName());

        String message;
        long repeatingTimer;
        long initialDelay;

        public rTimer(String msg, long timer, final ConfigParser.Elements elements, final PrintStream stream, long delay) {
            this.message = msg;
            this.repeatingTimer = timer;
            this.elements = elements;
            this.outstream = stream;
            this.initialDelay = delay;
        }

        private void sendMessage(final String msg) {
            final String message = "/me > " + msg;
            this.outstream.println("PRIVMSG #"
                    + this.elements.configNode.getElementsByTagName("myChannel").item(0).getTextContent()
                    + " "
                    + ":"
                    + message);
        }

        @Override
        public void run() {
            if (initialDelay != 0) {
                try {
                    Thread.sleep(initialDelay);
                } catch (InterruptedException e) {
                    LOGGER.severe("CAUGHT AT TIMER DELAY: " + e);
                }
            } 
            while (true) {
                sendMessage(this.message);
                try {
                    Thread.sleep(repeatingTimer);
                } catch (InterruptedException e) {
                    LOGGER.severe("ERROR: " + e);
                }
            }
        }
    }
}
