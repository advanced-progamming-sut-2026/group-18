package com.compileordie.pvz2.models.repositories.databases;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.compileordie.pvz2.config.Constants;
import com.compileordie.pvz2.models.AppModel;
import com.compileordie.pvz2.models.repositories.databases.fundamentals.MutableDatabase;
import com.compileordie.pvz2.models.user.Player;
import com.compileordie.pvz2.network.NetworkSession;
import com.compileordie.pvz2.network.PlayerSerializer;
import com.compileordie.pvz2.network.protocol.Message;

import java.io.IOException;

/**
 * فاز ۳ - چرا این کلاس دوباره نوشته شد:
 * قبلا (و بعد از تغییرات هم‌تیمی برای فیکس کرش NPE) این کلاس صرفا یک فایل JSON محلی
 * (Constants.Paths.Saves.USERS + username + ".json") را می‌خواند/می‌نوشت. مشکل: بعد از
 * افزوده شدن سرور (فاز ۱)، فقط signup/login واقعا با سرور صحبت می‌کنند (SignupMenuController
 * یک بار در لحظه‌ی ثبت‌نام PLAYER_STATE_PUSH می‌فرستد؛ LoginMenuController یک بار روی هر
 * لاگین PLAYER_STATE_PULL/داده‌ی همراه AUTH_RESULT می‌گیرد) اما همه‌ی ~۲۰ نقطه‌ی دیگر در کل
 * پروژه (خرید در شاپ، ست کردن تنظیمات، برداشتن کوئست، رسیدن به سطح گلخانه، آنلاک زامبی و ...)
 * هنوز مستقیما new UserDatabase(...).save(player)/.load() صدا می‌زنند که همچنان فقط یک فایل
 * محلی را می‌نوشت. چون بعد از هر لاگین AppModel.player دوباره از روی جواب سرور بازسازی
 * می‌شود، آن فایل محلی عملا "می‌نوشت ولی هیچ‌وقت خوانده نمی‌شد" -> از دید کاربر انگار
 * هیچ‌چیز ذخیره نمی‌شد.
 *
 * راه‌حل: به‌جای عوض کردن تک‌تک آن ~۲۰ فراخوانی (که با این حجم و ددلاین فردا پرریسک است)،
 * خودِ UserDatabase را به یک لایه‌ی سینک با سرور تبدیل کردیم. همه‌ی call-site های موجود
 * بدون هیچ تغییری همین الان با سرور سینک می‌شوند. فایل JSON محلی هنوز نوشته می‌شود، ولی
 * فقط به‌عنوان یک کش/بک‌آپ آفلاین (اگر لحظه‌ی ذخیره اتصال قطع بود) - نه منبع حقیقت.
 *
 * قوانین:
 *  - save(): همیشه اول در کش محلی می‌نویسد (سریع، حتی وقتی آفلاین)، بعد اگر این username
 *    همان کاربرِ لاگین‌شده‌ی سشن فعلی است، دیتا را (روی یک ترد جدا، fire-and-forget) به سرور
 *    push می‌کند. چرا ترد جدا؟ چون این متد از جاهای حساس به لگ هم صدا زده می‌شود (مثلا وسط
 *    GameBoard.tick() وقتی یک زامبی جدید برای اولین‌بار دیده می‌شود)؛ نباید رفت‌وبرگشت شبکه
 *    فریم بازی را فریز کند.
 *  - load(): اگر این username همان کاربرِ لاگین‌شده‌ی سشن فعلی است، سعی می‌کند تازه‌ترین
 *    نسخه را از سرور (PLAYER_STATE_PULL) بگیرد؛ اگر شبکه در دسترس نبود یا این username کاربر
 *    دیگری است (مثلا لیدربورد قدیمی که سعی می‌کند دیتای کاربر دیگری را بخواند)، به کش محلی
 *    برمی‌گردد. توجه: سرور فعلا فقط اجازه‌ی pull برای "خودِ کاربر لاگین‌شده‌ی همین کانکشن" را
 *    می‌دهد (نگاه کنید به ClientHandler.handlePlayerStatePull)، پس load برای کاربر دیگر همیشه
 *    فقط کش محلی (که احتمالا خالی است) را برمی‌گرداند - ساخت یک لیدربورد واقعی سمت سرور
 *    (که در مستند فاز ۳ هم به‌عنوان یک کار جدا خواسته شده) خارج از حوزه‌ی همین فیکس است.
 */
public class UserDatabase extends MutableDatabase<Player> {
    private final String username;

    public UserDatabase() {
        this(AppModel.player != null ? AppModel.player.username : null);
    }

    public UserDatabase(String username) {
        super(localPathFor(username));
        this.username = username;
    }

    private static String localPathFor(String username) {
        if (username == null || username.isEmpty()) {
            return null;
        }
        return Constants.Paths.Saves.USERS + username + ".json";
    }

    /** آیا این نمونه به همان کاربری اشاره دارد که هم‌اکنون روی این کانکشن لاگین کرده است؟ */
    private boolean isCurrentSession() {
        String session = NetworkSession.getCurrentSession();
        return username != null && username.equals(session);
    }

    @Override
    public Player load() {
        if (isCurrentSession()) {
            try {
                Message result = NetworkSession.ensureConnected().pullPlayerData(username);
                if ("SUCCESS".equals(result.get("status"))) {
                    Player player = PlayerSerializer.deserialize(result.get("data"));
                    if (player != null) {
                        // کش محلی را هم تازه نگه می‌داریم تا اگر دفعه‌ی بعد آفلاین بودیم، جا نمانیم.
                        super.save(player);
                        return player;
                    }
                }
            } catch (IOException e) {
                Gdx.app.error("PVZ-USERDB",
                    "Could not reach server to load '" + username + "'; falling back to local cache.");
            }
        }
        return loadLocalCache();
    }

    @Override
    public void save(Player player) {
        if (player == null) {
            return;
        }

        // ۱. کش محلی: سریع و بدون وابستگی به شبکه (دقیقا رفتار قدیمی).
        super.save(player);

        // ۲. سینک واقعی با سرور، فقط برای کاربر لاگین‌شده‌ی همین سشن.
        if (!isCurrentSession()) {
            return;
        }

        String payload = PlayerSerializer.serialize(player);
        Thread pushThread = new Thread(() -> {
            try {
                Message result = NetworkSession.ensureConnected().pushPlayerData(username, payload);
                if (!"SUCCESS".equals(result.get("status"))) {
                    Gdx.app.error("PVZ-USERDB", "Server rejected player-state push for '" + username + "'.");
                }
            } catch (IOException e) {
                Gdx.app.error("PVZ-USERDB",
                    "Could not push player data for '" + username + "' to server; " +
                        "only the local cache has it for now.");
            }
        }, "player-state-push");
        pushThread.setDaemon(true);
        pushThread.start();
    }

    private Player loadLocalCache() {
        if (filePath == null) {
            return null;
        }
        FileHandle file = Gdx.files.local(filePath);
        if (!file.exists()) {
            return null;
        }
        return json.fromJson(Player.class, file);
    }
}
