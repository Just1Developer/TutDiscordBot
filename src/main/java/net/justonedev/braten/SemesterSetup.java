package net.justonedev.braten;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.StringSelectInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.LayoutComponent;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.interactions.components.selections.StringSelectMenu;

import net.justonedev.braten.semester.Module;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SemesterSetup extends ListenerAdapter {

    private static final String MESSAGE_TITLE = "## Neues Semester erstellen";

    private static final int SUMMER_SEMESTER_BEGIN_INCLUSIVE = 4;
    private static final int SUMMER_SEMESTER_END_INCLUSIVE = 9;

    private static final String COMMAND_NAME = "new-semester";
    private static final String REQUIRED_ROLE_NAME_CONTAINS = "Tutor";

    private final Map<Guild, Role> requiredRoles;

    private final JDA jda;

    // region Constructor and Setup

    public SemesterSetup(JDA jda) {
        this.jda = jda;
        jda.getGuilds().forEach(guild -> {
            // TODO change to isProduction
            if (!DiscordJDA.isTestServer(guild)) return;
            var command2 = guild.upsertCommand(COMMAND_NAME, "Initializes a new semester, with channels and updates")
                    .addCheck(() -> true).complete();
        });
        requiredRoles = new HashMap<>();
        loadRoles();
    }

    private void loadRoles() {
        jda.getGuilds().forEach(guild -> {
            if (!DiscordJDA.isListedServer(guild)) return;
            for (Role role : guild.getRolesByName(REQUIRED_ROLE_NAME_CONTAINS, true)) {
                requiredRoles.put(guild, role);
                break;
            }
        });
    }

    private boolean verifyIfAuthorizedServerCommand(SlashCommandInteractionEvent event) {
        if (event.getMember() == null) return false;
        if (!DiscordJDA.isListedServer(event.getGuild())) return false;
        Role requiredRole = requiredRoles.get(event.getGuild());
        return requiredRole == null || event.getMember().getRoles().contains(requiredRole) || event.getMember().isOwner();
    }

    //endregion

    //region Events

    @Override
    public void onStringSelectInteraction(StringSelectInteractionEvent event) {

        if (event.getInteraction().getSelectedOptions().isEmpty()) return;
        if (!event.getMessage().getContentRaw().startsWith(MESSAGE_TITLE)) return;

        switch (event.getInteraction().getId()) {
            case "setup:module":
                break;
            case "setup:semester":
                break;
            case "setup:options":
                break;
            default:
                event.deferEdit().queue();
                return;
        }

        List<LayoutComponent> currentLayout = new ArrayList<>(event.getMessage().getComponents());

        if (event.getInteraction().getSelectedOptions().getFirst().getValue().contains("summer")) {
            StringSelectMenu optionsSelect = StringSelectMenu.create("setup:options")
                    .setPlaceholder("Optionen auswählen...")
                    .addOption("Rollen überspringen", "noRoles", "Die Semester-Rolle wird nicht erstellt")
                    .addOption("Kanäle überspringen", "noChannels", "Text- und Sprachkanäle werden nicht erstellt")
                    .addOption("Nachricht an voriges Semester überspringen", "noMessage", "Es wird keine Switch-Nachricht ins alte Semester geschickt")
                    .setRequiredRange(0, 3)
                    .build();

            if (currentLayout.size() < 4) {
                currentLayout.add(2, ActionRow.of(optionsSelect));
            } else {
                currentLayout.set(2, ActionRow.of(optionsSelect));
            }
            event.editComponents(currentLayout).queue();
        } else {
            if (currentLayout.size() >= 3) {
                currentLayout.remove(1);
                event.editComponents(currentLayout).queue();
            } else {
                event.deferEdit().queue();
            }
        }
    }

    @Override
    public void onSlashCommandInteraction(SlashCommandInteractionEvent event) {
        if (!event.getName().equals(COMMAND_NAME)) return;
        if (!verifyIfAuthorizedServerCommand(event)) return;
        // Has permission.

        var semesterSelectBuild = StringSelectMenu.create("setup:semester")
                .setPlaceholder("Semester auswählen...");
        listPossibleSemesters().forEach(semester -> {
            semesterSelectBuild.addOption(semester.label, semester.value);
        });
        StringSelectMenu semesterSelect = semesterSelectBuild
                .setRequiredRange(1, 1)
                .build();

        StringSelectMenu optionsSelect = StringSelectMenu.create("setup:options")
                .setPlaceholder("Optionen auswählen...")
                .addOption("Rollen überspringen", "noRoles", "Die Semester-Rolle wird nicht erstellt")
                .addOption("Kanäle überspringen", "noChannels", "Text- und Sprachkanäle werden nicht erstellt")
                .addOption("Nachricht an voriges Semester überspringen", "noMessage", "Es wird keine Switch-Nachricht ins alte Semester geschickt")
                .setRequiredRange(0, 3)
                .build();

        Button createButton = Button.success("setup:create", "Erstellen");

        event.reply(MESSAGE_TITLE)
                .setEphemeral(true)
                .addComponents(
                        ActionRow.of(moduleSelect),
                        ActionRow.of(semesterSelect),
                        //ActionRow.of(optionsSelect),
                        ActionRow.of(createButton)
                )
                .queue();
    }

    @Override
    public void onButtonInteraction(ButtonInteractionEvent event) {
        if (!event.getMessage().getContentRaw().startsWith(MESSAGE_TITLE)) return;
    }

    //endregion

    //region Setup Interaction Modify methods

    private void handleModuleSelected(StringSelectInteractionEvent event) {

    }

    private void handleSemesterSelected(StringSelectInteractionEvent event) {

    }

    private void handleOptionsSelected(StringSelectInteractionEvent event) {

    }

    private void handleCreatePressed(ButtonInteractionEvent event) {

    }

    //endregion

    //region Component Construction

    private StringSelectMenu constructModuleSelect() {
        var menuBuilder = StringSelectMenu.create("setup:module")
                .setPlaceholder("Modul auswählen...")
                .setRequiredRange(1, 1);
        for (Module module : Module.values()) {

        }
        return menuBuilder
                .addOption("Programmieren", "proggen", "Erstellt die Kategorie und Rollen benannt für Proggen")
                .addOption("Algorithmen", "algo", "Erstellt die Kategorie und Rollen benannt für Algo")
                .addOption("Grundbegriffe der theoretischen Informatik", "gti", "Erstellt die Kategorie und Rollen benannt für GTI")
                .addOption("Theoretische Grundlagen der Informatik", "tgi", "Erstellt die Kategorie und Rollen benannt für TGI")
                .build();
    }

    private StringSelectMenu constructSemesterSelect(String module) {
        if (module == null || module.isBlank()) return null;



        return null;
    }

    private StringSelectMenu constructOptions(Guild guild, String module, String semester) {

        return null;
    }

    //endregion

    //region Semester Utils

    private List<SelectedSemester> listPossibleSemesters() {
        // List two semesters. exclude semesters that are in the past and semesters that already exist
        LocalDate currentSemester = LocalDateTime.now().toLocalDate();
        return List.of(
                getSemester(currentSemester),
                getSemester(currentSemester.plusMonths(6))
        );
    }

    private SelectedSemester getSemester(LocalDate date) {
        if (date.getMonth().getValue() >= SUMMER_SEMESTER_BEGIN_INCLUSIVE &&
                date.getMonth().getValue() <= SUMMER_SEMESTER_END_INCLUSIVE) {
            return new SelectedSemester("Sommersemester %04d".formatted(date.getYear()), "summer%04d".formatted(date.getYear()));
        }
        int firstYear = date.getYear();
        if (date.getMonth().getValue() < SUMMER_SEMESTER_BEGIN_INCLUSIVE) firstYear--;
        int secondYear = (date.getYear() + 1);
        int secondYearTrimmed = secondYear % 100;
        return new SelectedSemester("Wintersemester %04d/%2d".formatted(firstYear, secondYearTrimmed), "winter%04d%4d".formatted(firstYear, secondYear));
    }

    private record SelectedSemester(String label, String value) { }

    //endregion

    //region Component Utils

    //endregion
}
