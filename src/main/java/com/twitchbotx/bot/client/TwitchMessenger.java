package com.twitchbotx.bot.client;

import java.io.PrintStream;

/**
 * This is a Twitch messenger for all outbound twitch messages.
 */
public final class TwitchMessenger {

    private final PrintStream outstream;

    private final String channel;

    public TwitchMessenger(final PrintStream stream, final String channel) {
        this.outstream = stream;
        this.channel = channel;
    }

    /**
     * This method will send a whisper out to a particular user.
     *
     * @TODO I believe this method is currently incomplete, missing user to be sent to.
     *
     * @param msg The message to be sent out to the channel.
     */
    private void sendWhisper(final String msg) {
        final String message = msg;
        this.outstream.println("PRIVMSG #"
                + channel
                + " "
                + ":"
                + message);
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
                + channel
                + " "
                + ":"
                + message);
    }
}
