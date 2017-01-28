/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.twitchbotx.bot;

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
    
}
