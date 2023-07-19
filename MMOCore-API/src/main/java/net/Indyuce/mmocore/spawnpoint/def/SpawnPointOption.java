package net.Indyuce.mmocore.spawnpoint.def;

import io.lumine.mythic.lib.api.MMOLineConfig;
import net.Indyuce.mmocore.MMOCore;
import net.Indyuce.mmocore.api.player.PlayerData;
import net.Indyuce.mmocore.spawnpoint.SpawnPoint;
import org.apache.commons.lang.Validate;

import java.util.Optional;

public class SpawnPointOption extends DefaultSpawnOption {
    private final SpawnPoint spawnPoint;

    public SpawnPointOption(MMOLineConfig config) {
        super(config);
        Validate.isTrue(MMOCore.plugin.spawnPointManager.isSpawnPoint(config.getString("id")));
        spawnPoint = MMOCore.plugin.spawnPointManager.getSpawnPoint(config.getString("id"));
    }

    @Override
    public SpawnPoint getSpawnPoint(PlayerData playerData) {
        return spawnPoint;
    }
}
