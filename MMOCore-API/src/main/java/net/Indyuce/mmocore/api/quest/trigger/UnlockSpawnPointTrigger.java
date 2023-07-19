package net.Indyuce.mmocore.api.quest.trigger;

import io.lumine.mythic.lib.api.MMOLineConfig;
import net.Indyuce.mmocore.MMOCore;
import net.Indyuce.mmocore.api.player.PlayerData;
import net.Indyuce.mmocore.skill.binding.SkillSlot;
import net.Indyuce.mmocore.api.quest.trigger.api.Removable;
import net.Indyuce.mmocore.spawnpoint.SpawnPoint;
import org.apache.commons.lang.Validate;


public class UnlockSpawnPointTrigger extends Trigger implements Removable {
    private final SpawnPoint spawnPoint;

    public UnlockSpawnPointTrigger(MMOLineConfig config) {
        super(config);
        config.validateKeys("spawn-point");
        Validate.isTrue(MMOCore.plugin.spawnPointManager.isSpawnPoint(config.getString("spawn-point")), config.getString("spawn-point") + " is not a valid spawn point");
        spawnPoint = MMOCore.plugin.spawnPointManager.getSpawnPoint(config.getString("spawn-point"));
    }

    @Override
    public void apply(PlayerData player) {
        if (!player.hasUnlocked(spawnPoint))
            player.unlock(spawnPoint);
    }

    @Override
    public void remove(PlayerData player) {
        if (player.hasUnlocked(spawnPoint))
            player.lock(spawnPoint);
    }
}
