package net.justonedev.braten;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.events.guild.member.GuildMemberJoinEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

public class GiveRoles extends ListenerAdapter {
    private final String discordUser;
    private final Role moduleRole;
    private final Role semesterRole;

    public GiveRoles(JDA jda, String discordUser) {
        this.discordUser = discordUser;
        var roles = jda.getRoles();
        for (var role : roles) {

        }
    }

    @Override
    public void onGuildMemberJoin(GuildMemberJoinEvent event) {
        super.onGuildMemberJoin(event);

    }
}
