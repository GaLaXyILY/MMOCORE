package net.Indyuce.mmocore.manager;

import io.lumine.mythic.lib.MythicLib;
import io.lumine.mythic.lib.api.MMOLineConfig;
import io.lumine.mythic.lib.script.Script;
import io.lumine.mythic.lib.script.condition.Condition;
import io.lumine.mythic.lib.skill.SimpleSkill;
import io.lumine.mythic.lib.skill.Skill;
import io.lumine.mythic.lib.skill.handler.MythicLibSkillHandler;
import io.lumine.mythic.lib.skill.trigger.TriggerType;
import io.lumine.mythic.lib.util.ConfigFile;
import io.lumine.mythic.lib.util.configobject.ConfigSectionObject;
import net.Indyuce.mmocore.MMOCore;
import net.Indyuce.mmocore.spawnpoint.SpawnPoint;
import net.Indyuce.mmocore.spawnpoint.def.DefaultSpawnOption;
import org.bukkit.configuration.file.FileConfiguration;

import javax.swing.text.html.Option;
import java.util.*;
import java.util.logging.Level;

public class SpawnPointManager implements MMOCoreManager {

    private Optional<Condition> globalUnlockCondition;
    private Optional<Skill> globalUnlockScript;
    private Optional<Skill> globalRespawnScript;

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

        if (config.isConfigurationSection("spawn-points"))
            for (String key : config.getConfigurationSection("spawn-points").getKeys(false)) {
                try {
                    SpawnPoint spawnPoint = new SpawnPoint(config.getConfigurationSection("spawn-points." + key));
                    spawnPoints.put(spawnPoint.getId(), spawnPoint);
                } catch (Exception e) {
                    MMOCore.log(Level.WARNING, "An error occured while loading spawnpoint " + key + ": " + e.getMessage());
                }

            }
        for (String defaultSpawnOption : config.getStringList("default-spawn")) {
            defaultSpawnOptions.add(MMOCore.plugin.loadManager.loadDefaultSpawnOption(new MMOLineConfig(defaultSpawnOption)));
        }


        Skill globalUnlockScript = null;
        if (config.isConfigurationSection("global.unlock-script"))
            try {
                final Script script = MythicLib.plugin.getSkills().loadScript(config.getConfigurationSection("global.unlock-script"));
                globalUnlockScript = new SimpleSkill(TriggerType.CAST, new MythicLibSkillHandler(script));
            } catch (RuntimeException exception) {
                MMOCore.plugin.getLogger().log(Level.WARNING, "Could not load unlock script: " + exception.getMessage());
            }
        this.globalUnlockScript = Optional.ofNullable(globalUnlockScript);

        Skill globalRespawnScript = null;
        if (config.isConfigurationSection("global.respawn-script"))
            try {
                final Script script = MythicLib.plugin.getSkills().loadScript(config.getConfigurationSection("global.respawn-script"));
                globalRespawnScript = new SimpleSkill(TriggerType.CAST, new MythicLibSkillHandler(script));
            } catch (RuntimeException exception) {
                MMOCore.plugin.getLogger().log(Level.WARNING, "Could not load respawn script: " + exception.getMessage());
            }
        this.globalRespawnScript = Optional.ofNullable(globalRespawnScript);

        Condition globalUnlockCondition = null;
        if (config.isConfigurationSection("global.unlock-condition"))
            globalUnlockCondition = MythicLib.plugin.getSkills().loadCondition(new ConfigSectionObject(config.getConfigurationSection("global.unlock-condition")));
        this.globalUnlockCondition = Optional.ofNullable(globalUnlockCondition);


    }

    public Optional<Condition> getUnlockCondition() {
        return globalUnlockCondition;
    }

    public Optional<Skill> getUnlockScript() {
        return globalUnlockScript;
    }

    public Optional<Skill> getRespawnScript() {
        return globalRespawnScript;
    }

    public List<DefaultSpawnOption> getDefaultSpawnOptions() {
        return new ArrayList(defaultSpawnOptions);
    }
}
