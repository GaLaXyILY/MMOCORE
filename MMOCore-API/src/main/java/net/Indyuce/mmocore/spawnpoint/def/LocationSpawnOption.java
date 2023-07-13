package net.Indyuce.mmocore.spawnpoint.def;

import io.lumine.mythic.lib.api.MMOLineConfig;
import io.lumine.mythic.lib.util.configobject.ConfigObject;
import net.Indyuce.mmocore.api.player.PlayerData;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;

import javax.swing.text.html.Option;
import java.util.Optional;

public class LocationSpawnOption extends DefaultSpawnOption {
    private final double x, y, z;
    private final Optional<World> world;

    public LocationSpawnOption(MMOLineConfig config) {
        super(config);
        this.x = config.getDouble("spawn-x");
        this.y = config.getDouble("spawn-y");
        this.z = config.getDouble("spawn-z");
        this.world = Optional.ofNullable(Bukkit.getWorld(config.getString("spawn-world")));
    }

    @Override
    public Location getSpawnLocation(PlayerData playerData) {
        return new Location(world.orElseGet(() -> playerData.getPlayer().getWorld()), x, y, z);
    }

    @Override
    public void whenRespawn(PlayerData playerData) {

    }
}
