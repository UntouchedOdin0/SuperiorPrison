package com.bgsoftware.superiorprison.plugin.nms;

import com.oop.orangeengine.material.OMaterial;
import net.minecraft.server.v1_15_R1.BlockPosition;
import net.minecraft.server.v1_15_R1.PacketPlayOutMapChunk;
import net.minecraft.server.v1_15_R1.ServerConnection;
import net.minecraft.server.v1_15_R1.TileEntity;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.craftbukkit.v1_15_R1.CraftChunk;
import org.bukkit.craftbukkit.v1_15_R1.CraftWorld;
import org.bukkit.craftbukkit.v1_15_R1.entity.CraftPlayer;
import org.bukkit.craftbukkit.v1_15_R1.util.CraftMagicNumbers;
import org.bukkit.entity.Player;

import java.util.List;

public class NmsHandler_v1_15_R1 implements ISuperiorNms {
    @Override
    public void setBlock(Location location, OMaterial material) {

        int chunkX = location.getBlockX() >> 4;
        int chunkZ = location.getBlockZ() >> 4;
        if (!location.getWorld().isChunkLoaded(chunkX, chunkZ))
            return;

        net.minecraft.server.v1_15_R1.World world = ((CraftWorld) location.getWorld()).getHandle();
        BlockPosition pos = new BlockPosition(location.getBlockX(), location.getBlockY(), location.getBlockZ());
        net.minecraft.server.v1_15_R1.Chunk chunk = world.getChunkAt(chunkX, chunkZ);

        chunk.setType(pos, CraftMagicNumbers.getBlock(material.parseMaterial(), material.getData()), false);
    }

    @Override
    public void refreshChunks(World world, List<Chunk> chunkList) {
        for (Chunk chunk : chunkList) {
            net.minecraft.server.v1_15_R1.Chunk nmsChunk = ((CraftChunk) chunk).getHandle();
            for (Player player : world.getPlayers()) {
                ((CraftPlayer) player).getHandle().playerConnection.sendPacket(new PacketPlayOutMapChunk(nmsChunk, 65535));
            }
        }
    }
}
