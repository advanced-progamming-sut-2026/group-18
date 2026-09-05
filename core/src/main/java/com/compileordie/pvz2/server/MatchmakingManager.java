package com.compileordie.pvz2.server;

import com.compileordie.pvz2.network.protocol.Message;
import com.compileordie.pvz2.network.protocol.MessageType;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

public class MatchmakingManager {
    private static final ConcurrentLinkedQueue<PendingRequest> PLANT_QUEUE = new ConcurrentLinkedQueue<>();
    private static final ConcurrentLinkedQueue<PendingRequest> ZOMBIE_QUEUE = new ConcurrentLinkedQueue<>();
    private static final ConcurrentHashMap<String, String> ACTIVE_INVITES = new ConcurrentHashMap<>();

    public record PendingRequest(String username, ClientHandler handler, boolean isPlants) {
    }

    public static void handleMatchRequest(ClientHandler sender, Message msg, Server server) {
        String username = sender.getUsername();
        boolean specific = Boolean.parseBoolean(msg.get("specific"));
        boolean isPlants = Boolean.parseBoolean(msg.get("isPlants"));

        if (specific) {
            String targetUser = msg.get("target");

            // 1. Self-invite prevention
            if (targetUser != null && targetUser.equalsIgnoreCase(username)) {
                sender.send(new Message(MessageType.IZOMBIE_MATCH_ERROR)
                    .put("reason", "You cannot invite yourself."));
                return;
            }

            // 2. Non-existent account check
            if (targetUser == null || !server.getAccountStore().exists(targetUser)) {
                sender.send(new Message(MessageType.IZOMBIE_MATCH_ERROR)
                    .put("reason", "User '" + targetUser + "' does not exist."));
                return;
            }

            // 3. Offline account check
            ClientHandler targetHandler = server.getOnlineClient(targetUser);
            if (targetHandler == null) {
                sender.send(new Message(MessageType.IZOMBIE_MATCH_ERROR)
                    .put("reason", "Player '" + targetUser + "' is currently offline."));
                return;
            }

            // 4. Busy status check
            if (targetHandler.isBusy()) {
                sender.send(new Message(MessageType.IZOMBIE_MATCH_ERROR)
                    .put("reason", "Player '" + targetUser + "' is already in another match."));
                return;
            }

            ACTIVE_INVITES.put(targetUser, username);
            targetHandler.send(new Message(MessageType.IZOMBIE_INVITE)
                .put("inviter", username)
                .put("inviterRole", isPlants ? "PLANTS" : "ZOMBIES"));
        } else {
            // Random Matchmaking Queue
            ConcurrentLinkedQueue<PendingRequest> opposingQueue = isPlants ? ZOMBIE_QUEUE : PLANT_QUEUE;
            PendingRequest match = opposingQueue.poll();

            if (match != null && match.handler.isConnected()) {
                startMatch(sender, username, isPlants, match.handler, match.username, match.isPlants);
            } else {
                (isPlants ? PLANT_QUEUE : ZOMBIE_QUEUE).add(new PendingRequest(username, sender, isPlants));
            }
        }
    }

    public static void handleAccept(ClientHandler sender, Message msg, Server server) {
        String receiver = sender.getUsername();

        // Query and consume the active invite mapping (targetUser -> inviterUsername)
        String inviter = ACTIVE_INVITES.remove(receiver);

        if (inviter == null) {
            sender.send(new Message(MessageType.IZOMBIE_MATCH_ERROR)
                .put("reason", "No active invitation found or invite expired."));
            return;
        }

        ClientHandler inviterHandler = server.getOnlineClient(inviter);
        if (inviterHandler != null && inviterHandler.isConnected()) {
            boolean inviterIsPlants = "PLANTS".equals(msg.get("inviterRole"));
            startMatch(inviterHandler, inviter, inviterIsPlants, sender, receiver, !inviterIsPlants);
        } else {
            sender.send(new Message(MessageType.IZOMBIE_MATCH_ERROR)
                .put("reason", "Inviter is no longer online."));
        }
    }

    public static void handleReject(ClientHandler sender, Message msg, Server server) {
        String receiver = sender.getUsername();

        // Query and consume the active invite mapping
        String inviter = ACTIVE_INVITES.remove(receiver);

        if (inviter != null) {
            ClientHandler inviterHandler = server.getOnlineClient(inviter);
            if (inviterHandler != null) {
                inviterHandler.send(new Message(MessageType.IZOMBIE_MATCH_ERROR)
                    .put("reason", receiver + " rejected your invite."));
            }
        }
    }

    public static void handleCancel(ClientHandler sender) {
        String user = sender.getUsername();
        PLANT_QUEUE.removeIf(req -> req.username.equals(user));
        ZOMBIE_QUEUE.removeIf(req -> req.username.equals(user));
        ACTIVE_INVITES.entrySet().removeIf(entry -> entry.getKey().equals(user) || entry.getValue().equals(user));
    }

    private static void startMatch(ClientHandler p1Handler, String p1User, boolean p1IsPlants,
                                   ClientHandler p2Handler, String p2User, boolean p2IsPlants) {
        p1Handler.setBusy(true);
        p2Handler.setBusy(true);

        p1Handler.send(new Message(MessageType.IZOMBIE_MATCH_FOUND)
            .put("opponent", p2User)
            .put("isReceiverClient", String.valueOf(!p1IsPlants)));

        p2Handler.send(new Message(MessageType.IZOMBIE_MATCH_FOUND)
            .put("opponent", p1User)
            .put("isReceiverClient", String.valueOf(!p2IsPlants)));
    }
}
