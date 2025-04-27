package net.justonedev.braten;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.events.guild.member.GuildMemberJoinEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;

import java.time.LocalDateTime;

public class GiveRoles extends ListenerAdapter {
    private final String discordUser;
    private final Role moduleRole;
    private final Role semesterRole;

    private static final String moduleRoleName = "Algorithmen";
    private static final String semesterRoleName = "Algo %s";

    public GiveRoles(JDA jda, String discordUser) {
        this.discordUser = discordUser;
        var roles = jda.getRoles();
        String semester = semesterRoleName.formatted(getCurrentSemester());

        Role moduleRole = null;
        Role semesterRole = null;

        for (var role : roles) {
            if (role.getName().equals(moduleRoleName)) {
                Main.log("Found module role: %s".formatted(role.getName()));
                moduleRole = role;
            } else if (role.getName().equals(semester)) {
                Main.log("Found semester role: %s".formatted(role.getName()));
                semesterRole = role;
            }
        }

        if (moduleRole == null) {
            Main.log("Could not find module role, expected role of name %s".formatted(moduleRoleName));
        }
        if (semesterRole == null) {
            Main.log("Could not find semester role, expected role of name %s".formatted(semester));
        }

        this.moduleRole = moduleRole;
        this.semesterRole = semesterRole;
    }

    @Override
    public void onGuildMemberJoin(@NotNull GuildMemberJoinEvent event) {
        super.onGuildMemberJoin(event);
        event.getGuild().addRoleToMember(event.getMember(), moduleRole).complete();
        event.getGuild().addRoleToMember(event.getMember(), semesterRole).complete();

    }

    private static String getCurrentSemester() {
        LocalDateTime time = LocalDateTime.now();
        int month = time.getMonthValue();
        var year = time.getYear() % 2000 - (month < 4 ? 1 : 0);
        if (month < 4 || month > 9) {
            // Winter semester
            return "%d/%d".formatted(year, year + 1);
        } else {
            return "%d".formatted(year);
        }
    }
}
