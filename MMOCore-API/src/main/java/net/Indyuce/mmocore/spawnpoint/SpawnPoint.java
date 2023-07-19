package net.Indyuce.mmocore.spawnpoint;

import io.lumine.mythic.lib.MythicLib;
import io.lumine.mythic.lib.UtilityMethods;
import io.lumine.mythic.lib.api.player.EquipmentSlot;
import io.lumine.mythic.lib.script.Script;
import io.lumine.mythic.lib.script.condition.Condition;
import io.lumine.mythic.lib.skill.SimpleSkill;
import io.lumine.mythic.lib.skill.Skill;
import io.lumine.mythic.lib.skill.SkillMetadata;
import io.lumine.mythic.lib.skill.handler.MythicLibSkillHandler;
import io.lumine.mythic.lib.skill.trigger.TriggerMetadata;
import io.lumine.mythic.lib.skill.trigger.TriggerType;
import io.lumine.mythic.lib.util.configobject.ConfigSectionObject;
import net.Indyuce.mmocore.MMOCore;
import net.Indyuce.mmocore.api.player.PlayerData;
import net.Indyuce.mmocore.player.Unlockable;
import org.apache.commons.lang.Validate;
import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;

import java.util.Optional;
import java.util.logging.Level;

public class SpawnPoint implements Unlockable {
    private final String id;

    private final Optional<String> server;
    private final Location location;
    private final Condition unlockCondition;
    private final double strength;

    private final Optional<Skill> unlockScript;
    private final Optional<Skill> respawnScript;

    private final boolean overridesUnlockCondition, overridesRespawnScript, overridesUnlockScript;

    public SpawnPoint(ConfigurationSection section) {
        id = section.getName();
        server = Optional.ofNullable(section.getString("server"));
        location = UtilityMethods.readLocation(new ConfigSectionObject(section.getConfigurationSection("location")));
        Validate.isTrue(section.isConfigurationSection("unlock-condition"), "You must specify an unlock condition.");
        unlockCondition = MythicLib.plugin.getSkills().loadCondition(
                new ConfigSectionObject(section.getConfigurationSection("unlock-condition")));

        strength = section.getDouble("strength", 1);
        overridesRespawnScript = section.getBoolean("override-respawn-script", false);
        overridesUnlockScript = section.getBoolean("override-unlock-script", false);
        overridesUnlockCondition = section.getBoolean("override-unlock-condition", false);
        Skill unlockScript = null;
        if (section.isConfigurationSection("unlock-script"))
            try {
                final Script script = MythicLib.plugin.getSkills().loadScript(section.getConfigurationSection("unlock-script"));
                unlockScript = new SimpleSkill(TriggerType.CAST, new MythicLibSkillHandler(script));
            } catch (RuntimeException exception) {
                MMOCore.log(Level.WARNING, "Could not load unlock script: " + exception.getMessage());
            }
        this.unlockScript = Optional.ofNullable(unlockScript);

        Skill respawnScript = null;
        if (section.isConfigurationSection("respawn-script"))
            try {
                final Script script = MythicLib.plugin.getSkills().loadScript(section.getConfigurationSection("respawn-script"));
                respawnScript = new SimpleSkill(TriggerType.CAST, new MythicLibSkillHandler(script));
            } catch (RuntimeException exception) {
                MMOCore.log(Level.WARNING, "Could not load respawn script: " + exception.getMessage());
            }
        this.respawnScript = Optional.ofNullable(respawnScript);
    }

    public String getId() {
        return id;
    }

    public Location getLocation() {
        return location;
    }

    @Override
    public String getUnlockNamespacedKey() {
        return "spawnpoint:" + id;
    }

    public double getStrength() {
        return strength;
    }

    @Override
    public boolean isUnlockedByDefault() {
        return false;
    }

    @Override
    public void whenLocked(PlayerData playerData) {
    }

    @Override
    public void whenUnlocked(PlayerData playerData) {
        TriggerMetadata triggerMetadata = new TriggerMetadata(playerData.getMMOPlayerData().getStatMap().cache(EquipmentSlot.MAIN_HAND));
        if (!overridesUnlockScript)
            MMOCore.plugin.spawnPointManager.getUnlockScript().ifPresent(skill -> skill.cast(triggerMetadata));
        unlockScript.ifPresent(skill -> skill.cast(triggerMetadata));
    }

    public boolean isOtherServer() {
        return !server.isEmpty() && server.get() != MMOCore.plugin.pluginMessageManager.getServerName();
    }

    public boolean matchesCondition(PlayerData playerData) {
        SkillMetadata metadata = new SkillMetadata(null, playerData.getMMOPlayerData());
        if (!overridesUnlockCondition && !MMOCore.plugin.spawnPointManager.getUnlockCondition()
                .map((condition) -> condition.isMet(metadata)).orElse(true))
            return false;
        return unlockCondition.isMet(metadata);
    }

    public void whenRespawn(PlayerData playerData) {
        if (isOtherServer()) {
            MMOCore.plugin.pluginMessageManager.teleportToOtherServer(playerData, server.get());
        } else {
            playerData.setLastUsedSpawnPoint(this);
            TriggerMetadata triggerMetadata = new TriggerMetadata(playerData.getMMOPlayerData().getStatMap().cache(EquipmentSlot.MAIN_HAND));
            if (!overridesRespawnScript)
                MMOCore.plugin.spawnPointManager.getRespawnScript().ifPresent(skill -> skill.cast(triggerMetadata));
            respawnScript.ifPresent(skill -> skill.cast(triggerMetadata));
        }

    }
}
