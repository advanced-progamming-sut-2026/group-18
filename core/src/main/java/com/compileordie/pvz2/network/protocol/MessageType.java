package com.compileordie.pvz2.network.protocol;

/**
 * فاز ۰ - گام ۰.۲
 * لیست اولیه‌ی type های پیام مشترک بین کلاینت و سرور.
 * این فایل باید عینا (byte-identical) هم در پروژه‌ی کلاینت و هم در پروژه‌ی سرور وجود داشته باشد
 * (چون پروتکل ارتباطی روی این enum تعریف می‌شود). فعلا خیلی از این‌ها placeholder هستند
 * و در فازهای بعدی معنا/payload‌شان پیاده‌سازی می‌شود.
 */
public enum MessageType {
    // زیرساخت / تست اتصال
    PING,
    PONG,
    ERROR,

    // فاز ۱ - حساب کاربری
    AUTH_LOGIN,
    AUTH_REGISTER,
    AUTH_RESULT,
    AUTH_CHANGE_PASSWORD,
    AUTH_CHANGE_USERNAME,
    PLAYER_STATE_PUSH,
    PLAYER_STATE_PULL,
    PLAYER_STATE_RESULT,

    // فاز ۲ - لیدربورد
    LEADERBOARD_REQUEST,
    LEADERBOARD_RESULT,

    // فاز ۳ - گام ۳.۱: لیست کاربران، برای صفحه‌ی انتخاب حریف «من، زامبی»
    // (scope=ONLINE در payload یعنی فقط آنلاین‌ها، scope=ALL یعنی همه‌ی حساب‌ها)
    USER_LIST_REQUEST,
    USER_LIST_RESULT,

    // فاز ۳ - تطبیق حریف «من، زامبی»
    IZOMBIE_MATCH_FOUND,
    IZOMBIE_INVITE,
    IZOMBIE_MATCH_ERROR,
    IZOMBIE_MATCH_REQUEST,
    IZOMBIE_ACCEPT,
    IZOMBIE_REJECT,
    IZOMBIE_CANCEL,
    IZOMBIE_SPAWN_REQUEST,

    // فاز ۴ - همگام‌سازی لحظه‌ای بازی
    IZOMBIE_ACTION,
    IZOMBIE_STATE_SYNC,
    IZOMBIE_GAME_OVER,
    // I_ZOMBIE only: زامبی‌ساید (isReceiverClient=true) هرگز gameBoard خودش را authoritative
    // نمی‌داند (۲۰ بار در ثانیه overwrite می‌شود)، پس نمی‌تواند خودش خورشید جمع کند/سان اضافه
    // کند - فقط درخواست جمع‌آوری برای طرف مقابل (پلنت‌ساید، simulator واقعی) می‌فرستد.
    IZOMBIE_SUN_COLLECT_REQUEST,

    // فاز ۵ / ۷ - واکنش در حین بازی
    REACTION_SEND,

    // فاز ۸ - بازی امتیازی تحت شبکه
    SCORE_SUBMIT
}
