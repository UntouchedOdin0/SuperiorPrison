package com.bgsoftware.superiorprison.api.data.player;

import java.util.Set;

public interface BoosterData {

    boolean hasActiveBooster();

    Set<Double> getActiveBoosters();

}