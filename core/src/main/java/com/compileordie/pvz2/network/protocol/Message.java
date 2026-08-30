package com.compileordie.pvz2.network.protocol;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * فاز ۰ - گام ۰.۲
 * قرارداد پایه‌ی همه‌ی پیام‌های رد و بدل شده بین کلاینت و سرور.
 * منطقا معادل همان {"type": "...", "payload": {...}} پیشنهاد شده در توضیحات است،
 * با این تفاوت که برای سادگی (و برای این‌که سرور مجبور نباشد به کتابخانه‌ی JSON کلاینت
 * (Gdx.Json که وابسته به Application Context است) وابسته باشد) خودمان یک سریالایز
 * متنیِ خیلی ساده و تک‌خطی می‌نویسیم:
 *
 *   TYPE|key1=value1;key2=value2;key3=value3\n
 *
 * هر پیام دقیقا در یک خط ارسال می‌شود (چون سمت سرور/کلاینت با BufferedReader.readLine
 * می‌خوانیم). مقادیر نباید شامل کاراکترهای |  ;  = یا newline باشند؛ در همین حد برای
 * این پروژه کافی است و در فازهای بعدی در صورت نیاز به مقادیر پیچیده‌تر (مثلا snapshot کامل
 * وضعیت بازی در فاز ۴)، آن‌ها را به‌صورت جدا سریالایز کرده و به‌عنوان یک مقدار رشته‌ای
 * (مثلا با جداکننده‌ی دیگر یا Base64) داخل payload می‌گذاریم.
 */
public class Message {

    private final MessageType type;
    private final Map<String, String> payload;

    public Message(MessageType type) {
        this(type, new LinkedHashMap<>());
    }

    public Message(MessageType type, Map<String, String> payload) {
        this.type = type;
        this.payload = payload;
    }

    public MessageType getType() {
        return type;
    }

    public String get(String key) {
        return payload.get(key);
    }

    public Message put(String key, String value) {
        payload.put(key, value);
        return this;
    }

    public Map<String, String> getPayload() {
        return payload;
    }

    /** سریالایز پیام به یک خط متنی برای ارسال روی سوکت. */
    public String toWire() {
        StringBuilder sb = new StringBuilder();
        sb.append(type.name()).append('|');
        boolean first = true;
        for (Map.Entry<String, String> entry : payload.entrySet()) {
            if (!first) sb.append(';');
            first = false;
            sb.append(entry.getKey()).append('=').append(entry.getValue());
        }
        return sb.toString();
    }

    /** دی‌سریالایز یک خط متنی دریافتی از سوکت به Message. */
    public static Message fromWire(String line) {
        if (line == null || line.isEmpty()) {
            throw new IllegalArgumentException("empty message line");
        }
        int barIndex = line.indexOf('|');
        String typeStr = barIndex == -1 ? line : line.substring(0, barIndex);
        MessageType type = MessageType.valueOf(typeStr);
        Map<String, String> payload = new LinkedHashMap<>();
        if (barIndex != -1 && barIndex + 1 < line.length()) {
            String body = line.substring(barIndex + 1);
            for (String pair : body.split(";")) {
                if (pair.isEmpty()) continue;
                int eq = pair.indexOf('=');
                if (eq == -1) {
                    payload.put(pair, "");
                } else {
                    payload.put(pair.substring(0, eq), pair.substring(eq + 1));
                }
            }
        }
        return new Message(type, payload);
    }

    @Override
    public String toString() {
        return toWire();
    }
}
