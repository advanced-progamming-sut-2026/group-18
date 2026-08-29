package com.compileordie.pvz2.models.repositories.databases;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.compileordie.pvz2.config.Constants;
import com.compileordie.pvz2.models.AppModel;
import com.compileordie.pvz2.models.repositories.databases.fundamentals.MutableDatabase;
import com.compileordie.pvz2.models.user.Player;

public class UserDatabase extends MutableDatabase<Player> {
    public UserDatabase() {
        this(AppModel.player != null ? AppModel.player.username : null);
    }

    public UserDatabase(String username) {
        super(localPathFor(username));
    }

    /**
     * 🐛 فیکس کرش (NullPointerException در MutableDatabase.save -> Gdx.files.local(null)):
     *
     * قبلا مسیر از AuthManager.getUserPathByUsername(username) می‌اومد که خودش username
     * رو فقط توی رجیستری محلی قدیمی (AuthDatabase / لیست UserRegistry با یه saveField
     * که یه UUID تصادفیه) جست‌وجو می‌کرد. از وقتی ثبت‌نام/ورود از مسیر شبکه (فاز ۱)
     * انجام می‌شه، دیگه هیچ‌جا چیزی توی اون رجیستری محلی نوشته نمی‌شه (SignupMenuController
     * صراحتا دیگه AuthDatabase محلی رو صدا نمی‌زنه، ثبت‌نام واقعی سمت سرور انجام می‌شه).
     * پس برای *هر* کاربری که از این به بعد ثبت‌نام کرده، اون lookup همیشه null برمی‌گردوند
     * -> filePath می‌شد null -> اولین جایی که new UserDatabase().save(...) صدا زده می‌شد
     * (مثلا PlantSelectionMenuController.initializeGame، ولی عملا هر کد دیگه‌ای هم که
     * player رو محلی cache/save می‌کنه) با NullPointerException کرش می‌کرد، چون برخلاف
     * load()، متد save() اصلا چک null نداشت.
     *
     * فیکس: مسیر دیگه از رجیستری محلی لغزنده در نمیاد؛ مستقیم از روی خودِ username ساخته
     * می‌شه. این امن‌ه چون UserValidator.isUsernameValid فقط [a-zA-Z0-9-]+ رو قبول می‌کنه
     * (یعنی برای اسم فایل روی هر سیستم‌عاملی معتبره) و همین‌طور برای هر دو حالت کاربر
     * (چه فقط سمت سرور ثبت‌نام کرده، چه قبلا محلی هم بوده) به یک شکل کار می‌کنه.
     * (به‌عنوان یک لایه‌ی محافظتی اضافه، MutableDatabase.save هم حالا یک null-check
     * مستقل داره تا اگه یه جای دیگه هم همین مشکل پیش اومد، دیگه کل بازی کرش نکنه.)
     */
    private static String localPathFor(String username) {
        if (username == null || username.isEmpty()) {
            return null;
        }
        return Constants.Paths.Saves.USERS + username + ".json";
    }

    @Override
    public Player load() {
        if (filePath != null) {
            FileHandle file = Gdx.files.local(filePath);

            if (file.exists()) {
                return json.fromJson(Player.class, file);
            } else {
                return null;
            }
        } else {
            return null;
        }
    }
}
