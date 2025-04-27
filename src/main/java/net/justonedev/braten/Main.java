package net.justonedev.braten;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Main {
    private static final DateTimeFormatter DATE_LOG_FORMAT = DateTimeFormatter.ofPattern("HH:mm:ss");
    private static final String DEFAULT_DISCORD_USER = "justonedeveloper";

    public static void main(String[] args) {
        // Get Token:
        try {
            var file = Files.readAllLines(new File("discord.credentials").toPath());
            if (file.isEmpty()) {
                log("No token found in .credentials file.");
                return;
            }
            String token = file.getFirst();
            String discordUser = file.size() > 1 ? file.get(1) : DEFAULT_DISCORD_USER;
            new DiscordJDA(token, discordUser);
        } catch (IOException e) {
            log("Error reading credentials file: " + e.getMessage());
        } catch (InterruptedException e) {
            log("The JDA was interrupted: " + e.getMessage());
        }
    }

    public static void log(String s) {
        System.out.printf("[%s] %s%n", DATE_LOG_FORMAT.format(LocalDateTime.now()), s);
    }
}
