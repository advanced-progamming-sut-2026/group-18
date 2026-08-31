package com.compileordie.pvz2.network;

import com.compileordie.pvz2.network.protocol.Message;
import com.compileordie.pvz2.network.protocol.MessageType;

import java.util.ArrayList;
import java.util.List;

/**
 * فاز ۳ (زیرساخت): گرفتن لیست کاربرهایی که می‌شه با آن‌ها I-Zombie بازی کرد.
 *
 * دو تا متد داریم:
 *  - getOnlinePlayers(): فقط کاربرهایی که الان به سرور وصل‌اند (کاندید واقعی برای یک چالش زنده).
 *  - getAllPlayers(): همه‌ی کاربرهای ثبت‌نام‌شده، فارغ از آنلاین بودن یا نبودن.
 * در هر دو مورد، خودِ کاربرِ درخواست‌دهنده (طبق session لاگین‌شده) از لیست حذف می‌شود؛
 * این فیلتر سمت سرور انجام می‌شود (نه اینجا)، چون سرور می‌داند این پیام از کدام کانکشن/کاربر آمده.
 *
 * مثل AuthClient، فعلا synchronous/blocking است؛ برای استفاده‌ی واقعی داخل UI (مثلا صفحه‌ی
 * انتخاب حریف در فاز ۳)، این فراخوانی باید در یک ترد جدا اجرا شود و نتیجه با
 * Gdx.app.postRunnable به render thread برگردد.
 */
public class PlayersClient {

    private static final long DEFAULT_TIMEOUT_MS = 5000;

    private final NetworkClient networkClient;

    public PlayersClient(NetworkClient networkClient) {
        this.networkClient = networkClient;
    }

    /** لیست username کاربرهایی که الان آنلاین‌اند (و می‌شه بهشون IZOMBIE_CHALLENGE_REQUEST فرستاد). */
    public List<String> getOnlinePlayers() {
        Message response = sendAndWaitFor(
                new Message(MessageType.ONLINE_PLAYERS_REQUEST),
                MessageType.ONLINE_PLAYERS_RESULT, MessageType.ERROR);
        return parseUsernames(response);
    }

    /** لیست username همه‌ی کاربرهای ثبت‌نام‌شده، فارغ از آنلاین بودن یا نبودن. */
    public List<String> getAllPlayers() {
        Message response = sendAndWaitFor(
                new Message(MessageType.ALL_PLAYERS_REQUEST),
                MessageType.ALL_PLAYERS_RESULT, MessageType.ERROR);
        return parseUsernames(response);
    }

    private List<String> parseUsernames(Message response) {
        List<String> result = new ArrayList<>();
        if (response.getType() == MessageType.ERROR) {
            return result; // خطا/timeout؛ لیست خالی برمی‌گردد (caller می‌تواند دوباره تلاش کند)
        }
        String usernames = response.get("usernames");
        if (usernames == null || usernames.isEmpty()) {
            return result;
        }
        for (String username : usernames.split(",")) {
            if (!username.isEmpty()) {
                result.add(username);
            }
        }
        return result;
    }

    /** دقیقا هم‌الگو با AuthClient.sendAndWaitFor. */
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
}
