package com.compileordie.pvz2.config;

public final class GamePreferences {
    /**
     * فاز ۳: قبلا اینجا فقط یک "defaultUserField" (یک شناسه‌ی محلی نامرتبط با username،
     * برگرفته از رجیستری محلی قدیمی AuthDatabase) نگه‌داری می‌شد که اجازه می‌داد بدون رمز
     * دوباره وارد شوی (چون صرفا یک فایل محلی خوانده می‌شد). حالا که حساب واقعا روی سرور
     * است، برای ورود خودکار واقعا باید دوباره login بزنیم؛ برای همین نام کاربری و رمز عبور
     * (وقتی کاربر تیک "Stay logged in" را زده) مستقیما همین‌جا نگه داشته می‌شوند تا در
     * اجرای بعدی برنامه دوباره استفاده شوند.
     */
    public String rememberedUsername;
    public String rememberedPassword;

    public GamePreferences() {
    }
}
