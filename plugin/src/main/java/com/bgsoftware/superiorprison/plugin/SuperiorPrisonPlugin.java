package com.bgsoftware.superiorprison.plugin;

import com.bgsoftware.superiorprison.api.SuperiorPrison;
import com.bgsoftware.superiorprison.api.SuperiorPrisonAPI;
import com.bgsoftware.superiorprison.plugin.commands.CommandsRegister;
import com.bgsoftware.superiorprison.plugin.controller.DataController;
import com.bgsoftware.superiorprison.plugin.controller.TaskController;
import com.bgsoftware.superiorprison.plugin.listeners.ProtectionListener;
import com.bgsoftware.superiorprison.plugin.nms.ISuperiorNms;
import com.oop.orangeengine.command.CommandController;
import com.oop.orangeengine.database.ODatabase;
import com.oop.orangeengine.database.types.SqlLiteDatabase;
import com.oop.orangeengine.main.plugin.EnginePlugin;
import org.bukkit.Bukkit;

public class SuperiorPrisonPlugin extends EnginePlugin implements SuperiorPrison {

    public static boolean debug = true;
    private static SuperiorPrisonPlugin instance;
    private TaskController taskController;
    private DataController dataController;
    private ODatabase database;
    private ISuperiorNms nms;

    public SuperiorPrisonPlugin() {
        instance = this;
    }

    public static SuperiorPrisonPlugin getInstance() {
        return instance;
    }

    @Override
    public void enable() {

        // Setup NMS
        if (!setupNms()) {
            Bukkit.getPluginManager().disablePlugin(this);
            return;
        }

        // Setup API
        new SuperiorPrisonAPI(this);

        // Make sure plugin data folder exists
        if (!getDataFolder().exists())
            getDataFolder().mkdirs();

        // Setup Database
        ODatabase database = new SqlLiteDatabase(getDataFolder(), "data");
        this.dataController = new DataController(database);

        // Initialize controllers
        this.taskController = new TaskController();
        new ProtectionListener(this);

        CommandController commandController = new CommandController(this);
        CommandsRegister.register(commandController);
    }

    @Override
    public void disable() {
        instance = null;
    }

    @Override
    public DataController getMineController() {
        return dataController;
    }

    @Override
    public DataController getPrisonerController() {
        return dataController;
    }

    public boolean setupNms() {
        String version = getServer().getClass().getPackage().getName().split("\\.")[3];
        try {
            Object o = Class.forName("com.bgsoftware.superiorprison.plugin.nms.NmsHandler_" + version).newInstance();
            this.nms = (ISuperiorNms) o;
            getOLogger().print("Server version: " + version.replace("_", " .") + ". Fully compatible!");
            return true;
        } catch (ClassNotFoundException e) {
            getOLogger().printError("Unsupported version " + version + ". Failed to find NmsHandler, contact author!");
        } catch (InstantiationException | IllegalAccessException e) {
            e.printStackTrace();
        }
        return false;
    }

    public ISuperiorNms getNmsHandler() {
        return nms;
    }

}