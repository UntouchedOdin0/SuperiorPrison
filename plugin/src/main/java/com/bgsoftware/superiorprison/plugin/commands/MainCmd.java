package com.bgsoftware.superiorprison.plugin.commands;

import com.oop.orangeengine.command.CommandController;
import com.oop.orangeengine.command.OCommand;

public class MainCmd extends OCommand {
    public MainCmd() {
        label("superiorprison");
        alias("sp", "prison");
        permission("prison.cmds.admin.main");
    }

    public void afterRegister(CommandController controller) {
        for (OCommand value : controller.getRegisteredCommands().values()) {
            subCommand(value.clone());
        }
    }
}
