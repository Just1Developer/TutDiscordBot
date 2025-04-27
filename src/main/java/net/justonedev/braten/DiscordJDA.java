package net.justonedev.braten;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.managers.Presence;
import net.dv8tion.jda.api.requests.GatewayIntent;

public class DiscordJDA {

    public DiscordJDA(String token, String discordUser) throws InterruptedException {
        JDA jda = JDABuilder.createDefault(token).enableIntents(GatewayIntent.GUILD_MEMBERS).build().awaitReady();
        jda.addEventListener(new GiveRoles(jda, discordUser));

        Presence presence = jda.getPresence();
        presence.setActivity(Activity.watching("an O(n!) algorithm"));
        presence.setStatus(OnlineStatus.ONLINE);
    }

}
