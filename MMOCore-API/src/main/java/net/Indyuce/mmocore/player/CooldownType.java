package net.Indyuce.mmocore.player;

import io.lumine.mythic.lib.player.cooldown.CooldownProvider;
import net.Indyuce.mmocore.MMOCore;
import org.bukkit.NamespacedKey;
import org.jetbrains.annotations.NotNull;

import javax.inject.Provider;

/**
 * Used by MMOCore when it has to store the last time a player
 * did some action. This also features a timeout function which
 * can be used for cooldowns.
 */
public enum CooldownType implements CooldownProvider {
    USE_WAYPOINT(() -> MMOCore.plugin.configManager.waypointUseCooldown),

    FRIEND_REQUEST(() -> MMOCore.plugin.configManager.friendRequestTimeout),

    ACTION_BAR_MESSAGE(() -> MMOCore.plugin.actionBarManager.getTimeOut()),

    LOOT_CHEST_SPAWN(() -> MMOCore.plugin.configManager.lootChestPlayerCooldown),

    CAST_SKILL(() -> MMOCore.plugin.configManager.globalSkillCooldown),

    ;

    private final Provider<Long> timeout;
    private final NamespacedKey nsk;

    CooldownType(@NotNull Provider<Long> timeout) {
        this.timeout = timeout;
        this.nsk = new NamespacedKey(MMOCore.plugin, name());
    }

    @Override
    public NamespacedKey getCooldownKey() {
        return nsk;
    }

    @Override
    public long getCooldown() {
        return timeout.get();
    }
}
