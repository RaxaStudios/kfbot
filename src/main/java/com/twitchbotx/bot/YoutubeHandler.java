package com.twitchbotx.bot;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.logging.Logger;

/**
 * This class handles all the youtube based queries from Twitch chat.
 * 
 * It will essentially query youtube, and get the ID and information about
 * the video back.
 */
public final class YoutubeHandler {
    
    private static final Logger LOGGER = Logger.getLogger(YoutubeHandler.class.getSimpleName());

    /**
     * This method will get the youtube video ID from a given URL link to a 
     * youtube resource.
     * 
     * @param url
     * The URL of the resource link 
     * 
     * @return 
     * A Youtube video ID
     */
    private String getYoutubeVideoID(final String url) {
        if (url.contains("youtube")) {
            return url.substring(url.lastIndexOf("=") + 1);
        }
        if (url.contains("youtu.be")) {
            return url.substring(url.lastIndexOf("/") + 1);
        }
        return "";
    }

    /**
     * This method will get the youtube title given a URL link to the youtube
     * resource.
     * 
     * @param url 
     * The URL of the youtube resource
     */
    private void getYoutubeTitle(final URL url) {
        String startTag = "&title=";
        int startTagLength = startTag.length();

        int startIndex = 0;
        int endIndex = 0;
        try {
            BufferedReader bufReader = new BufferedReader(new InputStreamReader(url.openStream()));
            String line = bufReader.readLine();
            startIndex = line.indexOf(startTag);
            endIndex = line.indexOf("&", startIndex + startTagLength);
            String title = line.substring(startIndex + startTagLength, endIndex);
            bufReader.close();

            title = youtubeDescToPlainText(title);
            if (title.length() > 0) {
                LOGGER.info("Sending youtube title: " + title);
                // SendMessage(title);
            } else {
                System.out.println("No title found in document.");
            }
        } catch (IOException e) {
            System.out.println("GetTitle.GetTitle - error opening or reading URL: " + e);
        }
    }

    /**
     * This method will translate the youtube description into plain text.
     * 
     * @param input
     * Youtube description
     * 
     * @return 
     * Plain text of the youtube description
     */
    private String youtubeDescToPlainText(final String input) {
        String output = "";
        int escapeSequence = 0;
        for (int i = 0; i < input.length(); i++) {
            char c = input.charAt(i);
            if (escapeSequence > 0) {
                escapeSequence--;
            } else if (c == '+') {
                output = output + " ";
            } else if (c == '%') {
                escapeSequence = 2;
            } else {
                output = output + c;
            }
        }
        return output;
    }
    
    /**
     * This method will translate a provided link and describe the youtube 
     * video given.
     * 
     * @param msg 
     * A full message from Twitch IRC API from a particular user
     */
    public void handleLinkRequest(final String msg) {
        int startToken = 0;
        int endToken = -1;
        int msgLength = msg.length();
        String token = "";
        for (;;) {
            try {
                startToken = endToken + 1;
                if (endToken >= msgLength - 1) {
                    return;
                }
                endToken = msg.indexOf(" ", startToken);
                if (endToken == -1) {
                    endToken = msgLength;
                }
                token = msg.substring(startToken, endToken);
                if ((token.startsWith("http://")) || (token.startsWith("https://"))) {
                    if ((token.contains("youtube")) || (token.contains("youtu.be"))) {
                        getYoutubeTitle(new URL("http://youtube.com/get_video_info?video_id=" + getYoutubeVideoID(token)));
                        return;
                    }
                }
            } catch (Exception e) {
                LOGGER.severe(e.toString());
            } finally {
            }
        }
    }


}
