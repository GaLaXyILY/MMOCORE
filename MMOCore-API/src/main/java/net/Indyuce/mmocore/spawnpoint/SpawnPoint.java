package net.Indyuce.mmocore.spawnpoint;

import io.lumine.mythic.lib.MythicLib;
import io.lumine.mythic.lib.UtilityMethods;
import io.lumine.mythic.lib.api.player.EquipmentSlot;
import io.lumine.mythic.lib.player.PlayerMetadata;
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
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;

import java.util.Optional;
import java.util.logging.Level;

public class SpawnPoint implements Unlockable {
    private final String id;
    private final Location location;
    private final Condition unlockCondition;
    private final double strength;

    private final Optional<Skill> unlockScript;
    private final Optional<Skill> respawnScript;

    public SpawnPoint(ConfigurationSection section) {
        id = section.getName();
        location = UtilityMethods.readLocation(new ConfigSectionObject(section.getConfigurationSection("location")));
        Validate.isTrue(section.isConfigurationSection("unlock-condition"), "You must specify an unlock condition.");
        unlockCondition = MythicLib.plugin.getSkills().loadCondition(
                new ConfigSectionObject(section.getConfigurationSection("unlock-condition")));

        strength = section.getDouble("strength", 1);
        Skill unlockScript = null;
        if (section.isConfigurationSection("unlock-script"))
            try {
                final Script script = MythicLib.plugin.getSkills().loadScript(section.getConfigurationSection("unlock-script"));
                unlockScript = new SimpleSkill(TriggerType.CAST, new MythicLibSkillHandler(script));
            } catch (RuntimeException exception) {
                MMOCore.plugin.getLogger().log(Level.WARNING, "Could not load unlock script: " + exception.getMessage());
            }
        this.unlockScript = Optional.ofNullable(unlockScript);
        Skill respawnScript = null;
        if (section.isConfigurationSection("respawn-script"))
            try {
                final Script script = MythicLib.plugin.getSkills().loadScript(section.getConfigurationSection("respawn-script"));
                respawnScript = new SimpleSkill(TriggerType.CAST, new MythicLibSkillHandler(script));
            } catch (RuntimeException exception) {
                MMOCore.plugin.getLogger().log(Level.WARNING, "Could not load respawn script: " + exception.getMessage());
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
        PlayerMetadata caster = playerData.getMMOPlayerData().getStatMap().cache(EquipmentSlot.MAIN_HAND);
        unlockScript.ifPresent(skill -> skill.cast(new TriggerMetadata(caster)));
    }

    public boolean matchesCondition(PlayerData playerData) {
        return unlockCondition.isMet(new SkillMetadata(null, playerData.getMMOPlayerData()));
    }

    public void whenRespawn(PlayerData playerData) {
       PlayerMetadata caster = playerData.getMMOPlayerData().getStatMap().cache(EquipmentSlot.MAIN_HAND);
        respawnScript.ifPresent(skill -> skill.cast(new TriggerMetadata(caster)));
    }
}
