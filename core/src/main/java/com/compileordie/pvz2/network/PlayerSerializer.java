package com.compileordie.pvz2.network;

import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonWriter.OutputType;
import com.compileordie.pvz2.models.user.Player;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

/**
 * فاز ۱ - گام ۱.۴: تبدیل Player به یک رشته‌ی مات (Base64 روی JSON) برای فرستادن به سرور،
 * و برعکس. سرور خودش هیچ‌وقت این محتوا را باز نمی‌کند (فقط ذخیره‌اش می‌کند)، فقط کلاینت
 * (که کلاس Player و همه‌ی enum های وابسته‌اش مثل PlantType/ZombieType را دارد) این تبدیل را
 * انجام می‌دهد. تنظیمات Json دقیقا مطابق همان چیزی است که DatabaseConnector فعلی استفاده می‌کند
 * (setIgnoreUnknownFields, OutputType.json) تا رفتار سریالایز/دی‌سریالایز یکسان بماند.
 */
public final class PlayerSerializer {

    private PlayerSerializer() {
    }

    private static Json newJson() {
        Json json = new Json();
        json.setOutputType(OutputType.json);
        json.setIgnoreUnknownFields(true);
        return json;
    }

    public static String serialize(Player player) {
        String jsonString = newJson().toJson(player, Player.class);
        return Base64.getEncoder().encodeToString(jsonString.getBytes(StandardCharsets.UTF_8));
    }

    /** اگر ورودی خالی یا نامعتبر باشد null برمی‌گرداند (مثلا کاربری که هنوز هیچ داده‌ای push نکرده). */
    public static Player deserialize(String base64Data) {
        if (base64Data == null || base64Data.isEmpty()) {
            return null;
        }
        try {
            String jsonString = new String(Base64.getDecoder().decode(base64Data), StandardCharsets.UTF_8);
            return newJson().fromJson(Player.class, jsonString);
        } catch (Exception e) {
            return null;
        }
    }
}
