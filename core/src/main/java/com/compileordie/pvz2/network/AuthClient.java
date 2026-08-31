package com.compileordie.pvz2.network;

import com.compileordie.pvz2.network.protocol.Message;
import com.compileordie.pvz2.network.protocol.MessageType;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

/**
 * فاز ۱ - گام ۱.۲: کلاینت را به یک واسط نازک (thin client) تبدیل می‌کنیم.
 * این کلاس همان چیزی است که LoginMenuController/SignupMenuController به‌جای صدا زدن
 * مستقیم AuthManager باید صدا بزنند: به‌جای منطق لوکال، یک پیام به سرور می‌فرستد و
 * منتظر AUTH_RESULT/PLAYER_STATE_RESULT می‌ماند.
 *
 * نکته‌ی مهم درباره‌ی UI: متدهای این کلاس (فعلا) synchronous/blocking پیاده شده‌اند
 * (یعنی تا جواب سرور نیاید برنمی‌گردند) که برای تست خط‌فرمانی این فاز کافی‌ست.
 * برای استفاده‌ی واقعی داخل LoginMenuScreen/SignupMenuScreen (که نباید UI thread را
 * فریز کنند)، باید این فراخوانی‌ها را داخل یک ترد جدا اجرا کنید و نتیجه را با
 * Gdx.app.postRunnable(...) به render thread برگردانید؛ این کار در فاز بعدی (وصل کردن
 * واقعی کنترلرها) انجام می‌شود.
 */
public class AuthClient {

    private static final long DEFAULT_TIMEOUT_MS = 5000;

    private final NetworkClient networkClient;

    public AuthClient(NetworkClient networkClient) {
        this.networkClient = networkClient;
    }

    public void connect(String host, int port) throws IOException {
        networkClient.connect(host, port);
    }

    /** فاز ۱ - گام ۱.۱/۱.۲: ثبت‌نام. خروجی AUTH_RESULT خام سرور است (status, error, session, data). */
    public Message register(String username, String password) {
        Message request = new Message(MessageType.AUTH_REGISTER)
                .put("username", username)
                .put("password", encodePassword(password));
        return sendAndWaitFor(request, MessageType.AUTH_RESULT, MessageType.ERROR);
    }

    /** فاز ۱ - گام ۱.۲/۱.۳/۱.۴: ورود. اگر status=SUCCESS باشد، session و data (آخرین دیتای بازیکن) هم برمی‌گردد. */
    public Message login(String username, String password) {
        Message request = new Message(MessageType.AUTH_LOGIN)
                .put("username", username)
                .put("password", encodePassword(password));
        return sendAndWaitFor(request, MessageType.AUTH_RESULT, MessageType.ERROR);
    }

    /** فاز ۳: کل لیست حساب‌ها (username + آخرین Blob دیتای بازیکن هرکدام) برای رندر لیدربورد. */
    public Message fetchLeaderboard() {
        Message request = new Message(MessageType.LEADERBOARD_REQUEST);
        return sendAndWaitFor(request, MessageType.LEADERBOARD_RESULT, MessageType.ERROR);
    }

    /**
     * فاز ۳ - گام ۳.۱: username های الان آنلاین (یعنی می‌شود همین حالا باهاشان «من، زامبی»
     * شروع کرد - چالش مستقیم یا صف تصادفی). اگر کاربر فعلی خودش لاگین کرده باشد، در این
     * لیست نیست (چون نمی‌تواند با خودش بازی کند). در صورت قطعی/timeout شبکه، لیست خالی
     * برمی‌گردد (نه null) تا کد UI مجبور به چک null نباشد.
     */
    public java.util.List<String> fetchOnlineUsernames() {
        return fetchUserList("ONLINE");
    }

    /**
     * فاز ۳ - گام ۳.۱: همه‌ی username های ثبت‌شده روی سرور، فارغ از آنلاین بودن یا نبودن.
     * در صورت قطعی/timeout شبکه، لیست خالی برمی‌گردد (نه null).
     */
    public java.util.List<String> fetchAllUsernames() {
        return fetchUserList("ALL");
    }

    private java.util.List<String> fetchUserList(String scope) {
        Message request = new Message(MessageType.USER_LIST_REQUEST).put("scope", scope);
        Message result = sendAndWaitFor(request, MessageType.USER_LIST_RESULT, MessageType.ERROR);

        java.util.List<String> usernames = new java.util.ArrayList<>();
        if (result.getType() != MessageType.USER_LIST_RESULT) {
            return usernames;
        }
        int count = parseCountSafely(result.get("count"));
        for (int i = 0; i < count; i++) {
            String username = result.get("user_" + i);
            if (username != null) {
                usernames.add(username);
            }
        }
        return usernames;
    }

    private static int parseCountSafely(String raw) {
        try {
            return raw == null ? 0 : Integer.parseInt(raw);
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    /** فاز ۳: تغییر رمز عبور واقعی روی سرور (قبلا این کار فقط محلی/تزئینی انجام می‌شد). */
    public Message changePassword(String session, String oldPassword, String newPassword) {
        Message request = new Message(MessageType.AUTH_CHANGE_PASSWORD)
                .put("session", session)
                .put("oldPassword", encodePassword(oldPassword))
                .put("newPassword", encodePassword(newPassword));
        return sendAndWaitFor(request, MessageType.AUTH_RESULT, MessageType.ERROR);
    }

    /** فاز ۳: تغییر نام‌کاربری واقعی روی سرور. موفقیت‌آمیز بودنش یعنی session جدید = newUsername است. */
    public Message changeUsername(String session, String newUsername) {
        Message request = new Message(MessageType.AUTH_CHANGE_USERNAME)
                .put("session", session)
                .put("newUsername", newUsername);
        return sendAndWaitFor(request, MessageType.AUTH_RESULT, MessageType.ERROR);
    }

    /**
     * پروتکل سیمی فعلی (Message.toWire) با یک جداکننده‌ی متنی ساده (| ; =) کار می‌کند و پسورد
     * را خام (بدون escape) داخل payload می‌گذارد. چون UserValidator.isPasswordStrong عملا
     * وجود حداقل یک کاراکتر خاص از جمله =  ;  | را *الزامی* می‌کند، یک پسورد کاملا معتبر
     * می‌توانست پیام سیمی را خراب کند. برای جلوگیری از این باگ، پسورد را همیشه Base64
     * می‌کنیم (سرور هم قبل از هش کردن دیکد می‌کند)؛ بقیه‌ی فیلدها (username و ...) نیازی به
     * این کار ندارند چون UserValidator.isUsernameValid از قبل کاراکترها را به [a-zA-Z0-9-] محدود می‌کند.
     */
    private static String encodePassword(String password) {
        return Base64.getEncoder().encodeToString((password == null ? "" : password).getBytes(StandardCharsets.UTF_8));
    }

    /** فاز ۱ - گام ۱.۴: بعد از هر تغییر (خرید و ...)، کل دیتای Player (Base64 شده) را به سرور push می‌کنیم. */
    public Message pushPlayerData(String session, String playerDataBase64) {
        Message request = new Message(MessageType.PLAYER_STATE_PUSH)
                .put("session", session)
                .put("data", playerDataBase64);
        return sendAndWaitFor(request, MessageType.PLAYER_STATE_RESULT, MessageType.ERROR);
    }

    /** درخواست دستی آخرین نسخه‌ی دیتای بازیکن (بیشتر برای دیباگ/تست؛ لاگین خودش data را می‌دهد). */
    public Message pullPlayerData(String session) {
        Message request = new Message(MessageType.PLAYER_STATE_PULL)
                .put("session", session);
        return sendAndWaitFor(request, MessageType.PLAYER_STATE_RESULT, MessageType.ERROR);
    }

    /**
     * پیام را می‌فرستد و تا timeout منتظر اولین پیامی می‌ماند که نوعش یکی از expectedTypes باشد.
     * چون هر AuthClient روی کانکشن خودش کار می‌کند و در این فاز هر درخواست دقیقا یک جواب دارد،
     * این روش ساده (بدون request-id) برای این سطح از پروژه کافی است.
     */
    private Message sendAndWaitFor(Message request, MessageType... expectedTypes) {
        networkClient.send(request);
        long deadline = System.currentTimeMillis() + DEFAULT_TIMEOUT_MS;
        while (System.currentTimeMillis() < deadline) {
            Message incoming = networkClient.poll();
            if (incoming != null) {
                for (MessageType expected : expectedTypes) {
                    if (incoming.getType() == expected) {
                        return incoming;
                    }
                }
                // پیام نامرتبط؛ نادیده گرفته می‌شود (در این فاز پیش نمی‌آید).
            } else {
                try {
                    Thread.sleep(20);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        }
        return new Message(MessageType.ERROR).put("reason", "timeout");
    }

    public void disconnect() {
        networkClient.disconnect();
    }
}
