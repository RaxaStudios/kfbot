package com.twitchbotx.bot;

import java.util.Timer;
import java.util.TimerTask;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 * This class is responsible for timer management.
 */
public final class TimerManagement {
    
//    private Timer onlineCheckTimer = new Timer();
//    private Timer hostLengthTimer = new Timer();
//    private boolean timeForNewHost = false;
//    
//    public void setupPeriodicBroadcast(final NodeList commandNodes) {
//        for (int i = 0; i < commandNodes.getLength(); i++) {
//            Element ce = (Element) commandNodes.item(i);
//            if (Boolean.parseBoolean(ce.getAttribute("repeating")))
//            {
//                Long d = Long.parseLong(ce.getAttribute("initialDelay")) * 1000L;
//                Long l = Long.parseLong(ce.getAttribute("interval")) * 1000L;
//                if (l < 60000L) {
//                    System.out.println("Repeating interval too short for command " + ce.getAttribute("name"));
//                } else {
//                    // new Timer().schedule(new rTimer(ce.getTextContent(), l), d.longValue());
//                }
//            }
//        }
//    }
//    
//    static class ahTimer
//      extends TimerTask
//    {
//      public void run()
//      {
//        com.xtwitchbot.bot.TwitchBotX.onlineCheckTimer.schedule(new ahTimer(), Integer.parseInt(com.xtwitchbot.bot.TwitchBotX.configNode.getElementsByTagName("onlineCheckTimer").item(0).getTextContent()) * 1000);
//        try
//        {
//          if (!com.xtwitchbot.bot.TwitchBotX.CheckIfLive(com.xtwitchbot.bot.TwitchBotX.currentHostTarget).booleanValue()) {
//            // XTwitchBot.access$400();
//          }
//        }
//        catch (Exception e)
//        {
//          com.xtwitchbot.bot.TwitchBotX.LogError(e.toString());
//        }
//      }
//    }
//
//    static class hlTimer
//      extends TimerTask
//    {
//      public void run()
//      {
//        com.xtwitchbot.bot.TwitchBotX.hostLengthTimer.schedule(new hlTimer(), Integer.parseInt(com.xtwitchbot.bot.TwitchBotX.configNode.getElementsByTagName("hostLengthTimer").item(0).getTextContent()) * 1000);
//        if (Boolean.parseBoolean(com.xtwitchbot.bot.TwitchBotX.configNode.getElementsByTagName("cycleTargets").item(0).getTextContent())) {
//          // XTwitchBot.access$400();
//        }
//      }
//    }
//
//    static class rTimer
//      extends TimerTask
//    {
//      String message;
//      long repeatingTimer;
//
//      public rTimer(String msg, long timer)
//      {
//        this.message = msg;
//        this.repeatingTimer = timer;
//      }
//
//      public void run()
//      {
//        new Timer().schedule(new rTimer(this.message, this.repeatingTimer), this.repeatingTimer);
//        com.xtwitchbot.bot.TwitchBotX.SendMessage(this.message);
//      }
//    }
}
