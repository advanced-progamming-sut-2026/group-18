package com.compileordie.pvz2.utils;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.backends.headless.HeadlessApplication;
import com.badlogic.gdx.backends.headless.HeadlessApplicationConfiguration;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.Json;
import com.compileordie.pvz2.config.Constants;
import com.compileordie.pvz2.models.repositories.databases.UserDatabase;
import com.compileordie.pvz2.models.user.Player;

/**
 * Independent migration tool for Team Compile or Die!
 * Run this class directly inside IntelliJ to upgrade all user save files.
 */
public class SaveMigrator {

    public static void main(String[] args) {
        HeadlessApplicationConfiguration config = new HeadlessApplicationConfiguration();

        // We wrap this in a LibGDX HeadlessApplication to safely initialize Gdx.files
        new HeadlessApplication(new ApplicationAdapter() {
            @Override
            public void create() {
                System.out.println("=== Starting Database Migration Scheme ===");

                FileHandle usersDir = Gdx.files.local(Constants.Paths.Saves.USERS);
                if (!usersDir.exists() || !usersDir.isDirectory()) {
                    System.out.println("[ABORT] Users directory does not exist or is empty.");
                    Gdx.app.exit();
                    return;
                }

                FileHandle[] userFiles = usersDir.list(".json");
                System.out.println("Found " + userFiles.length + " profile(s) to process.");

                int successCount = 0;
                Json jsonParser = new Json();

                for (FileHandle file : userFiles) {
                    try {
                        // Deserialize the raw JSON cleanly using our local parser
                        Player player = jsonParser.fromJson(Player.class, file);

                        if (player != null) {
                            System.out.print("Migrating account: '" + player.username + "'... ");

                            // Execute your structural patch algorithms
                            SaveMigrationHelper.migratePlayer(player);

                            // Instantiate a clean writer targeted at this specific user to overwrite disk
                            new UserDatabase(player.username).save(player);

                            System.out.println("SUCCESS.");
                            successCount++;
                        }
                    } catch (Exception e) {
                        System.out.println("FAILED (" + e.getMessage() + ")");
                    }
                }

                System.out.println("=== Migration Complete: Successfully patched " + successCount + "/" + userFiles.length + " file(s) ===");
                Gdx.app.exit();
            }
        }, config);
    }
}
