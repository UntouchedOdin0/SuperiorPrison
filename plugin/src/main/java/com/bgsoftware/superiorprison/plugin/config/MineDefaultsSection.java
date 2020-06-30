package com.bgsoftware.superiorprison.plugin.config;

import com.bgsoftware.superiorprison.api.data.mine.settings.ResetType;
import com.oop.orangeengine.item.custom.OItem;
import com.oop.orangeengine.main.util.data.pair.OPair;
import com.oop.orangeengine.material.OMaterial;
import com.oop.orangeengine.yaml.ConfigSection;
import lombok.Getter;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Getter
public class MineDefaultsSection {

    private final OItem icon;
    private int limit = -1;

    private final OPair<ResetType, String> resetting = new OPair<>(ResetType.PERCENTAGE, "50");
    private final List<OPair<Double, OMaterial>> materials;

    private final List<OPair<OMaterial, BigDecimal>> shopPrices;

    MineDefaultsSection(ConfigSection section) {
        this.icon = new OItem().load(section.getSection("icon").get());
        this.limit = section.getAs("limit");

        ConfigSection resettingSection = section.getSection("resetting").get();
        this.resetting.set(ResetType.valueOf(resettingSection.getAs("mode", String.class).toUpperCase()), resettingSection.getAs("value"));

        this.materials = ((List<String>) section.getAs("materials"))
                .stream()
                .map(string -> string.split(":"))
                .map(array -> new OPair<>(Double.parseDouble(array[1]), OMaterial.matchMaterial(array[0])))
                .collect(Collectors.toList());

        this.shopPrices = !section.isValuePresent("shop items") ? new ArrayList<>() : ((List<String>) section.getAs("shop items"))
                .stream()
                .map(string -> string.split(":"))
                .map(array -> new OPair<>(OMaterial.matchMaterial(array[0].toUpperCase()), new BigDecimal(array[1])))
                .collect(Collectors.toList());
    }
}