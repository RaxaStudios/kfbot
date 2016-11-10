package com.twitchbotx.bot;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.logging.Logger;

/**
 * This class handles all the youtube based queries from Twitch chat.
 * 
 * It will essentially query youtube, and get the ID and information about
 * the video back.
 */
public final class YoutubeHandler {
    
    private final PrintStream outstream;
    private static final Logger LOGGER = Logger.getLogger(YoutubeHandler.class.getSimpleName());
    private final ConfigParser.Elements elements;

    
    public YoutubeHandler(final ConfigParser.Elements elements,
            final PrintStream stream) {
            this.elements = elements;
            this.outstream = stream;
            }
    
    /**
     * This method sets youtube URL, reads, and sends out title
     * 
     * @param url 
     * 
     */
    private void getYoutubeTitle(String request) {
        try {
            String ytAPI = this.elements.configNode.getElementsByTagName("youtubeTitle").item(0).getTextContent();
            ytAPI = ytAPI.replaceAll("#id", "&id=" + request);
            ytAPI = ytAPI.replaceAll("#key", "&key=" + this.elements.configNode.getElementsByTagName("youtubeAPI").item(0).getTextContent());
            URL url = new URL(ytAPI);
            URLConnection con = (URLConnection) url.openConnection();
            System.out.println(con);
            BufferedReader bufReader = new BufferedReader(new InputStreamReader(con.getInputStream()));
            String line;
            StringBuilder response = new StringBuilder();
            while ((line = bufReader.readLine()) != null) {
                response.append(line);
            }
            bufReader.close();
            if (response.toString().contains("\"items\": []")) {
                sendMessage("Video not found.");
            } 
            else {
                int bi = response.toString().indexOf("\"title\":") + 10;
                int ei = response.toString().indexOf(",", bi) - 1;
                String s = response.toString().substring(bi, ei); 
                if (s.length() > 0) {
                sendMessage(s);
                } 
            }
        } catch (IOException e) {
            System.out.println("GetTitle.GetTitle - error opening or reading URL: " + e);
        }
    }

    /**
     * This method searches all messages in stream for youtube links 
     * Sends ID to getYoutubeTitle method 
     * This method requires 11 character video ID
     * 
     * @param msg 
     * A full message from Twitch IRC API from a particular user
     */
    
    public void handleLinkRequest(final String msg) {

        for (;;) {
            try {
                
                if(msg.contains("youtube.com")){
                    
                    int startToken = msg.indexOf("youtube.com") + 20;
                    int endToken = msg.indexOf("v=") + 13;
                    String ytId = msg.substring(startToken, endToken);
                    getYoutubeTitle(ytId);
                }else if(msg.contains("youtu.be")){
                    
                    int startToken = msg.indexOf("youtu.be") + 9;
                    int endToken = msg.indexOf("/") + 1;
                    String ytId = msg.substring(startToken, endToken);
                    System.out.println(ytId);
                    getYoutubeTitle(ytId);
                }else {
                    return;
                }
                return;
               
            } catch (Exception e) {
                LOGGER.severe(e.toString());
                return;
                
            } finally {
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

}
