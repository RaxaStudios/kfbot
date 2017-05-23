package com.twitchbotx.bot.handlers;

import java.util.ArrayList;
import java.util.List;

public final class PyramidDetector {

    private final List<CachedMessage> recentMessages = new ArrayList<>();

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
            System.out.println(this.user + this.msg + " CACHED MESSAGES");
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
     * This method is a quick and dirty solution for pyramid detection.
     *
     * It basically caches the last 15 messages, and user mapping, and does a
     * check on whether or not the user is doing a pattern of 3.
     *
     * @param user A given user that say a given message
     *
     * @param msg A message provided by the user
     */
    public void pyramidDetection(final String user, String msg) {
        recentMessages.add(new CachedMessage(user, msg));
        if (recentMessages.size() > store.getConfiguration().recentMessageCacheSize) {
            recentMessages.remove(0);
        }
        int patternEnd = msg.indexOf(" ");
        String pattern;
        if (patternEnd == -1) {
            pattern = msg;
            System.out.println(pattern + " PATTERN1");
        } else {
            pattern = msg.substring(0, msg.indexOf(" "));
            System.out.println(pattern + " PATTERN2");
        }
        if (!msg.contentEquals(pattern + " " + pattern + " " + pattern)) {
            System.out.println(msg + " IF MSG DOES NOT TEST");
            return;
        }
        int patternCount = 3;
        for (int i = recentMessages.size() - 2; i >= 0; i--) {
            CachedMessage cm = recentMessages.get(i);
            if ((patternCount == 3) && (cm.getMsg().contentEquals(pattern + " " + pattern)) && (cm.getUser().contentEquals(user))) {
                System.out.println(cm.getMsg() + " CACHED MESSAGE PATTERN 2");
                patternCount = 2;
            } else if ((patternCount == 2) && (cm.getMsg().contentEquals(pattern)) && (cm.getUser().contentEquals(user))) {
                sendMessage(store.getConfiguration().pyramidResponse);
                return;
            }
        }
    }
}
