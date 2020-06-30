package com.bgsoftware.superiorprison.plugin.util;

import com.oop.datamodule.SerializableObject;
import com.oop.datamodule.SerializedData;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.util.NumberConversions;

@Getter
@Setter
@Accessors(fluent = true, chain = true)
@AllArgsConstructor
public class SPLocation implements Cloneable, SerializableObject {

    private double x;
    private double y;
    private double z;
    private String worldName;
    private float pitch = 0;
    private float yaw = 0;

    protected SPLocation() {}

    public SPLocation(String worldName, double x, double y, double z) {
        this.worldName = worldName;
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public SPLocation(Location location) {
        this.x = location.getBlockX();
        this.z = location.getBlockZ();
        this.y = location.getBlockY();
        this.worldName = location.getWorld().getName();
        this.pitch = location.getPitch();
        this.yaw = location.getYaw();
    }

    public Location toBukkit() {
        World world = getWorld();
        if (world == null) return null;

        return new Location(world, x, y, z, yaw, pitch);
    }

    public SPLocation add(double x, double y, double z) {
        this.x = this.x + x;
        this.y = this.y + y;
        this.z = this.z + z;
        return this;
    }

    public World getWorld() {
        return Bukkit.getWorld(worldName);
    }

    @Override
    public String toString() {
        return "(world: " + worldName + ", x: " + x + ", y: " + y + ", z: " + z + ")";
    }

    @Override
    public SPLocation clone() {
        try {
            return (SPLocation) super.clone();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return null;
    }

    public int xBlock() {
        return NumberConversions.floor(x);
    }

    public int yBlock() {
        return NumberConversions.floor(y);
    }

    public int zBlock() {
        return NumberConversions.floor(z);
    }

    @Override
    public void serialize(SerializedData data) {
        data.write("world", worldName);
        data.write("x", x);
        data.write("y", y);
        data.write("z", z);
        data.write("yaw", yaw);
        data.write("pitch", pitch);
    }

    @Override
    public void deserialize(SerializedData data) {
        this.worldName = data.applyAs("world", String.class);
        this.x = data.applyAs("x", double.class);
        this.y = data.applyAs("y", double.class);
        this.z = data.applyAs("z", double.class);
        this.yaw = data.applyAs("yaw", float.class, () -> 0f);
        this.pitch = data.applyAs("pitch", float.class, () -> 0f);
    }
}