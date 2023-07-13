package net.Indyuce.mmocore.spawnpoint.def;

import io.lumine.mythic.lib.api.MMOLineConfig;
import net.Indyuce.mmocore.api.player.PlayerData;
import org.bukkit.Location;

public class LastSpawnOption extends DefaultSpawnOption
{

    public LastSpawnOption(MMOLineConfig config) {
        super(config);
    }

    //TODO
    @Override
    public Location getSpawnLocation(PlayerData playerData) {
        return null;
    }

    @Override
    public void whenRespawn(PlayerData playerData) {

    }
}
