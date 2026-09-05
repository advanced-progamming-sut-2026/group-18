package com.compileordie.pvz2.server;

import com.compileordie.pvz2.network.protocol.Message;
import com.compileordie.pvz2.network.protocol.MessageType;
import com.compileordie.pvz2.server.auth.Account;
import com.compileordie.pvz2.server.auth.AccountStore;
import com.compileordie.pvz2.server.auth.AccountValidator;
import com.compileordie.pvz2.server.auth.PasswordHasher;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

/**
 * فاز ۰ - گام ۰.۳: یک Thread جداگانه به‌ازای هر اتصال کلاینت (اجرا شده روی thread-pool سرور).
 * فعلا فقط PING -> PONG را جواب می‌دهد؛ در فاز ۱ به بعد سوییچ روی MessageType کامل می‌شود
 * (AUTH_LOGIN, AUTH_REGISTER, LEADERBOARD_REQUEST, IZOMBIE_* و ...).
 */
public class ClientHandler implements Runnable {

    private final Socket socket;
    private final Server server;
    private PrintWriter out;

    /** بعد از لاگین موفق (فاز ۱) پر می‌شود؛ تا آن موقع null است. */
    private String username;

    /** نشان‌دهنده‌ی درگیر بودن کاربر در روم/مسابقه «من، زامبی». */
    private volatile boolean busy = false;

    public ClientHandler(Socket socket, Server server) {
        this.socket = socket;
        this.server = server;
    }

    @Override
    public void run() {
        try (
            BufferedReader in = new BufferedReader(
                new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8));
            PrintWriter writer = new PrintWriter(
                socket.getOutputStream(), true, StandardCharsets.UTF_8)
        ) {
            this.out = writer;
            String line;
            while ((line = in.readLine()) != null) {
                try {
                    Message message = Message.fromWire(line);
                    handleMessage(message);
                } catch (IllegalArgumentException badMessage) {
                    send(new Message(MessageType.ERROR).put("reason", "bad_message"));
                }
            }
        } catch (IOException e) {
            System.out.println("[Server] Disconnected: " + socket.getRemoteSocketAddress());
        } finally {
            server.unregisterOnlineClient(username);
            try {
                socket.close();
            } catch (IOException ignored) {
            }
        }
    }

    /**
     * نقطه‌ی ورود پردازش پیام‌ها. فعلا فقط PING پیاده شده؛ در فاز ۱ به بعد
     * case های AUTH_LOGIN, AUTH_REGISTER, LEADERBOARD_REQUEST, IZOMBIE_* و ... اضافه می‌شوند.
     */
    private void handleMessage(Message message) {
        switch (message.getType()) {
            case PING:
                send(new Message(MessageType.PONG));
                break;
            case AUTH_REGISTER:
                handleRegister(message);
                break;
            case AUTH_LOGIN:
                handleLogin(message);
                break;
            case AUTH_CHANGE_PASSWORD:
                handleChangePassword(message);
                break;
            case AUTH_CHANGE_USERNAME:
                handleChangeUsername(message);
                break;
            case PLAYER_STATE_PUSH:
                handlePlayerStatePush(message);
                break;
            case PLAYER_STATE_PULL:
                handlePlayerStatePull(message);
                break;
            case LEADERBOARD_REQUEST:
                handleLeaderboardRequest();
                break;
            case USER_LIST_REQUEST:
                handleUserListRequest(message);
                break;
            case IZOMBIE_STATE_SYNC:
                handleStateSync(message);
                break;
            case IZOMBIE_GAME_OVER:
                relayToTarget(message);
                break;
            case IZOMBIE_SUN_COLLECT_REQUEST:
                relayToTarget(message);
                break;
            case IZOMBIE_MATCH_REQUEST:
                MatchmakingManager.handleMatchRequest(this, message, server);
                break;
            case IZOMBIE_ACCEPT:
                MatchmakingManager.handleAccept(this, message, server);
                break;
            case IZOMBIE_REJECT:
                MatchmakingManager.handleReject(this, message, server);
                break;
            case IZOMBIE_CANCEL:
                MatchmakingManager.handleCancel(this);
                break;
            case IZOMBIE_SPAWN_REQUEST:
                handleSpawnRequest(message);
                break;
            case REACTION_SEND:
                handleReaction(message);
                break;
            default:
                send(new Message(MessageType.ERROR).put("reason", "not_implemented_yet"));
                break;
        }
    }

    private void handleReaction(Message message) {
        String targetUser = message.get("target");
        if (targetUser == null || targetUser.isEmpty()) return;

        ClientHandler targetHandler = server.getOnlineClient(targetUser);
        if (targetHandler != null) {
            targetHandler.send(message);
        }
    }

    private void handleSpawnRequest(Message message) {
        String targetUser = message.get("target");
        if (targetUser == null || targetUser.isEmpty()) return;

        ClientHandler targetHandler = server.getOnlineClient(targetUser);
        if (targetHandler != null) {
            targetHandler.send(message);
        }
    }

    /**
     * هر پیامی که فقط با یک فیلد "target" (username حریف) باید عیناً به همان یک نفر
     * relay بشه (بدون این‌که سرور محتوایش رو تفسیر کنه) - دقیقاً همان رفتاری که
     * handleSpawnRequest/handleStateSync هم از قبل داشتن؛ IZOMBIE_GAME_OVER و
     * IZOMBIE_SUN_COLLECT_REQUEST هم از همین الگو استفاده می‌کنن.
     */
    private void relayToTarget(Message message) {
        String targetUser = message.get("target");
        if (targetUser == null || targetUser.isEmpty()) return;

        ClientHandler targetHandler = server.getOnlineClient(targetUser);
        if (targetHandler != null) {
            targetHandler.send(message);
        }
    }

    /** فاز ۱ - گام ۱.۱: ثبت‌نام. یکتایی username اینجا (سمت سرور) نهایی چک می‌شود. */
    private void handleRegister(Message message) {
        AccountStore store = server.getAccountStore();
        String username = message.get("username");
        String password = decodePassword(message.get("password"));

        String usernameError = AccountValidator.validateUsername(username);
        if (usernameError != null) {
            send(new Message(MessageType.AUTH_RESULT).put("status", "INVALID_USERNAME").put("error", usernameError));
            return;
        }
        String passwordError = AccountValidator.validatePassword(password);
        if (passwordError != null) {
            send(new Message(MessageType.AUTH_RESULT).put("status", "INVALID_PASSWORD").put("error", passwordError));
            return;
        }
        if (store.exists(username)) {
            send(new Message(MessageType.AUTH_RESULT).put("status", "USERNAME_TAKEN"));
            return;
        }

        String passwordHash = PasswordHasher.sha256Hex(password);
        store.createAccount(username, passwordHash);

        this.username = username;
        server.registerOnlineClient(username, this);

        send(new Message(MessageType.AUTH_RESULT)
            .put("status", "SUCCESS")
            .put("session", username)
            .put("data", ""));
    }

    /** فاز ۱ - گام ۱.۲ و ۱.۴: ورود؛ اگر موفق بود، آخرین دیتای بازیکن هم همراه پاسخ برگردانده می‌شود. */
    private void handleLogin(Message message) {
        AccountStore store = server.getAccountStore();
        String username = message.get("username");
        String password = decodePassword(message.get("password"));

        Account account = store.get(username);
        if (account == null) {
            send(new Message(MessageType.AUTH_RESULT).put("status", "USER_NOT_FOUND"));
            return;
        }

        String passwordHash = PasswordHasher.sha256Hex(password);
        if (!account.getPasswordHashHex().equals(passwordHash)) {
            send(new Message(MessageType.AUTH_RESULT).put("status", "WRONG_PASSWORD"));
            return;
        }

        this.username = username;
        server.registerOnlineClient(username, this);

        send(new Message(MessageType.AUTH_RESULT)
            .put("status", "SUCCESS")
            .put("session", username)
            .put("data", account.getPlayerDataBase64()));
    }

    private void handleChangePassword(Message message) {
        String session = message.get("session");
        if (session == null || !session.equals(this.username)) {
            send(new Message(MessageType.ERROR).put("reason", "invalid_session"));
            return;
        }

        AccountStore store = server.getAccountStore();
        Account account = store.get(session);
        if (account == null) {
            send(new Message(MessageType.AUTH_RESULT).put("status", "USER_NOT_FOUND"));
            return;
        }

        String oldPassword = decodePassword(message.get("oldPassword"));
        if (!account.getPasswordHashHex().equals(PasswordHasher.sha256Hex(oldPassword))) {
            send(new Message(MessageType.AUTH_RESULT).put("status", "WRONG_PASSWORD"));
            return;
        }

        String newPassword = decodePassword(message.get("newPassword"));
        String passwordError = AccountValidator.validatePassword(newPassword);
        if (passwordError != null) {
            send(new Message(MessageType.AUTH_RESULT).put("status", "INVALID_PASSWORD").put("error", passwordError));
            return;
        }

        store.updatePassword(session, PasswordHasher.sha256Hex(newPassword));
        send(new Message(MessageType.AUTH_RESULT).put("status", "SUCCESS").put("session", session));
    }

    private void handleChangeUsername(Message message) {
        String session = message.get("session");
        if (session == null || !session.equals(this.username)) {
            send(new Message(MessageType.ERROR).put("reason", "invalid_session"));
            return;
        }

        String newUsername = message.get("newUsername");
        String usernameError = AccountValidator.validateUsername(newUsername);
        if (usernameError != null) {
            send(new Message(MessageType.AUTH_RESULT).put("status", "INVALID_USERNAME").put("error", usernameError));
            return;
        }

        AccountStore store = server.getAccountStore();
        if (!newUsername.equals(session) && store.exists(newUsername)) {
            send(new Message(MessageType.AUTH_RESULT).put("status", "USERNAME_TAKEN"));
            return;
        }

        if (!store.renameAccount(session, newUsername)) {
            send(new Message(MessageType.ERROR).put("reason", "rename_failed"));
            return;
        }

        server.unregisterOnlineClient(this.username);
        this.username = newUsername;
        server.registerOnlineClient(newUsername, this);

        send(new Message(MessageType.AUTH_RESULT).put("status", "SUCCESS").put("session", newUsername));
    }

    private static String decodePassword(String encoded) {
        if (encoded == null || encoded.isEmpty()) {
            return "";
        }
        try {
            return new String(Base64.getDecoder().decode(encoded), StandardCharsets.UTF_8);
        } catch (IllegalArgumentException e) {
            return "";
        }
    }

    private void handleLeaderboardRequest() {
        java.util.List<Account> accounts = server.getAccountStore().getAllAccounts();
        Message response = new Message(MessageType.LEADERBOARD_RESULT)
            .put("count", String.valueOf(accounts.size()));
        int i = 0;
        for (Account account : accounts) {
            response.put("user_" + i, account.getUsername());
            response.put("data_" + i, account.getPlayerDataBase64());
            i++;
        }
        send(response);
    }

    private void handleUserListRequest(Message message) {
        String scope = message.get("scope");
        java.util.List<String> usernames;

        if ("ONLINE".equals(scope)) {
            usernames = new java.util.ArrayList<>(server.getOnlineUsernames());
            usernames.remove(this.username);
        } else if ("ALL".equals(scope)) {
            usernames = new java.util.ArrayList<>();
            for (Account account : server.getAccountStore().getAllAccounts()) {
                usernames.add(account.getUsername());
            }
        } else {
            send(new Message(MessageType.ERROR).put("reason", "invalid_scope"));
            return;
        }

        Message response = new Message(MessageType.USER_LIST_RESULT)
            .put("scope", scope)
            .put("count", String.valueOf(usernames.size()));
        for (int i = 0; i < usernames.size(); i++) {
            response.put("user_" + i, usernames.get(i));
        }
        send(response);
    }

    private void handlePlayerStatePush(Message message) {
        String session = message.get("session");
        String data = message.get("data");
        if (session == null || !session.equals(this.username)) {
            send(new Message(MessageType.ERROR).put("reason", "invalid_session"));
            return;
        }
        server.getAccountStore().updatePlayerData(session, data == null ? "" : data);
        send(new Message(MessageType.PLAYER_STATE_RESULT).put("status", "SUCCESS"));
    }

    private void handlePlayerStatePull(Message message) {
        String session = message.get("session");
        if (session == null || !session.equals(this.username)) {
            send(new Message(MessageType.ERROR).put("reason", "invalid_session"));
            return;
        }
        Account account = server.getAccountStore().get(session);
        String data = account == null ? "" : account.getPlayerDataBase64();
        send(new Message(MessageType.PLAYER_STATE_RESULT).put("status", "SUCCESS").put("data", data));
    }

    public void send(Message message) {
        if (out != null) {
            out.println(message.toWire());
        }
    }

    private void handleStateSync(Message message) {
        String targetUser = message.get("target");
        if (targetUser == null || targetUser.isEmpty()) return;

        ClientHandler targetHandler = server.getOnlineClient(targetUser);
        if (targetHandler != null) {
            targetHandler.send(message);
        }
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public boolean isBusy() {
        return busy;
    }

    public void setBusy(boolean busy) {
        this.busy = busy;
    }

    public boolean isConnected() {
        return socket != null && socket.isConnected() && !socket.isClosed();
    }
}
