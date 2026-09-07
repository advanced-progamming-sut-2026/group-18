package com.compileordie.pvz2.network;

import com.compileordie.pvz2.network.protocol.Message;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

/**
 * تست کامل و خودکار فاز ۱.
 * قبل از اجرا، سرور باید روی 127.0.0.1:5050 در حال اجرا باشد.
 *
 * سناریوهایی که چک می‌شود:
 *  ۱. ثبت‌نام کاربر جدید با username/password معتبر -> باید SUCCESS بدهد.
 *  ۲. push کردن یک «دیتای بازیکن» ساختگی (شبیه‌سازِ Json.toJson(player)) -> SUCCESS.
 *  ۳. لاگین همان کاربر از یک اتصال کاملا جدا (شبیه‌سازی «دستگاه دوم») -> باید همان
 *     دیتایی که در قدم ۲ push شده بود را برگرداند. (این دقیقا گام ۱.۴ داکیومنت است:
 *     «اگه کاربر از دستگاه دیگه‌ای وارد شد، همون مقادیر رو ببینه».)
 *  ۴. تلاش برای ثبت‌نام دوباره با همان username -> باید USERNAME_TAKEN بدهد (گام ۱.۵: یکتایی).
 *  ۵. لاگین با رمز غلط -> باید WRONG_PASSWORD بدهد.
 *  ۶. لاگین با کاربر ناموجود -> باید USER_NOT_FOUND بدهد.
 *  ۷. ثبت‌نام با username نامعتبر (شامل فاصله) -> باید INVALID_USERNAME بدهد.
 *  ۸. ثبت‌نام با رمز ضعیف -> باید INVALID_PASSWORD بدهد.
 */
public class Phase1Test {

    private static int passed = 0;
    private static int failed = 0;

    public static void main(String[] args) throws Exception {
        String host = "127.0.0.1";
        int port = 5050;
        String username = "aref-test-" + System.currentTimeMillis(); // یکتا بین اجراهای مختلف تست
        String password = "Str0ng!Pass";
        String fakePlayerJson = "{\"username\":\"" + username + "\",\"coins\":150,\"diamonds\":7}";
        String fakePlayerBase64 = Base64.getEncoder().encodeToString(fakePlayerJson.getBytes(StandardCharsets.UTF_8));

        // --- کلاینت شماره ۱ (مثلا "دستگاه اول") ---
        AuthClient deviceOne = new AuthClient(NetworkClient.freshInstanceForTesting());
        deviceOne.connect(host, port);

        Message registerResult = deviceOne.register(username, password);
        check("۱) ثبت‌نام کاربر جدید",
                "SUCCESS".equals(registerResult.get("status")),
                registerResult);

        String session = registerResult.get("session");
        check("۱.۱) session برابر username است",
                username.equals(session),
                registerResult);

        Message pushResult = deviceOne.pushPlayerData(session, fakePlayerBase64);
        check("۲) push کردن دیتای بازیکن",
                "SUCCESS".equals(pushResult.get("status")),
                pushResult);

        deviceOne.disconnect();

        // --- کلاینت شماره ۲ (شبیه‌سازی "دستگاه دوم") - یک اتصال کاملا جدا ---
        AuthClient deviceTwo = new AuthClient(NetworkClient.freshInstanceForTesting());
        deviceTwo.connect(host, port);

        Message loginResult = deviceTwo.login(username, password);
        check("۳) لاگین از دستگاه دوم -> SUCCESS",
                "SUCCESS".equals(loginResult.get("status")),
                loginResult);

        String returnedData = loginResult.get("data");
        String decoded = returnedData == null ? "" : new String(Base64.getDecoder().decode(returnedData),
            StandardCharsets.UTF_8);
        check("۳.۱) دیتای بازگشتی از دستگاه دوم دقیقا همان چیزیه که دستگاه اول push کرده بود",
                fakePlayerJson.equals(decoded),
                "expected=" + fakePlayerJson + " | actual=" + decoded);

        // --- ۴) تلاش برای ثبت‌نام دوباره با همون username ---
        Message duplicateRegister = deviceTwo.register(username, password);
        check("۴) ثبت‌نام تکراری -> USERNAME_TAKEN",
                "USERNAME_TAKEN".equals(duplicateRegister.get("status")),
                duplicateRegister);

        // --- ۵) لاگین با رمز غلط ---
        Message wrongPassword = deviceTwo.login(username, "ThisIsWrong!123");
        check("۵) لاگین با رمز غلط -> WRONG_PASSWORD",
                "WRONG_PASSWORD".equals(wrongPassword.get("status")),
                wrongPassword);

        // --- ۶) لاگین با کاربر ناموجود ---
        Message notFound = deviceTwo.login("no-such-user-xyz", "whatever123!A");
        check("۶) لاگین کاربر ناموجود -> USER_NOT_FOUND",
                "USER_NOT_FOUND".equals(notFound.get("status")),
                notFound);

        // --- ۷) ثبت‌نام با username نامعتبر ---
        Message invalidUsername = deviceTwo.register("bad username with space", password);
        check("۷) username نامعتبر -> INVALID_USERNAME",
                "INVALID_USERNAME".equals(invalidUsername.get("status")),
                invalidUsername);

        // --- ۸) ثبت‌نام با پسورد ضعیف ---
        Message invalidPassword = deviceTwo.register("brand-new-user-" + System.currentTimeMillis(), "weak");
        check("۸) پسورد ضعیف -> INVALID_PASSWORD",
                "INVALID_PASSWORD".equals(invalidPassword.get("status")),
                invalidPassword);

        deviceTwo.disconnect();

        System.out.println();
        System.out.println("================================");
        System.out.println("نتیجه: " + passed + " موفق / " + failed + " ناموفق");
        System.out.println("================================");
        if (failed > 0) {
            System.exit(1);
        }
    }

    private static void check(String testName, boolean condition, Object debugInfo) {
        if (condition) {
            passed++;
            System.out.println("[PASS] " + testName);
        } else {
            failed++;
            System.out.println("[FAIL] " + testName + "   -> " + debugInfo);
        }
    }
}
