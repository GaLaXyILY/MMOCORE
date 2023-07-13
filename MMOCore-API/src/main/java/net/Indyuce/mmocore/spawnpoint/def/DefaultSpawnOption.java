package net.Indyuce.mmocore.spawnpoint.def;

import io.lumine.mythic.lib.api.MMOLineConfig;
import net.Indyuce.mmocore.api.player.PlayerData;
import org.bukkit.Location;
import org.bukkit.World;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public abstract class DefaultSpawnOption {

    protected final String worldExpression;

    public DefaultSpawnOption(MMOLineConfig config) {
        this.worldExpression = config.getString("world-expression");
    }

    public boolean matches(World world) {
        String worldName = world.getName();
        Pattern pattern = Pattern.compile(this.worldExpression);
        Matcher matcher = pattern.matcher(worldName);
        return matcher.matches();
    }

    public abstract Location getSpawnLocation(PlayerData playerData);

    public abstract void whenRespawn(PlayerData playerData);

}
