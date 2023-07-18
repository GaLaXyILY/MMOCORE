package net.Indyuce.mmocore.spawnpoint.def;

import io.lumine.mythic.lib.api.MMOLineConfig;
import net.Indyuce.mmocore.api.player.PlayerData;
import net.Indyuce.mmocore.spawnpoint.SpawnPoint;
import net.Indyuce.mmocore.spawnpoint.SpawnPointContext;
import org.bukkit.World;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public abstract class DefaultSpawnOption {

    protected final String worldExpression;

    public DefaultSpawnOption() {
        this.worldExpression = "true";
    }

    public DefaultSpawnOption(MMOLineConfig config) {
        this.worldExpression = config.getString("world-expression");
    }

    public boolean matches(World world) {
        String worldName = world.getName();
        Pattern pattern = Pattern.compile(this.worldExpression);
        Matcher matcher = pattern.matcher(worldName);
        return matcher.matches();
    }

    public abstract SpawnPointContext getSpawnPointContext(PlayerData playerData);

}
