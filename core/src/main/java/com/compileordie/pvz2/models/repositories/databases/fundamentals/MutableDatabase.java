package com.compileordie.pvz2.models.repositories.databases.fundamentals;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;

abstract public class MutableDatabase<T> extends DatabaseConnector<T> {
    public MutableDatabase(String filePath) {
        super(filePath);
    }

    public void save(T data) {
        // 🛡️ لایه‌ی محافظتی: قبلا اینجا هیچ چکی نبود، پس هر مسیر null (مثلا از یک زیرکلاس
        // دیگه که مشابه UserDatabase از یه lookup ناموفق مسیر می‌سازه) مستقیم به
        // Gdx.files.local(null) می‌رسید که در سازنده‌ی java.io.File یه NullPointerException
        // خام (بدون هیچ پیام قابل‌فهمی) پرتاب می‌کنه و کل اپ رو کرش می‌ده. الان به‌جاش فقط
        // یه خطای خوانا لاگ می‌شه و ذخیره‌ی محلی نادیده گرفته می‌شه (بدون کرش).
        if (filePath == null) {
            Gdx.app.error("PVZ-DATABASE",
                "⚠️ ذخیره‌سازی محلی رد شد چون filePath نامعتبر (null) بود - نوع دیتا: "
                    + (data != null ? data.getClass().getSimpleName() : "null"));
            return;
        }

        FileHandle file = Gdx.files.local(filePath);
        file.writeString(json.prettyPrint(data, 0)
            .replace("\t", "  ")
            .replace("\n", "\n  ")
            .replace("\n  ]", "\n]"), false);
    }
}
