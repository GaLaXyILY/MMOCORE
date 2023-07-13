package net.Indyuce.mmocore.spawnpoint;

import io.lumine.mythic.lib.MythicLib;
import io.lumine.mythic.lib.api.player.EquipmentSlot;
import io.lumine.mythic.lib.player.PlayerMetadata;
import io.lumine.mythic.lib.script.Script;
import io.lumine.mythic.lib.script.condition.Condition;
import io.lumine.mythic.lib.skill.SimpleSkill;
import io.lumine.mythic.lib.skill.Skill;
import io.lumine.mythic.lib.skill.handler.MythicLibSkillHandler;
import io.lumine.mythic.lib.skill.trigger.TriggerMetadata;
import io.lumine.mythic.lib.skill.trigger.TriggerType;
import io.lumine.mythic.lib.util.configobject.ConfigSectionObject;
import net.Indyuce.mmocore.MMOCore;
import net.Indyuce.mmocore.api.player.PlayerData;
import net.Indyuce.mmocore.player.Unlockable;
import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;

import java.util.Optional;
import java.util.logging.Level;

public class SpawnPoint implements Unlockable, SpawnProvider {
    private final String id;
    Location location;
    Condition unlockCondition;
    double strength;

    private Optional<Skill> unlockScript;
    private Optional<Skill> respawnScript;

    public SpawnPoint(ConfigurationSection section) {
        id = section.getName();
        location = new Location(MMOCore.plugin.getServer().getWorld(section.getString("world")), section.getDouble("x"), section.getDouble("y"), section.getDouble("z"));
        unlockCondition = MythicLib.plugin.getSkills().loadCondition(
                new ConfigSectionObject(section.getConfigurationSection("unlock-condition")));
        strength = section.getDouble("strength");
        if (section.isConfigurationSection("script.unlock"))
            try {
                final Script script = MythicLib.plugin.getSkills().loadScript(section.getConfigurationSection("script.unlock"));
                unlockScript = Optional.of(new SimpleSkill(TriggerType.CAST, new MythicLibSkillHandler(script)));
            } catch (RuntimeException exception) {
                MMOCore.plugin.getLogger().log(Level.WARNING, "Could not load unlock script: " + exception.getMessage());
            }
        if (section.isConfigurationSection("script.respawn"))
            try {
                final Script script = MythicLib.plugin.getSkills().loadScript(section.getConfigurationSection("script.respawn"));
                respawnScript = Optional.of(new SimpleSkill(TriggerType.CAST, new MythicLibSkillHandler(script)));
            } catch (RuntimeException exception) {
                MMOCore.plugin.getLogger().log(Level.WARNING, "Could not load respawn script: " + exception.getMessage());
            }
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
        PlayerMetadata caster = playerData.getMMOPlayerData().getStatMap().cache(EquipmentSlot.OTHER);
        unlockScript.ifPresent(skill -> skill.cast(new TriggerMetadata(caster)));
    }

    public void whenRespawn(PlayerData playerData) {

    }

    //TODO: Support BungeeCord. Point compliqué car il faut executer le script sur le serveur dans lequel le joueur va etre tp.
    @Override
    public void respawn(PlayerData playerData) {
        playerData.getPlayer().teleport(location);
        PlayerMetadata caster = playerData.getMMOPlayerData().getStatMap().cache(EquipmentSlot.OTHER);
        respawnScript.ifPresent(skill -> skill.cast(new TriggerMetadata(caster)));
    }
}
