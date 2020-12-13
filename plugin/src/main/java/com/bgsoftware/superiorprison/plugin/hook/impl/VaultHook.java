package com.bgsoftware.superiorprison.plugin.hook.impl;

import com.bgsoftware.superiorprison.plugin.hook.SHook;
import com.bgsoftware.superiorprison.plugin.object.player.SPrisoner;
import com.oop.orangeengine.main.task.OTask;
import com.oop.orangeengine.main.util.data.pair.OPair;
import lombok.Getter;
import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.permission.Permission;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.plugin.RegisteredServiceProvider;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

import static org.bukkit.Bukkit.getServer;

@Getter
public class VaultHook extends SHook {
    private static final BigDecimal MAX_DOUBLE = BigDecimal.valueOf(Double.MAX_VALUE);
    private Economy ecoProvider;
    private Permission permProvider;

    private final Map<UUID, BigDecimal> owed = new ConcurrentHashMap<>();
    private final Map<UUID, OPair<BigDecimal, BigDecimal>> cachedBalance = new ConcurrentHashMap<>();

    public VaultHook() {
        super(null);
        setRequired(true);

        RegisteredServiceProvider<Permission> permissionProvider = getServer().getServicesManager().getRegistration(net.milkbowl.vault.permission.Permission.class);
        if (permissionProvider != null && permissionProvider.getProvider() != null)
            this.permProvider = permissionProvider.getProvider();

        RegisteredServiceProvider<Economy> economyProvider = getServer().getServicesManager().getRegistration(net.milkbowl.vault.economy.Economy.class);
        if (economyProvider != null && economyProvider.getProvider() != null)
            this.ecoProvider = economyProvider.getProvider();

        disableIf(ecoProvider == null, "Failed to initialize Economy provider!");

        new OTask()
                .delay(TimeUnit.SECONDS, 1)
                .repeat(true)
                .runnable(this::handleDeposit)
                .execute();
    }

    public void withdrawPlayer(OfflinePlayer player, BigDecimal amount) {
        OPair<BigDecimal, BigDecimal> cache = cachedBalance.get(player.getUniqueId());
        if (cache != null) {
            cache.setFirst(cache.getFirst().subtract(amount));
            return;
        }

        getEcoProvider().withdrawPlayer(player, amount.doubleValue());
    }

    public BigDecimal getBalance(OfflinePlayer player) {
        OPair<BigDecimal, BigDecimal> cache = cachedBalance.get(player.getUniqueId());
        if (cache != null) return cache.getFirst();

        return BigDecimal.valueOf(getEcoProvider().getBalance(player));
    }

    public void submitWithdrawCache(OfflinePlayer player) {
        OPair<BigDecimal, BigDecimal> cache = cachedBalance.get(player.getUniqueId());
        if (cache != null) {
            BigDecimal subtract = cache.getSecond().subtract(cache.getFirst());
            getEcoProvider().withdrawPlayer(player, subtract.doubleValue());
            cachedBalance.remove(player.getUniqueId());
        }
    }

    public void enableCacheFor(Player player) {
        BigDecimal bigDecimal = BigDecimal.valueOf(getEcoProvider().getBalance(player));
        cachedBalance.put(player.getUniqueId(), new OPair<>(bigDecimal, bigDecimal));
    }

    public void removePermissions(SPrisoner prisoner, List<String> permissions) {
        Objects.requireNonNull(permProvider, "Failed to remove permission, missing permission provider!");
        permissions.forEach(perm -> permProvider.playerRemove(null, prisoner.getOfflinePlayer(), perm));
    }

    public void addPermissions(SPrisoner prisoner, List<String> permissions) {
        Objects.requireNonNull(permProvider, "Failed to add permission, missing permission provider!");
        permissions.forEach(perm -> permProvider.playerAdd(null, prisoner.getOfflinePlayer(), perm));
    }

    public void depositPlayer(SPrisoner prisoner, BigDecimal amount) {
        BigDecimal currentOwed = owed.getOrDefault(prisoner.getUUID(), BigDecimal.ZERO);
        owed.put(prisoner.getUUID(), currentOwed.add(amount));
    }

    public void handleDeposit() {
        owed.forEach((key, currentPrice) -> {
            OfflinePlayer prisoner = Bukkit.getOfflinePlayer(key);
            while (currentPrice.compareTo(BigDecimal.ZERO) > 0) {
                if (currentPrice.compareTo(MAX_DOUBLE) > 0) {
                    getEcoProvider().depositPlayer(prisoner, Double.MAX_VALUE);
                    currentPrice = currentPrice.subtract(MAX_DOUBLE);

                } else {
                    getEcoProvider().depositPlayer(prisoner, currentPrice.doubleValue());
                    break;
                }
            }
        });
        owed.clear();
    }

    @Override
    public String getPluginName() {
        return "Vault";
    }
}
