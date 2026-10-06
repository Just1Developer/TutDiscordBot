package net.justonedev.braten;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.StringSelectInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.LayoutComponent;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.interactions.components.selections.SelectOption;
import net.dv8tion.jda.api.interactions.components.selections.StringSelectMenu;

import net.justonedev.braten.semester.Module;
import net.justonedev.braten.semester.Semester;
import net.justonedev.braten.semester.SemesterType;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SemesterSetup extends ListenerAdapter {

    private static final String MESSAGE_TITLE = "## Neues Semester erstellen";
    private static final String MODULE_SEMESTER_KEY_FORMAT = "%s-%s";
    private static final String OK_BUTTON_ID = "semester:create";
    private static final String CREATE_KEY_FORMAT = OK_BUTTON_ID + "%s-%s-%s";

    private static final int MAX_SEMESTERS = 3;
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
            guild.upsertCommand(COMMAND_NAME, "Initializes a new semester, with channels and updates")
                    .addCheck(() -> true).queue();
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

        List<LayoutComponent> currentLayout = new ArrayList<>(event.getMessage().getComponents());
        switch (event.getComponentId()) {
            case "setup:module":
                handleModuleSelected(event, currentLayout);
                break;
            case "setup:semester":
                handleSemesterSelected(event, currentLayout);
                break;
            case "setup:options":
                handleOptionsSelected(event, currentLayout);
                break;
            default:
                event.deferEdit().queue();
                return;
        }
        event.editComponents(currentLayout).queue();
    }

    @Override
    public void onSlashCommandInteraction(SlashCommandInteractionEvent event) {
        if (!event.getName().equals(COMMAND_NAME)) return;
        if (!verifyIfAuthorizedServerCommand(event)) return;
        // Has permission.

        event.reply(MESSAGE_TITLE)
                .setEphemeral(true)
                .addComponents(ActionRow.of(constructModuleSelect()))
                .queue();
    }

    @Override
    public void onButtonInteraction(ButtonInteractionEvent event) {
        if (!event.getMessage().getContentRaw().startsWith(MESSAGE_TITLE)) return;
        handleCreatePressed(event);
    }

    //endregion

    //region Setup Interaction Modify methods

    private void handleModuleSelected(StringSelectInteractionEvent event, List<LayoutComponent> currentLayout) {
        String selectedModuleKey = getFirstSelectedValueOrNull(event);
        Module module = Module.fromKey(selectedModuleKey);
        removeMiddleElementsFromListUntilSize(currentLayout, 1, 1);
        if (module == null) return;
        // perhaps add logic to keep selected options if they are valid
        currentLayout.set(0, ActionRow.of(constructModuleSelect(module)));
        setOrAdd(currentLayout, 1, ActionRow.of(constructSemesterSelect(module)));
    }

    private void handleSemesterSelected(StringSelectInteractionEvent event, List<LayoutComponent> currentLayout) {
        String selectedSemesterKey = getFirstSelectedValueOrNull(event);
        Semester semester = Semester.fromKey(selectedSemesterKey);
        if (semester == null) {
            removeMiddleElementsFromListUntilSize(currentLayout, 2, 2);
            return;
        }
        Module module = Module.fromKey(selectedSemesterKey);
        currentLayout.set(0, ActionRow.of(constructModuleSelect(module)));
        currentLayout.set(1, ActionRow.of(constructSemesterSelect(module, semester)));
        setOrAdd(currentLayout, 2, ActionRow.of(constructOptions(event.getGuild(), module, semester)));
        setOrAdd(currentLayout, 3, ActionRow.of(Button.success(OK_BUTTON_ID, "Erstellen")));
    }

    private void handleOptionsSelected(StringSelectInteractionEvent event, List<LayoutComponent> currentLayout) {
        Module module = Module.fromKey(getFirstDefaultTrueOptionValue(event.getMessage(), 0));
        Semester semester = Semester.fromKey(getFirstDefaultTrueOptionValue(event.getMessage(), 1));
        CreateOptions options = CreateOptions.fromSelectedOptions(event.getInteraction().getSelectedOptions());
        setOrAdd(currentLayout, 2, ActionRow.of(constructOptions(event.getGuild(), module, semester, options)));
    }

    private void handleCreatePressed(ButtonInteractionEvent event) {
        if (event.getButton().getId() == null || !event.getButton().getId().startsWith(OK_BUTTON_ID)) return;
        // Assume validity:
        Module module = Module.fromKey(getFirstDefaultTrueOptionValue(event.getMessage(), 0));
        Semester semester = Semester.fromKey(getFirstDefaultTrueOptionValue(event.getMessage(), 1));
        CreateOptions options = CreateOptions.fromOptions(((StringSelectMenu) event.getMessage().getComponents().get(2).getComponents().getFirst()).getOptions());
        System.out.println("OK Button pressed: ");
        System.out.println(semester);
        System.out.println(module);
        System.out.printf("Options: Role: %s, Channels: %s, Message: %s%n", options.doCreateRole(), options.doCreateChannels(), options.doCreateMessage());
    }

    private String getFirstSelectedValueOrNull(StringSelectInteractionEvent event) {
        var list = event.getInteraction().getSelectedOptions();
        if (list.isEmpty() || list.getFirst() == null) return null;
        return list.getFirst().getValue();
    }

    private String getFirstDefaultTrueOptionValue(Message message, int componentId) {
        StringSelectMenu menu = (StringSelectMenu) message.getComponents().get(componentId).getComponents().getFirst();
        for (SelectOption option : menu.getOptions()) {
            if (option.isDefault()) return option.getValue();
        }
        return null;
    }

    private <T> void setOrAdd(List<T> list, int index, T element) {
        if (list.size() > index) {
            list.set(index, element);
        } else {
            // make it last element
            list.add(element);
        }
    }

    private void removeMiddleElementsFromListUntilSize(List<?> list, int size, int removalIndex) {
        while (list.size() > size) {
            list.remove(1);
        }
    }

    //endregion

    //region Component Construction

    private StringSelectMenu constructModuleSelect() {
        return constructModuleSelect(null);
    }
    private StringSelectMenu constructModuleSelect(Module selectedModule) {
        var menuBuilder = StringSelectMenu.create("setup:module")
                .setPlaceholder("Modul auswählen...")
                .setRequiredRange(1, 1);
        for (Module module : Module.values()) {
            menuBuilder.addOptions(
                    SelectOption.of(module.getTitle(), module.getKey())
                            .withDescription("Erstellt die Kategorie und Rollen benannt für %s".formatted(module.getShortName()))
                            .withDefault(module.equals(selectedModule))
            );
        }
        return menuBuilder.build();
    }

    private StringSelectMenu constructSemesterSelect(Module module) {
        return constructSemesterSelect(module, null);
    }
    private StringSelectMenu constructSemesterSelect(Module module, Semester selectedSemester) {
        var menuBuilder = StringSelectMenu.create("setup:semester")
                .setPlaceholder("Semester auswählen...")
                .setRequiredRange(1, 1);

        if (module != null) {
            menuBuilder.addOptions(
                    listPossibleSemesters(MAX_SEMESTERS)
                            .stream()
                            .filter(module::isInSemester)
                            .map(semester ->
                                    SelectOption.of(semester.toString(),
                                                    MODULE_SEMESTER_KEY_FORMAT.formatted(module.getKey(), semester.generateValueKey()))
                                            .withDefault(semester.equals(selectedSemester)))
                            .toList()
            );
        }

        if (menuBuilder.getOptions().isEmpty()) {
            menuBuilder.addOptions(SelectOption.of("Keine Semester gefunden :(", "failure").withDefault(true));
            menuBuilder.setDisabled(true);
        }

        return menuBuilder.build();
    }

    private StringSelectMenu constructOptions(Guild guild, Module module, Semester semester) {
        return constructOptions(guild, module, semester, CreateOptions.DEFAULTS);
    }
    private StringSelectMenu constructOptions(Guild guild, Module module, Semester semester, CreateOptions options) {
        var optionsSelectBuilder = StringSelectMenu.create("setup:options")
                .setPlaceholder("Optionen auswählen...")
                .setRequiredRange(0, 3);

        if (!doesRoleExist(guild, module, semester)) {
            optionsSelectBuilder.addOptions(
                    SelectOption.of("Rolle erstellen", CreateOptions.OPTION_VALUE_CREATE_ROLE)
                            .withDescription("Die Semester-Rolle wird erstellt")
                            .withDefault(options.doCreateRole())
            );
        }
        if (!doesChannelsExist(guild, module, semester)) {
            optionsSelectBuilder.addOptions(
                    SelectOption.of("Kanäle erstellen", CreateOptions.OPTION_VALUE_CREATE_CHANNELS)
                            .withDescription("Text- und Sprachkanäle werden für das Semester erstellt")
                            .withDefault(options.doCreateChannels())
            );
        }
        optionsSelectBuilder.addOptions(
                        SelectOption.of("Switch-Nachricht", CreateOptions.OPTION_VALUE_SEND_MESSAGE)
                        .withDescription("Eine Wechseln-Nachricht wird an #allgemein des vorherigen Semesters geschickt")
                        .withDefault(options.doCreateMessage())
                );
        return optionsSelectBuilder.build();
    }

    //endregion

    //region Semester Utils

    private List<Semester> listPossibleSemesters(int maxSemesters) {
        // List two semesters. exclude semesters that are in the past and semesters that already exist
        LocalDate currentSemester = LocalDateTime.now().toLocalDate();
        List<Semester> semesters = new ArrayList<>();
        for (int i = 0; i < maxSemesters; i++) {
            Semester semester = getSemester(currentSemester.plusMonths(6L * i));
            semesters.add(semester);
        }
        return semesters;
    }

    private Semester getSemester(LocalDate date) {
        if (date.getMonth().getValue() >= SUMMER_SEMESTER_BEGIN_INCLUSIVE &&
                date.getMonth().getValue() <= SUMMER_SEMESTER_END_INCLUSIVE) {
            return new Semester(date.getYear(), SemesterType.SUMMER);
        }
        int firstYear = date.getYear();
        if (date.getMonth().getValue() < SUMMER_SEMESTER_BEGIN_INCLUSIVE) firstYear--;
        return new Semester(firstYear, SemesterType.WINTER);
    }

    //endregion

    //region Component Utils

    private boolean doesRoleExist(Guild guild, Module module, Semester semester) {
        return !guild.getRolesByName(SemesterCreator.getSemesterRoleName(module, semester), true).isEmpty();
    }

    private boolean doesChannelsExist(Guild guild, Module module, Semester semester) {
        return !guild.getCategoriesByName(SemesterCreator.getCategoryName(module, semester), true).isEmpty();
    }

    //endregion
}
