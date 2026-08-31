package com.compileordie.pvz2.network;

import java.io.IOException;

/**
 * فاز ۱ - گام ۱.۲/۱.۳: به‌جای این‌که هر کنترلر (Login/Signup/...) خودش یک NetworkClient/AuthClient
 * جدا بسازد، همه از همین نقطه‌ی مرکزی استفاده می‌کنند. اولین باری که لازم شود وصل می‌شود؛
 * اگر اتصال قطع شده باشد (مثلا سرور ری‌استارت شده)، دوباره تلاش می‌کند وصل شود.
 * currentSessionUsername همان چیزی است که در گام ۱.۳ به‌عنوان session token تعریف شد
 * (برابر با username) و بعد از لاگین/ثبت‌نام موفق ست می‌شود؛ در فازهای بعدی (لیدربورد،
 * izombie و ...) همین‌جا خوانده می‌شود.
 */
public final class NetworkSession {

    private static AuthClient authClient;
    private static PlayersClient playersClient;
    private static String currentSessionUsername;

    private NetworkSession() {
    }

    public static synchronized AuthClient ensureConnected() throws IOException {
        if (authClient == null || !NetworkClient.getInstance().isConnected()) {
            authClient = new AuthClient(NetworkClient.getInstance());
            authClient.connect(NetworkConfig.SERVER_HOST, NetworkConfig.SERVER_PORT);
        }
        return authClient;
    }

    /**
     * PlayersClient (لیست آنلاین‌ها/همه‌ی کاربرها) روی همون اتصال مشترک NetworkClient کار می‌کند؛
     * برای همین قبل از ساختنش باید مطمئن شویم که یک بار وصل شده‌ایم (ensureConnected).
     */
    public static synchronized PlayersClient getPlayersClient() throws IOException {
        ensureConnected();
        if (playersClient == null) {
            playersClient = new PlayersClient(NetworkClient.getInstance());
        }
        return playersClient;
    }

    public static synchronized void setCurrentSession(String username) {
        currentSessionUsername = username;
    }

    public static synchronized String getCurrentSession() {
        return currentSessionUsername;
    }

    public static synchronized void clearSession() {
        currentSessionUsername = null;
    }
}
