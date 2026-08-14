package com.compileordie.pvz2.models.user.authentication;

import com.compileordie.pvz2.config.Constants;
import com.compileordie.pvz2.models.AppModel;
import com.compileordie.pvz2.models.repositories.databases.AuthDatabase;
import com.compileordie.pvz2.models.repositories.databases.UserDatabase;
import com.compileordie.pvz2.models.user.Gender;
import com.compileordie.pvz2.models.user.Player;
import de.mkammerer.argon2.Argon2;
import de.mkammerer.argon2.Argon2Factory;

import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HexFormat;

import static com.compileordie.pvz2.config.Constants.ArgonHashing;

public class AuthManager {
    private static final Argon2 ARGON_2 = Argon2Factory.create(Argon2Factory.Argon2Types.ARGON2id);

    private AuthManager() {
    }

    public static String hashPassword(char[] plainTextPassword) {
        try {
            return ARGON_2.hash(ArgonHashing.ITERATIONS,
                ArgonHashing.MEMORY,
                ArgonHashing.PARALLELISM,
                plainTextPassword);
        } finally {
            ARGON_2.wipeArray(plainTextPassword);
        }
    }

    public static String hashPasswordSHA256(char[] plainTextPassword) {
        ByteBuffer byteBuffer = null;
        try {
            CharBuffer charBuffer = CharBuffer.wrap(plainTextPassword);
            byteBuffer = StandardCharsets.UTF_8.encode(charBuffer);
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            digest.update(byteBuffer);
            byte[] encodedHash = digest.digest();
            return HexFormat.of().formatHex(encodedHash);

        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 algorithm not found", e);
        } finally {
            if (byteBuffer != null && byteBuffer.hasArray()) {
                Arrays.fill(byteBuffer.array(), (byte) 0);
            }
        }
    }

    public static String getUsernameByField(String field) {
        ArrayList<UserRegistry> userRegistries = new AuthDatabase().load();
        for (UserRegistry userRegistry : userRegistries) {
            if (userRegistry.getSaveField().equals(field)) {
                return userRegistry.getUsername();
            }
        }
        return null;
    }

    public static String getUserFieldByUsername(String username) {
        ArrayList<UserRegistry> userRegistries = new AuthDatabase().load();
        for (UserRegistry userRegistry : userRegistries) {
            if (userRegistry.getUsername().equals(username)) {
                return userRegistry.getSaveField();
            }
        }
        return null;
    }

    public static String getUserPathByUsername(String username) {
        String userField = getUserFieldByUsername(username);
        if (userField != null) {
            return Constants.Paths.Saves.USERS + userField + ".json";
        } else {
            return null;
        }
    }

    public static Player getUserByUsername(String username) {
        return new UserDatabase(username).load();
    }

    public static void addUser(String username,
                               String passwordHash,
                               String nickname,
                               String email,
                               Gender gender,
                               String securityQuestion,
                               String securityAnswer) {
        Player player = new Player(username, passwordHash, nickname, email, gender, securityQuestion, securityAnswer);
        new UserDatabase(username).save(player);
    }

    public static void addRegistry(String username, String hashedPassword) {
        UserRegistry userRegistry = new UserRegistry(username, hashedPassword);
        new AuthDatabase().saveOne(userRegistry);
    }

    public static void signupPlayer(String username,
                                    char[] plainTextPassword,
                                    String nickname,
                                    String email,
                                    Gender gender,
                                    String securityQuestion,
                                    String securityAnswer) {
        String hashedPassword = hashPassword(plainTextPassword);
        addRegistry(username, hashedPassword);
        addUser(username, hashedPassword, nickname, email, gender, securityQuestion, securityAnswer);
    }

    public static boolean checkPassword(char[] plainTextPassword) {
        System.out.println(Arrays.toString(plainTextPassword));
        try {
            return ARGON_2.verify(AppModel.player.passwordHash, plainTextPassword);
        } finally {
            ARGON_2.wipeArray(plainTextPassword);
        }
    }

    public static AuthStatus authenticate(String username, char[] plainTextPassword) {
        UserRegistry user = new AuthDatabase().loadOne(username);
        if (user == null) {
            return AuthStatus.USER_NOT_FOUND;
        }

        try {
            boolean matches = ARGON_2.verify(user.getPasswordHash(), plainTextPassword);
            if (matches) {
                return AuthStatus.SUCCESS;
            } else {
                return AuthStatus.WRONG_PASSWORD;
            }
        } finally {
            ARGON_2.wipeArray(plainTextPassword);
        }
    }

    public static void loginPlayer(String username) {
        AppModel.player = new UserDatabase(username).load();
    }

    public static void setNewPassword(String username, char[] newPassword) {
        String newHash = hashPassword(newPassword);

        AuthDatabase authDb = new AuthDatabase();
        ArrayList<UserRegistry> registries = authDb.load();
        for (UserRegistry userRegistry : registries) {
            if (userRegistry.getUsername().equals(username)) {
                userRegistry.setPasswordHash(newHash);
                break;
            }
        }
        authDb.save(registries);

        UserDatabase database = new UserDatabase(username);
        Player user = database.load();
        user.passwordHash = newHash;
        database.save(user);
        Player player = AppModel.player;
        if (player != null && player.username.equals(username)) AppModel.player.passwordHash = newHash;
    }

    public static void setNewUsername(String newUsername) {
        AuthDatabase authDb = new AuthDatabase();
        ArrayList<UserRegistry> registries = authDb.load();
        for (UserRegistry userRegistry : registries) {
            if (userRegistry.getUsername().equals(AppModel.player.username)) {
                userRegistry.setUsername(newUsername);
                break;
            }
        }
        authDb.save(registries);

        AppModel.player.username = newUsername;
        new UserDatabase().save(AppModel.player);
    }
}
