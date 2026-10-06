package net.justonedev.braten;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.managers.Presence;
import net.dv8tion.jda.api.requests.GatewayIntent;

import java.util.List;

public class DiscordJDA {

    private static final String DISCORD_TITLE = "public void TasseKaffee()";
    private static final String DISCORD_SERVER_ID = "1301576860504817745";
    private static final List<String> DISCORD_TEST_SERVER_IDS = List.of("647837867292491786", "1556667150075302009");

    private static String name = "Bratensoße";

    public DiscordJDA(String token, String discordUser) throws InterruptedException {
        JDA jda = JDABuilder.createDefault(token).enableIntents(GatewayIntent.GUILD_MEMBERS).build().awaitReady();
        jda.addEventListener(new GiveRoles(discordUser));
        jda.addEventListener(new SemesterSetup(jda));
        name = jda.getSelfUser().getName();

        Presence presence = jda.getPresence();
        //presence.setActivity(Activity.watching("einem O(n!) Algorithmus zu"));
        //presence.setActivity(Activity.competing("ICPC contest"));
        presence.setActivity(Activity.playing("IntelliJ IDEA"));
        presence.setStatus(OnlineStatus.ONLINE);
    }

    public static String getName() {
        return name;
    }

    public static boolean isProductionServer(Guild guild) {
        if (guild == null) return false;
        return guild.getId().equals(DISCORD_SERVER_ID);
    }

    public static boolean isTestServer(Guild guild) {
        if (guild == null) return false;
        return DISCORD_TEST_SERVER_IDS.contains(guild.getId());
    }

    public static boolean isListedServer(Guild guild) {
        return isProductionServer(guild) || isTestServer(guild);
    }

}
