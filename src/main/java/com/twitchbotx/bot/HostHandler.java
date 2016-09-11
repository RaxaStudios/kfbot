package com.twitchbotx.bot;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.LinkedList;
import java.util.Random;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

public final class HostHandler {

//    public void Autohost(final String msg) {
//        onlineCheckTimer.schedule(new ahTimer(),
//                Integer.parseInt(
//                        configNode.getElementsByTagName("onlineCheckTimer").item(0).getTextContent()));
//
//        hostLengthTimer.schedule(new hlTimer(),
//                Integer.parseInt(
//                        configNode.getElementsByTagName("hostLengthTimer").item(0).getTextContent()));
//        if (msg.length() == 9) {
//            pickHostTarget();
//        } else {
//            currentHostTarget = msg.substring("!autohost".length() + 1);
//            try {
//                if (CheckIfLive(currentHostTarget).booleanValue()) {
//                    sendMessage("Attempting to host " + currentHostTarget);
//                    sendMessage("/host " + currentHostTarget);
//                    System.out.println("Hosting " + currentHostTarget + "...");
//                } else {
//                    PickHostTarget();
//                }
//            } catch (Exception e) {
//                LOGGER.severe(e.toString());
//            }
//        }
//    }
//
//    public void unhost(final String msg) {
//        currentHostTarget = "";
//        onlineCheckTimer.cancel();
//        onlineCheckTimer = new Timer();
//        sendMessage("/unhost");
//        System.out.println("autohost stopped...");
//    }
//
//    public void pickHostTarget() {
//        for (int p = 1; p <= 10; p++) {
//            LinkedList liveTargets = new LinkedList();
//            for (int i = 0; i < this.elements.hostTargetNodes.getLength(); i++) {
//                try {
//                    Node n = this.elements.hostTargetNodes.item(i);
//                    Element e = (Element) n;
//                    if (p == Integer.parseInt(e.getAttribute("priority"))) {
//                        if (checkIfLive(n.getTextContent())) {
//                            liveTargets.add(n.getTextContent());
//                        }
//                    }
//                } catch (Exception e) {
//                    System.out.println(e);
//                }
//            }
//            if (liveTargets.size() != 0) {
//                if (Boolean.parseBoolean(this.elements.configNode.getElementsByTagName("randomizeHostTarget").item(0).getTextContent())) {
//                    Random randomGenerator = new Random();
//                    int randomIndex = randomGenerator.nextInt(liveTargets.size());
//                    currentHostTarget = (String) liveTargets.get(randomIndex);
//                    sendMessage("/host " + currentHostTarget);
//                    System.out.println("[Random] Hosting " + currentHostTarget + " ...");
//                    return;
//                }
//                currentHostTarget = (String) liveTargets.getFirst();
//                sendMessage("/host " + currentHostTarget);
//                System.out.println("Hosting " + currentHostTarget + "...");
//                return;
//            }
//        }
//        if (currentHostTarget.contentEquals("")) {
//            System.out.println("No host targets online...");
//        }
//    }
//
//    public boolean checkIfLive(String streamer)
//            throws Exception {
//        String statusURL = this.elements.configNode.getElementsByTagName("twitchStreamerStatus").item(0).getTextContent();
//        statusURL = statusURL.replaceAll("#streamer", streamer);
//        URL url = new URL(statusURL);
//        URLConnection con = url.openConnection();
//        BufferedReader brin = new BufferedReader(new InputStreamReader(con.getInputStream()));
//        StringBuilder response = new StringBuilder();
//        String inputLine;
//        while ((inputLine = brin.readLine()) != null) {
//            response.append(inputLine);
//        }
//        brin.close();
//        if (response.toString().contains("\"stream\":null")) {
//            return false;
//        }
//        return true;
//    }
//
//    public void hostAdd(final String msg) {
//        try {
//            String value = getInputParameter("!host-add", msg, true);
//            if (value.contentEquals(this.elements.configNode.getElementsByTagName("myChannel").item(0).getTextContent())) {
//                sendMessage("Nice try...");
//                return;
//            }
//            for (int i = 0; i < this.elements.hostTargetNodes.getLength(); i++) {
//                Node n = this.elements.hostTargetNodes.item(i);
//                Element e = (Element) n;
//                if (value.contentEquals(e.getTextContent())) {
//                    sendMessage("[" + value + "] already a host target.");
//                    return;
//                }
//            }
//            Element newNode = this.elements.doc.createElement("name");
//            newNode.appendChild(this.elements.doc.createTextNode(value));
//            this.elements.autohostTargets.appendChild(newNode);
//            writeXML();
//
//            sendMessage("[" + value + "] added to host targets.");
//        } catch (IllegalArgumentException e) {
//            sendMessage("Syntax: !host-add [channel]");
//        }
//    }
//
//    public void hostDel(String msg) {
//        try {
//            String value = getInputParameter("!host-delete", msg, true);
//            for (int i = 0; i < this.elements.hostTargetNodes.getLength(); i++) {
//                Node n = this.elements.hostTargetNodes.item(i);
//                Element e = (Element) n;
//                if (value.contentEquals(e.getTextContent())) {
//                    this.elements.autohostTargets.removeChild(n);
//                    writeXML();
//
//                    sendMessage("[" + value + "] removed from host targets.");
//                    return;
//                }
//            }
//            sendMessage("[" + value + "] not a host target.");
//        } catch (IllegalArgumentException e) {
//            sendMessage("Syntax: !host-delete [channel]");
//        }
//    }
//
//    public void hostPriority(String msg) {
//        try {
//            String parameters = getInputParameter("!host-priority", msg, true);
//            int separator = parameters.indexOf(" ");
//            String channel = parameters.substring(0, separator);
//            int priority = Integer.parseInt(parameters.substring(separator + 1));
//            if ((priority < 1) || (priority > 10)) {
//                throw new IllegalArgumentException();
//            }
//            for (int i = 0; i < this.elements.hostTargetNodes.getLength(); i++) {
//                Node n = this.elements.hostTargetNodes.item(i);
//                Element el = (Element) n;
//                if (channel.contentEquals(el.getTextContent())) {
//                    el.setAttribute("priority", Integer.toString(priority));
//                    writeXML();
//                    sendMessage("Host target [" + channel + "] set to priority [" + priority + "]");
//                    return;
//                }
//            }
//            sendMessage("Host target [" + channel + "] not found.");
//        } catch (IllegalArgumentException e) {
//            sendMessage("Syntax: !host-priority [channel] [1-10]");
//        }
//    }
//
//    public void setOnlineCheckTimer(String msg) {
//        try {
//            String value = getInputParameter("!set-onlineCheckTimer", msg, true);
//            int seconds = Integer.parseInt(value);
//            if (seconds < 60) {
//                throw new IllegalArgumentException();
//            }
//            setConfigXML("onlineCheckTimer", Integer.toString(seconds));
//
//            sendMessage("onlineCheckTimer set to [" + seconds + "] seconds.");
//        } catch (IllegalArgumentException e) {
//            sendMessage("Syntax: !set-onlineCheckTimer [seconds] (60 seconds minimum)");
//        }
//    }
//
//    public void setHostLengthTimer(String msg) {
//        try {
//            String value = getInputParameter("!set-hostLengthTimer", msg, true);
//            int seconds = Integer.parseInt(value);
//            if (seconds < 300) {
//                throw new IllegalArgumentException();
//            }
//            setConfigXML("hostLengthTimer", Integer.toString(seconds));
//
//            sendMessage("hostLengthTimer set to [" + seconds + "] seconds.");
//        } catch (IllegalArgumentException e) {
//            sendMessage("Syntax: !set-hostLengthTimer [seconds] (300 seconds minimum)");
//        }
//    }
//
//    public void setRandomizeHostTarget(String msg) {
//        try {
//            String value = getInputParameter("!set-randomizehostTarget", msg, true);
//            if ((!value.contentEquals("true")) && (!value.contentEquals("false"))) {
//                throw new IllegalArgumentException();
//            }
//            setConfigXML("randomizehostTarget", value);
//
//            sendMessage("randomizehostTarget set to [" + value + "]");
//        } catch (IllegalArgumentException e) {
//            sendMessage("Syntax: !set-randomizehostTarget [true|false]");
//        }
//    }
//
//    public void setCycleHostTarget(String msg) {
//        try {
//            String value = getInputParameter("!set-cycleHostTarget", msg, true);
//            if ((!value.contentEquals("true")) && (!value.contentEquals("false"))) {
//                throw new IllegalArgumentException();
//            }
//            setConfigXML("cycleTargets", value);
//
//            sendMessage("cycleHostTarget set to [" + value + "]");
//        } catch (IllegalArgumentException e) {
//            sendMessage("Syntax: !set-cycleHostTarget [true|false]");
//        }
//    }
}
