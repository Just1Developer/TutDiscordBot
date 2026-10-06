package net.justonedev.braten;

import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.channel.concrete.Category;
import net.dv8tion.jda.api.entities.channel.concrete.ForumChannel;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.entities.channel.concrete.VoiceChannel;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.LayoutComponent;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.justonedev.braten.semester.Module;
import net.justonedev.braten.semester.Semester;

import java.util.ArrayList;
import java.util.List;

public class SemesterCreator {

    public static final String BUTTON_SWITCH_PREFIX = "semester-switch:";
    public static final String MESSAGE_TITLE = "## Neues Semester";
    public static final String MESSAGE_CONTENT = "Hey! Ein neues Semester für das Modul **%s** hat angefangen und es gibt neue Channels. Wenn du diese sehen möchtest, klicke auf den Button :)";

    private static final int MAXIMUM_SEMESTERS_LOOKBACK = 3;
    private static final String GENERAL_CHAT_NAME = "allgemein";

    private final Guild guild;
    private final Module module;
    private final Semester semester;

    public SemesterCreator(Guild guild, Module module, Semester semester) {
        this.guild = guild;
        this.module = module;
        this.semester = semester;
    }

    public void create(CreateOptions options) {
        new Thread(() -> {
            if (options.doCreateRole()) createRole();
            if (options.doCreateChannels()) createChannels();
            if (options.doCreateMessage()) createMessage();
        }).start();
    }

    private void createRole() {
        guild.createRole()
                .setName(getSemesterRoleName(module, semester))
                .setPermissions(0L)
                .setMentionable(true)
                .complete();
    }

    private void createChannels() {
        var denyPermissionsEveryone = List.of(Permission.VIEW_CHANNEL, Permission.MESSAGE_MENTION_EVERYONE);
        var allowPermissionsRole = List.of(
                Permission.VIEW_CHANNEL,
                Permission.CREATE_PUBLIC_THREADS,
                Permission.MESSAGE_SEND,
                Permission.MESSAGE_SEND_IN_THREADS,
                Permission.MESSAGE_SEND_POLLS,
                Permission.VOICE_CONNECT,
                Permission.VOICE_SPEAK,
                Permission.VOICE_STREAM,
                Permission.VOICE_USE_VAD,
                Permission.MESSAGE_EXT_EMOJI,
                Permission.MESSAGE_ADD_REACTION,
                Permission.MESSAGE_HISTORY
        );

        // We do complete to ensure the correct order

        long roleId = guild.getRolesByName(getSemesterRoleName(module, semester), false).getFirst().getIdLong();
        long everyoneRoleId = guild.getPublicRole().getIdLong();
        long botMemberId = guild.getSelfMember().getIdLong();

        Category category = guild.createCategory(getCategoryName(module, semester))
                .addRolePermissionOverride(everyoneRoleId, List.of(), denyPermissionsEveryone)
                .addRolePermissionOverride(roleId, allowPermissionsRole, List.of())
                .addMemberPermissionOverride(botMemberId, allowPermissionsRole, List.of())
                .complete();

        TextChannel general = guild.createTextChannel(GENERAL_CHAT_NAME, category)
                .addRolePermissionOverride(everyoneRoleId, List.of(), denyPermissionsEveryone)
                .addRolePermissionOverride(roleId, allowPermissionsRole, List.of())
                .addMemberPermissionOverride(botMemberId, allowPermissionsRole, List.of())
                .complete();
        TextChannel announcements = guild.createTextChannel("announcements", category)
                .addRolePermissionOverride(roleId, List.of(
                        Permission.VIEW_CHANNEL,
                        Permission.MESSAGE_EXT_EMOJI,
                        Permission.MESSAGE_ADD_REACTION,
                        Permission.MESSAGE_HISTORY
                ), List.of(
                        Permission.CREATE_PUBLIC_THREADS,
                        Permission.CREATE_PRIVATE_THREADS,
                        Permission.MESSAGE_SEND,
                        Permission.MESSAGE_SEND_IN_THREADS,
                        Permission.MESSAGE_SEND_POLLS
                ))
                .addMemberPermissionOverride(botMemberId, allowPermissionsRole, List.of())
                .addRolePermissionOverride(everyoneRoleId, List.of(), List.of(Permission.VIEW_CHANNEL))
                .complete();
        ForumChannel forumChannel = guild.createForumChannel("fragen", category)
                .addRolePermissionOverride(everyoneRoleId, List.of(), denyPermissionsEveryone)
                .addRolePermissionOverride(roleId, allowPermissionsRole, List.of())
                .addMemberPermissionOverride(botMemberId, allowPermissionsRole, List.of())
                .complete();
        TextChannel suggestions = guild.createTextChannel("tutoriums-gestaltung", category)
                .addRolePermissionOverride(everyoneRoleId, List.of(), denyPermissionsEveryone)
                .addRolePermissionOverride(roleId, allowPermissionsRole, List.of())
                .addMemberPermissionOverride(botMemberId, allowPermissionsRole, List.of())
                .complete();
        TextChannel offTopic = guild.createTextChannel("off-topic", category)
                .addRolePermissionOverride(everyoneRoleId, List.of(), denyPermissionsEveryone)
                .addRolePermissionOverride(roleId, allowPermissionsRole, List.of())
                .addMemberPermissionOverride(botMemberId, allowPermissionsRole, List.of())
                .complete();
        VoiceChannel voiceChannel = guild.createVoiceChannel("Auf eine Tasse Kaffee (VC)", category)
                .addRolePermissionOverride(everyoneRoleId, List.of(), denyPermissionsEveryone)
                .addRolePermissionOverride(roleId, allowPermissionsRole, List.of())
                .addMemberPermissionOverride(botMemberId, allowPermissionsRole, List.of())
                .complete();

        int index = 0;
        general.getManager().setPosition(index++).queue();
        announcements.getManager().setPosition(index++).queue();
        forumChannel.getManager().setPosition(index++).queue();
        suggestions.getManager().setPosition(index++).queue();
        offTopic.getManager().setPosition(index++).queue();
        voiceChannel.getManager().setPosition(index).queue();

        List<Category> tutorCategories = guild.getCategoriesByName("Tutoren", true);
        if (!tutorCategories.isEmpty()) {
            var tutorCategory = tutorCategories.getFirst();
            guild.modifyCategoryPositions().selectPosition(category).moveAbove(tutorCategory).queue();
        }
    }

    private void createMessage() {
        GeneralChat currentLastChat = findPreviousGeneralChat(semester);
        if (currentLastChat == null) return;
        GeneralChat beforeLastChat = findPreviousGeneralChat(currentLastChat.semester());
        if (beforeLastChat != null) {
            // delete old reference message
            Message oldMessage = findRedirectMessage(beforeLastChat.channel());
            if (oldMessage != null) oldMessage.delete().queue();
        }

        Message redirectMessage = findRedirectMessage(currentLastChat.channel());
        if (redirectMessage != null) redirectMessage.delete().queue();

        currentLastChat.channel().sendMessage("%s%n%s".formatted(
                MESSAGE_TITLE,
                MESSAGE_CONTENT.formatted(module.getSemiShortName())
        )).addComponents(
                constructSwitchMessageButton(module, semester)
        ).queue();
    }

    public static LayoutComponent constructSwitchMessageButton(Module module, Semester semester) {
        return ActionRow.of(Button.primary("%s%s-%s".formatted(BUTTON_SWITCH_PREFIX, module.getKey(), semester.generateValueKey()), "Neuem Semester beitreten"));
    }

    private GeneralChat findPreviousGeneralChat(Semester currentSemester) {
        currentSemester = currentSemester.getPreviousSemester();
        for (int i = 0; i < MAXIMUM_SEMESTERS_LOOKBACK; i++, currentSemester = currentSemester.getPreviousSemester()) {
            TextChannel general = findGeneralChat(currentSemester);
            if (general == null) continue;
            return new GeneralChat(general, currentSemester);
        }
        return null;
    }

    private Message findRedirectMessage(TextChannel generalChat) {
        for (Message message : generalChat.getHistory().retrievePast(35).complete()) {
            if (message.getAuthor().isBot() && message.getAuthor().getName().equals(DiscordJDA.getName())
                && message.getContentRaw().startsWith(MESSAGE_TITLE)) {
                return message;
            }
        }
        return null;
    }

    private TextChannel findGeneralChat(Semester currentSemester) {
        var categories = guild.getCategoriesByName(getCategoryName(module, currentSemester), false);
        if (categories.isEmpty()) return null;
        var channels = categories.getFirst().getTextChannels();
        for (TextChannel channel : channels) {
            if (channel.getName().equals(GENERAL_CHAT_NAME)) {
                return channel;
            }
        }
        return null;
    }

    public static boolean switchSemester(Guild guild, Member member, Module module, Semester semester) {
        List<Role> newRoles = new ArrayList<>();
        loadRole(guild, getSemesterRoleName(module, semester), newRoles);
        loadRole(guild, module.getSemiShortName(), newRoles);
        if (newRoles.isEmpty()) return false;
        guild.modifyMemberRoles(member, newRoles, List.of()).queue();
        return true;
    }

    private static void loadRole(Guild guild, String roleName, List<Role> list) {
        List<Role> roles = guild.getRolesByName(roleName, true);
        if (roles.isEmpty()) return;
        list.add(roles.getFirst());
    }

    public static String getSemesterRoleName(Module module, Semester semester) {
        return "%s %s".formatted(module.getShortName(), semester.getShortNumbersOnly());
    }

    public static String getCategoryName(Module module, Semester semester) {
        return "%s %s".formatted(module.getSemiShortName(), semester.getShortName());
    }

    private record GeneralChat(TextChannel channel, Semester semester) { }

}
