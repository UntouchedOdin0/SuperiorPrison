package com.bgsoftware.superiorprison.plugin.object.mine;

import com.bgsoftware.superiorprison.api.data.mine.IMineGenerator;
import com.bgsoftware.superiorprison.api.data.mine.ISuperiorMine;
import com.bgsoftware.superiorprison.api.util.SPLocation;
import com.bgsoftware.superiorprison.plugin.util.Cuboid;
import com.oop.orangeengine.eventssubscription.SubscriptionFactory;
import com.oop.orangeengine.eventssubscription.SubscriptionProperties;
import com.oop.orangeengine.main.task.OTask;
import com.oop.orangeengine.main.util.OptionalConsumer;
import com.oop.orangeengine.main.util.pair.OPair;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.event.world.WorldLoadEvent;

import java.io.Serializable;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

@Setter
@Getter
public class MineGenerator implements IMineGenerator, Serializable {

    private ISuperiorMine mine;
    private List<OPair<Double, Material>> generatorMaterials = new ArrayList<>();
    private transient Instant lastReset;
    private transient Instant nextReset;

    // << CACHING >>
    private transient Block[] cachedMineArea = new Block[]{};
    private transient boolean caching = false;
    private transient boolean worldLoadWait = false;

    public MineGenerator() {

    }

    @Override
    public Instant getLastReset() {
        return lastReset;
    }

    @Override
    public Instant getNextReset() {
        return nextReset;
    }

    public void generate() {
    }

    public void initCache() {
        if(isCaching() || isWorldLoadWait())
            return;

        if(mine.getMinPoint().getWorld() == null) {
            worldLoadWait = true;
            SubscriptionFactory.getInstance().subscribeTo(WorldLoadEvent.class, event -> {

                initCache();
                worldLoadWait = false;

            }, new SubscriptionProperties<WorldLoadEvent>().timeOut(TimeUnit.SECONDS, 3).filter(event -> event.getWorld().getName().equals(mine.getMinPoint().getWorldName())));

            return;
        }

        Location pos1 = mine.getMinPoint().toBukkit();
        Location pos2 = mine.getHighPoint().toBukkit();

        Cuboid cuboid = new Cuboid(pos1, pos2);
        caching = true;
        cuboid.getFutureArray().whenComplete((locations, throwable) -> {
            if (throwable != null)
                throw new IllegalStateException(throwable);

            // Ensure that we're going in sync thread
            new OTask()
                    .runnable(() -> {
                        Set<OPair<Integer, Integer>> checkedChunks = new HashSet<>();

                        // Convert SPLocations to Blocks
                        Block[] newBlocks = new Block[locations.length];
                        int currentBlock = 0;

                        for (SPLocation location : locations) {
                            Location bukkitLocation = location.toBukkit();
                            int chunkX = bukkitLocation.getBlockX() >> 4;
                            int chunkZ = bukkitLocation.getBlockZ() >> 4;

                            // << CHUNK CHECK >>
                            OptionalConsumer.of(checkedChunks.stream()
                                    .filter(pair -> pair.getKey() == chunkX && pair.getSecond() == chunkZ)
                                    .findFirst())
                                    .ifNotPresent(() -> {

                                        // Ensure that the chunk is loaded
                                        if (!bukkitLocation.getWorld().isChunkLoaded(chunkX, chunkZ))
                                            bukkitLocation.getWorld().loadChunk(chunkX, chunkZ);

                                        checkedChunks.add(new OPair<>(chunkX, chunkZ));
                                    });

                            newBlocks[currentBlock] = location.toBukkit().getBlock();
                            currentBlock++;
                        }

                        this.cachedMineArea = newBlocks;
                        caching = false;
                    }).execute();
        });
    }

}