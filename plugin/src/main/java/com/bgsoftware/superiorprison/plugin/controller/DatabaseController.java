package com.bgsoftware.superiorprison.plugin.controller;

import com.bgsoftware.superiorprison.plugin.config.MainConfig;
import com.bgsoftware.superiorprison.plugin.data.SMineHolder;
import com.bgsoftware.superiorprison.plugin.data.SPrisonerHolder;
import com.bgsoftware.superiorprison.plugin.data.SStatisticHolder;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import com.oop.datamodule.DataHelper;
import com.oop.datamodule.StorageHolder;
import com.oop.datamodule.database.DatabaseWrapper;
import com.oop.orangeengine.main.task.StaticTask;
import com.oop.orangeengine.nbt.NBTContainer;
import com.oop.orangeengine.nbt.NBTItem;
import lombok.Getter;
import lombok.NonNull;
import org.bukkit.inventory.ItemStack;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.oop.orangeengine.main.Engine.getEngine;

@Getter
public class DatabaseController extends StorageHolder {

    private final String UNICODE_REGEX = "\\\\u([0-9a-f]{4})";
    private final Pattern UNICODE_PATTERN = Pattern.compile("\\\\u([0-9a-f]{4})");

    private final SPrisonerHolder prisonerHolder;
    private final SMineHolder mineHolder;
    private final SStatisticHolder statisticHolder;

    private final DatabaseWrapper database;

    public DatabaseController(MainConfig config) {
        database = config.getDatabase().getDatabase();

        // Runners
        DataHelper.RUN_ASYNC = runnable -> StaticTask.getInstance().async(runnable);
        DataHelper.RUN_SYNC = runnable -> StaticTask.getInstance().sync(runnable);

        // ItemStack handler
        DataHelper.ITEMSTACK_TO_ELEMENT = this::serialize;
        DataHelper.ELEMENT_TO_ITEMSTACK = this::deserialize;

        this.prisonerHolder = new SPrisonerHolder(this);
        this.mineHolder = new SMineHolder(this);
        this.statisticHolder = new SStatisticHolder(this);
        registerStorage(prisonerHolder);
        registerStorage(mineHolder);
        registerStorage(statisticHolder);

        AtomicInteger integer = new AtomicInteger();
        load(false, () -> {
            integer.incrementAndGet();
            if (integer.get() == getStorages().size()) {
                getEngine().getLogger().print("Loaded {} mines", getMineHolder().getMines().size());
                getEngine().getLogger().print("Loaded {} prisoners", getPrisonerHolder().getPrisonerMap().size());
            }
        });
    }

    public ItemStack deserialize(JsonElement jsonElement) throws JsonParseException {
        return NBTItem.convertNBTtoItem(new NBTContainer(utf8(jsonElement.getAsString())));
    }

    public JsonElement serialize(@NonNull ItemStack itemStack) {
        return new JsonPrimitive(NBTItem.convertItemtoNBT(itemStack).asNBTString());
    }

    public String utf8(String text) {
        Matcher matcher = UNICODE_PATTERN.matcher(text);
        StringBuffer decodedMessage = new StringBuffer();

        while (matcher.find()) {
            matcher.appendReplacement(decodedMessage, String.valueOf((char) Integer.parseInt(matcher.group(1), 16)));
        }

        matcher.appendTail(decodedMessage);
        return decodedMessage.toString();
    }
}