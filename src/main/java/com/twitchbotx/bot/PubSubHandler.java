/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.twitchbotx.bot;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.io.StringReader;
import java.net.Socket;
import java.net.URI;
import java.util.logging.Logger;
import javax.json.Json;
import javax.json.JsonObject;
import javax.websocket.ClientEndpoint;
import javax.websocket.CloseReason;
import javax.websocket.ContainerProvider;
import javax.websocket.MessageHandler;
import javax.websocket.OnClose;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.WebSocketContainer;

/**
 *
 * @author RaxaStudios
 */

/*
** Manages connection and events from Twitch PubSub feed
** Outputs relay changes in stream video (going live/offline)
** as well as whispers received
** To be passed to Discord handler for stream is live notification
** whispers for filter management
 */

 /*
** Per PubSub API documentation
** send PING at least once per 5 minutes
** reconnect if no PONG message is received within 10 seconds
** RECONNECT messages may be received
** bot should reconnect within 30 seconds of message
 */
public class PubSubHandler {

    private PrintStream out;
    private BufferedReader in;
    private String dataIn;
    private MessageHandler messageHandler;

    private static final Logger LOGGER = Logger.getLogger(TwitchBotX.class.getSimpleName());

    private final ConfigParser.Elements elements;

    private final PrintStream outstream;

    public PubSubHandler(ConfigParser.Elements elements, PrintStream outstream, String host, int port) {
        this.elements = elements;
        this.outstream = outstream;
        try {
            Socket pubS = new Socket(host, port);
            out = new PrintStream(pubS.getOutputStream());
            in = new BufferedReader(new InputStreamReader(pubS.getInputStream()));

        } catch (IOException e) {
            System.out.println("ERROR AT PUBSUB CONNECTION: " + e);
        }
    }

    public static interface MessageHandler {

        JsonObject jsonObject = Json.createReader(new StringReader("")).readObject();
        String content = jsonObject.getString("message");
    }

    public void addMessageHandler(MessageHandler msgHandler) {
        this.messageHandler = msgHandler;
    }

    public void sendWhisper(final String msg) {
        final String message = msg;
        this.outstream.println("PRIVMSG #"
                + this.elements.configNode.getElementsByTagName("myChannel").item(0).getTextContent()
                + " "
                + ":"
                + message);
    }
}
