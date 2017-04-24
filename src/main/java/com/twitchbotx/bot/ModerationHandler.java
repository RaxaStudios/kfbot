/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.twitchbotx.bot;

import java.io.PrintStream;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
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
    private Pattern pattern;
    private Matcher matcher;
    private static final String BANNED_USERNAME = "(\\d{7}([A-z]{1})\\d{7}|\\d{14})";

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
            Calendar cal = Calendar.getInstance();
            SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
            System.out.println(sdf.format(cal.getTime()) + " " + username + ": " + msg);
            try {
                if (!filterCheck(msg).equals("no filter")) {
                    //System.out.println(reason);
                    sendMessage(".timeout " + username + " 600 " + reason);
                    return;
                } else if (userCheck(username)) {
                    sendMessage(".timeout " + username + " 600 Username caught by filter");
                    return;
                }
                return;

            } catch (Exception e) {
                LOGGER.severe(e.toString());
            }
            return;
        }

    }

    private boolean userCheck(String username) {
        pattern = Pattern.compile(BANNED_USERNAME);
        matcher = pattern.matcher(username);
        // System.out.println(matcher.matches());
        //System.out.println(username);
        return matcher.matches();
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
