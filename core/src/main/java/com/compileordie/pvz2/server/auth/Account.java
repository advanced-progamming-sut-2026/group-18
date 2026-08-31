package com.compileordie.pvz2.server.auth;

/**
 * یک رکورد کاربر سمت سرور. سرور از محتوای واقعی «داده‌ی بازیکن» (سکه، الماس، گیاهان باز شده و ...)
 * هیچ اطلاعی ندارد و نمی‌خواهد هم داشته باشد؛ چون آن ساختار (Player.java) به کلی کلاس‌های
 * مخصوص بازی (PlantType, ZombieType, LevelID, ...) و به کتابخانه‌ی libGDX وابسته است که
 * منطقی نیست سمت سرور (که یک پروژه‌ی خالص جاواست، بدون libGDX) هم وجود داشته باشد.
 *
 * تصمیم معماری فاز ۱: سرور فقط مسئول «احراز هویت» و «نگه‌داری یک بلاب دیتای دلخواه» است.
 * کلاینت (که همه‌ی کلاس‌های دامنه‌ی بازی را دارد) خودش Player را به JSON تبدیل می‌کند،
 * آن‌را Base64 می‌کند، و به‌عنوان یک رشته‌ی مات (opaque string) با PLAYER_STATE_PUSH به سرور می‌فرستد.
 * سرور هم دقیقا همان رشته را موقع لاگین/PLAYER_STATE_PULL برمی‌گرداند؛ کلاینت خودش Base64 و
 * Json.fromJson را روی آن اجرا می‌کند تا Player بسازد.
 */
public class Account {
    private final String username;
    private String passwordHashHex;
    /** رشته‌ی Base64 شده‌ی دیتای بازیکن؛ تا وقتی کلاینت اولین PLAYER_STATE_PUSH را نفرستاده، خالی است. */
    private String playerDataBase64;

    public Account(String username, String passwordHashHex, String playerDataBase64) {
        this.username = username;
        this.passwordHashHex = passwordHashHex;
        this.playerDataBase64 = playerDataBase64 == null ? "" : playerDataBase64;
    }

    public String getUsername() {
        return username;
    }

    public String getPasswordHashHex() {
        return passwordHashHex;
    }

    public void setPasswordHashHex(String passwordHashHex) {
        this.passwordHashHex = passwordHashHex;
    }

    public String getPlayerDataBase64() {
        return playerDataBase64;
    }

    public void setPlayerDataBase64(String playerDataBase64) {
        this.playerDataBase64 = playerDataBase64 == null ? "" : playerDataBase64;
    }
}
