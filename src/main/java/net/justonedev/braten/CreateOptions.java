package net.justonedev.braten;

import net.dv8tion.jda.api.interactions.components.selections.SelectOption;

import java.util.List;

public class CreateOptions {
    public static final String OPTION_VALUE_CREATE_ROLE = "optionRoles";
    public static final String OPTION_VALUE_CREATE_CHANNELS = "optionChannels";
    public static final String OPTION_VALUE_SEND_MESSAGE = "optionMessage";

    public static final CreateOptions DEFAULTS = new CreateOptions(true, true, true);

    private final boolean createRole;
    private final boolean createChannels;
    private final boolean createMessage;

    public CreateOptions(boolean createRole, boolean createChannels, boolean createMessage) {
        this.createRole = createRole;
        this.createChannels = createChannels;
        this.createMessage = createMessage;
    }

    public boolean doCreateRole() {
        return createRole;
    }

    public boolean doCreateChannels() {
        return createChannels;
    }

    public boolean doCreateMessage() {
        return createMessage;
    }

    public static CreateOptions fromOptions(List<SelectOption> options) {
        return fromSelectedOptions(options.stream().filter(SelectOption::isDefault).toList());
    }

    public static CreateOptions fromSelectedOptions(List<SelectOption> options) {
        // Default values here are false, since this is only for true (selected) options
        boolean createRole = false;
        boolean createChannels = false;
        boolean createMessage = false;
        for (SelectOption option : options) {
            switch (option.getValue()) {
                case OPTION_VALUE_CREATE_ROLE -> createRole = true;
                case OPTION_VALUE_CREATE_CHANNELS -> createChannels = true;
                case OPTION_VALUE_SEND_MESSAGE -> createMessage = true;
            }
        }
        return new CreateOptions(createRole, createChannels, createMessage);
    }
}
