package net.Indyuce.mmocore.spawnpoint.def;

import io.lumine.mythic.lib.api.MMOLineConfig;
import net.Indyuce.mmocore.api.player.PlayerData;
import net.Indyuce.mmocore.spawnpoint.SpawnPointContext;

import java.util.Optional;

public class SpawnPointOption extends DefaultSpawnOption {
    private final SpawnPointContext spawnPointContext;

    public SpawnPointOption(MMOLineConfig config) {
        super(config);
        spawnPointContext = new SpawnPointContext(config.getString("id"), config.contains("server") ?
                Optional.of(config.getString("server")) : Optional.empty());
    }

    @Override
    public SpawnPointContext getSpawnPointContext(PlayerData playerData) {
        return spawnPointContext;
    }
}
