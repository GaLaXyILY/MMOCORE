package net.Indyuce.mmocore.manager;

import io.lumine.mythic.lib.api.MMOLineConfig;
import io.lumine.mythic.lib.util.ConfigFile;
import net.Indyuce.mmocore.MMOCore;
import net.Indyuce.mmocore.spawnpoint.SpawnPoint;
import net.Indyuce.mmocore.spawnpoint.def.DefaultSpawnOption;
import org.apache.commons.lang.Validate;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.*;
import java.util.logging.Level;

public class SpawnPointManager implements MMOCoreManager {

    private final Map<String, SpawnPoint> spawnPoints = new HashMap<>();

    private List<DefaultSpawnOption> defaultSpawnOptions = new ArrayList<>();


    public SpawnPoint getSpawnPoint(String id) {
        return spawnPoints.get(id);
    }

    public boolean isSpawnPoint(String id) {
        return spawnPoints.containsKey(id);
    }

    public Collection<SpawnPoint> getAll() {
        return spawnPoints.values();
    }

    @Override
    public void initialize(boolean clearBefore) {
        FileConfiguration config = new ConfigFile(MMOCore.plugin, "", "spawn-points").getConfig();
        Validate.isTrue(config.isConfigurationSection("spawn-points.default"), "You must specify a default spawn-point.");
        if (config.isConfigurationSection("spawn-points"))
            for (String key : config.getConfigurationSection("spawn-points").getKeys(false)) {
                try{
                    SpawnPoint spawnPoint = new SpawnPoint(config.getConfigurationSection("spawn-points." + key));
                    spawnPoints.put(spawnPoint.getId(), spawnPoint);
                }catch (Exception e){
                    MMOCore.log(Level.WARNING,"An error occured while loading spawnpoint " + key + ": " + e.getMessage());
                }

            }
        for (String defaultSpawnOption : config.getStringList("default-spawn")) {
            defaultSpawnOptions.add(MMOCore.plugin.loadManager.loadDefaultSpawnOption(new MMOLineConfig(defaultSpawnOption)));
        }
    }

    public List<DefaultSpawnOption> getDefaultSpawnOptions() {
        return new ArrayList(defaultSpawnOptions);
    }
}
