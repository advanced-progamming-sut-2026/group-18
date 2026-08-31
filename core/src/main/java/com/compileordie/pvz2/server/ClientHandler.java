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
import java.util.List;
import java.util.Set;

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
            case ONLINE_PLAYERS_REQUEST:
                handleOnlinePlayersRequest(message);
                break;
            case ALL_PLAYERS_REQUEST:
                handleAllPlayersRequest(message);
                break;
            default:
                send(new Message(MessageType.ERROR).put("reason", "not_implemented_yet"));
                break;
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

        String passwordHash = PasswordHasher.sha256Hex(password == null ? "" : password);
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

    /**
     * فاز ۳: تغییر رمز عبور واقعی (قبلا فقط یک فیلد آرگون۲ تزئینی داخل خودِ Player تغییر
     * می‌کرد که هیچ ربطی به هش واقعی احراز هویت سرور - SHA-256 داخل AccountStore - نداشت).
     */
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

    /**
     * فاز ۳: تغییر نام‌کاربری واقعی. چون accounts.tsv با username کلید می‌خورد، این یک
     * rename واقعی سمت AccountStore است؛ بعد از موفقیت، هویت همین کانکشن هم به‌روز می‌شود
     * (this.username) و پاسخ session جدید را برمی‌گرداند تا کلاینت هم NetworkSession خودش
     * را با آن هماهنگ کند - وگرنه فراخوانی‌های بعدی PLAYER_STATE_PUSH/PULL با session قدیمی
     * رد می‌شدند.
     */
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

    /** فاز ۳: ببینید AuthClient.encodePassword برای دلیل Base64 کردن پسورد در پروتکل سیمی. */
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

    /**
     * فاز ۳: لیدربورد ("اطلاعات لیدربورد باید از دادههای ذخیرهشده کاربران در سرور دریافت شود
     * و با تغییر اطلاعات کاربران بهروزرسانی شود"). سرور معنای داخلی Player را نمی‌داند (طبق
     * تصمیم اولیه‌ی پروژه - نگاه کنید AccountValidator)، پس فقط username و همان Blob ای که
     * PLAYER_STATE_PUSH آخرین بار فرستاده را برمی‌گرداند؛ کلاینت خودش (که ساختار Player را
     * می‌شناسد) امتیازها را استخراج، مرتب و رندر می‌کند. چون این داده مستقیما از همان
     * accountsByUsername زنده خوانده می‌شود (نه یک کپی قدیمی/کش‌شده)، هر بار که این پیام
     * دوباره درخواست شود (هر بار که کاربر لیدربورد را باز می‌کند) آخرین نسخه‌ی هر کاربر را
     * می‌دهد.
     */
    private void handleLeaderboardRequest() {
        Set<String> usernames = server.getAccountStore().getAllUsernames();
        Message response = new Message(MessageType.LEADERBOARD_RESULT)
                .put("count", String.valueOf(usernames.size()));
        int i = 0;
        for (String username : usernames) {
            response.put("user_" + i, username);
            response.put("data_" + i, server.getAccountStore().get(username).getPlayerDataBase64());
            i++;
        }
        send(response);
    }

    /**
     * فاز ۱ - گام ۱.۴: کلاینت بعد از هر تغییر مهم (خرید، امتیاز و ...) کل دیتای Player را
     * (به‌صورت Base64 شده) دوباره push می‌کند تا سرور همیشه آخرین نسخه را داشته باشد.
     */
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

    /** درخواست دستی گرفتن آخرین نسخه‌ی دیتای بازیکن (معمولا لازم نیست چون login خودش data را برمی‌گرداند). */
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

    /**
     * لیست username کاربرهایی که الان آنلاین‌اند و می‌شه باهاشون I-Zombie چالش گذاشت.
     * طبق درخواست: خودِ کاربری که این درخواست را فرستاده از لیست حذف می‌شود (نباید بتونه
     * با خودش چالش بذاره). چون هنوز فرمت پیام لیست ندارد، username ها با کاما (,) به هم
     * وصل می‌شوند (کاما تو username مجاز نیست، پس این جداکننده امن است).
     */
    private void handleOnlinePlayersRequest(Message message) {
        Set<String> online = server.getOnlineUsernames();
        if (this.username != null) {
            online.remove(this.username);
        }
        send(new Message(MessageType.ONLINE_PLAYERS_RESULT).put("usernames", String.join(",", online)));
    }

    /** لیست username همه‌ی کاربرهای ثبت‌نام‌شده، فارغ از آنلاین بودن. خودِ کاربر درخواست‌دهنده هم حذف می‌شود. */
    private void handleAllPlayersRequest(Message message) {
        Set<String> all = server.getAllUsernames();
        if (this.username != null) {
            all.remove(this.username);
        }
        send(new Message(MessageType.ALL_PLAYERS_RESULT).put("usernames", String.join(",", all)));
    }

    /** ارسال یک پیام به این کلاینت مشخص (بعدا برای relay بین دو کلاینت هم استفاده می‌شود). */
    public void send(Message message) {
        if (out != null) {
            out.println(message.toWire());
        }
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }
}
