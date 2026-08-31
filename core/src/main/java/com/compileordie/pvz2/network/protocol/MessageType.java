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

    // فاز ۳ - تطبیق حریف «من، زامبی»
    ONLINE_PLAYERS_REQUEST,
    ONLINE_PLAYERS_RESULT,
    ALL_PLAYERS_REQUEST,
    ALL_PLAYERS_RESULT,
    IZOMBIE_CHALLENGE_REQUEST,
    IZOMBIE_CHALLENGE_INCOMING,
    IZOMBIE_CHALLENGE_RESPONSE,
    IZOMBIE_CHALLENGE_ERROR,
    IZOMBIE_RANDOM_QUEUE_JOIN,
    IZOMBIE_MATCH_FOUND,

    // فاز ۴ - همگام‌سازی لحظه‌ای بازی
    IZOMBIE_ACTION,
    IZOMBIE_STATE_SYNC,
    IZOMBIE_GAME_OVER,

    // فاز ۵ / ۷ - واکنش در حین بازی
    REACTION_SEND,

    // فاز ۸ - بازی امتیازی تحت شبکه
    SCORE_SUBMIT
}
