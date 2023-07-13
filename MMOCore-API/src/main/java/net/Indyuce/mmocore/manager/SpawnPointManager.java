package net.Indyuce.mmocore.manager;

import io.lumine.mythic.lib.UtilityMethods;
import io.lumine.mythic.lib.api.MMOLineConfig;
import io.lumine.mythic.lib.util.ConfigFile;
import io.lumine.mythic.lib.util.configobject.ConfigSectionObject;
import net.Indyuce.mmocore.MMOCore;
import net.Indyuce.mmocore.spawnpoint.SpawnPoint;
import net.Indyuce.mmocore.spawnpoint.def.DefaultSpawnOption;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SpawnPointManager implements MMOCoreManager {
    /**
     * Ordered by world for faster access time.
     */
    private final Map<World, List<SpawnPoint>> spawnPoints = new HashMap<>();

    private Location globalSpawn;

    private List<DefaultSpawnOption> defaultSpawnOptions = new ArrayList<>();


    public List<SpawnPoint> getSpawnPoints(World world) {
        return spawnPoints.getOrDefault(world, new ArrayList<>());
    }

    @Override
    public void initialize(boolean clearBefore) {
        FileConfiguration config = new ConfigFile(MMOCore.plugin, "", "spawn-points").getConfig();
        if (config.isConfigurationSection("spawn-points"))
            for (String key : config.getConfigurationSection("spawn-points").getKeys(false)) {
                SpawnPoint spawnPoint = new SpawnPoint(config.getConfigurationSection("spawn-points." + key));
                spawnPoints.putIfAbsent(spawnPoint.getLocation().getWorld(), new ArrayList<>());
                spawnPoints.get(spawnPoint.getLocation().getWorld()).add(spawnPoint);
            }
        globalSpawn = UtilityMethods.readLocation(new ConfigSectionObject(config.getConfigurationSection("global-spawn")));
        for (String defaultSpawnOption : config.getStringList("default-spawn")) {
            defaultSpawnOptions.add(MMOCore.plugin.loadManager.loadDefaultSpawnOption(new MMOLineConfig(defaultSpawnOption)));
        }
    }

    public Location getGlobalSpawn() {
        return globalSpawn;
    }

    public List<DefaultSpawnOption> getDefaultSpawnOptions() {
        return new ArrayList(defaultSpawnOptions);
    }
}
