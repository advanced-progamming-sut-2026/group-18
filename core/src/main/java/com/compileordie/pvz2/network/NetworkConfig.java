package com.compileordie.pvz2.network;

/**
 * آدرس سروری که کلاینت باید بهش وصل بشه. فعلا هاردکد روی لوکال‌هاست است (برای تست
 * روی یک سیستم)؛ برای اجرای واقعی روی دو سیستم مختلف، SERVER_HOST را به IP سیستمی
 * که سرور رویش اجرا می‌شود تغییر بدهید (و مطمئن شوید پورت ۵۰۵۰ روی فایروال باز است).
 */
public final class NetworkConfig {
    private NetworkConfig() {
    }

    public static final String SERVER_HOST = "127.0.0.1";
    public static final int SERVER_PORT = 5050;
}
