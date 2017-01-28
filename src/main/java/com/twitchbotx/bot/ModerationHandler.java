/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.twitchbotx.bot;

import java.io.PrintStream;
import java.util.logging.Logger;
import org.w3c.dom.Element;

/**
 *
 * @author RaxaStudios
 */
public class ModerationHandler {

    private final PrintStream outstream;
    private static final Logger LOGGER = Logger.getLogger(YoutubeHandler.class.getSimpleName());
    private final ConfigParser.Elements elements;
    private String reason;

    public ModerationHandler(final ConfigParser.Elements elements,
            final PrintStream stream) {
        this.elements = elements;
        this.outstream = stream;
    }

    public String filterCheck(String msg) {
        for (int i = 0; i < elements.filterNodes.getLength(); i++) {
            Element ca = (Element) elements.filterNodes.item(i);
            if (!Boolean.parseBoolean(ca.getAttribute("disabled"))) {
                String filter = ca.getAttribute("name");
                reason = ca.getAttribute("reason");
                if (msg.contains(filter)) {
                    return reason;
                }
            } else {
                reason = "no filter";
                return reason;
            }
        }
        reason = "no filter";
        return reason;
    }

    public void handleTool(String username, String msg) {
        for (;;) {
            try {
                if (!filterCheck(msg).equals("no filter")) {
                    System.out.println(reason);
                    sendMessage(".timeout " + username + " 600 " + reason);
                    return;
                }
                return;

            } catch (Exception e) {
                LOGGER.severe(e.toString());
            }
            return;
        }

    }

    private void sendMessage(final String msg) {
        final String message = msg;
        this.outstream.println("PRIVMSG #"
                + this.elements.configNode.getElementsByTagName("myChannel").item(0).getTextContent()
                + " "
                + ":"
                + message);

    }
}
