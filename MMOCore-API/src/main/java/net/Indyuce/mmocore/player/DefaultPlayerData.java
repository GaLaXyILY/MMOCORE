package net.Indyuce.mmocore.player;

import net.Indyuce.mmocore.MMOCore;
import net.Indyuce.mmocore.api.player.PlayerData;
import net.Indyuce.mmocore.manager.data.PlayerDataManager;
import net.Indyuce.mmocore.skill.ClassSkill;
import net.Indyuce.mmocore.skilltree.SkillTreeNode;
import org.bukkit.attribute.Attribute;
import org.bukkit.configuration.ConfigurationSection;

import java.util.*;

public class DefaultPlayerData implements ClassDataContainer {
    private final int level, classPoints, skillPoints, attributePoints, attrReallocPoints, skillReallocPoints, skillTreeReallocPoints;
    private final double health, mana, stamina, stellium;
    private final Map<String, Integer> skillLevels, skillTreePoints, nodeLevels, attributeLevels;
    private final Set<String> unlockedItems;
    private final Map<Integer, String> boundSkills;

    public static final DefaultPlayerData DEFAULT = new DefaultPlayerData(
        1,
        0,
        0,
        0,
        0,
        0,
        0,
        20,
        0,
        0,
        0,
        new HashMap<>(),
        new HashMap<>(),
        new HashMap<>(),
        new HashMap<>(),
        new HashSet<>(),
        new HashMap<>()
    );

    public DefaultPlayerData(ConfigurationSection config) {
        level = config.getInt("level", 1);
        classPoints = config.getInt("class-points");
        skillPoints = config.getInt("skill-points");
        attributePoints = config.getInt("attribute-points");
        attrReallocPoints = config.getInt("attribute-realloc-points");
        skillReallocPoints = config.getInt("skill-realloc-points", 0);
        skillTreeReallocPoints = config.getInt("skill-tree-realloc-points", 0);
        health = config.getDouble("health", 20);
        mana = config.getDouble("mana", 20);
        stamina = config.getDouble("stamina", 20);
        stellium = config.getDouble("stellium", 20);

        skillLevels = config.isConfigurationSection("skill") ? (Map) config.getConfigurationSection("skill").getValues(false) : new HashMap<>();
        skillTreePoints = config.isConfigurationSection("skill-tree-points") ? (Map) config.getConfigurationSection("skill-tree-points").getValues(false) : new HashMap<>();
        nodeLevels = config.isConfigurationSection("skill-tree-level") ? (Map) config.getConfigurationSection("skill-tree-level").getValues(false) : new HashMap<>();
        attributeLevels = config.isConfigurationSection("attribute") ? (Map) config.getConfigurationSection("attribute").getValues(false) : new HashMap<>();
        unlockedItems = new HashSet<>(config.getStringList("unlocked-items"));
        boundSkills = new HashMap<>();
        if (config.isConfigurationSection("bound-skills")) {
            for (String key : config.getConfigurationSection("bound-skills").getKeys(false)) {
                boundSkills.put(Integer.parseInt(key), config.getString("bound-skills." + key));
            }
        }
    }

    public DefaultPlayerData(int level, int classPoints, int skillPoints, int attributePoints,
                             int attrReallocPoints, int skillReallocPoints, int skillTreeReallocPoints,
                             double health, double mana, double stamina, double stellium,
                             Map<String, Integer> skillLevels, Map<String, Integer> skillTreePoints,
                             Map<String, Integer> nodeLevels, Map<String, Integer> attributeLevels,
                             Set<String> unlockedItems, Map<Integer, String> boundSkills) {
        this.level = level;
        this.classPoints = classPoints;
        this.skillPoints = skillPoints;
        this.attributePoints = attributePoints;
        this.attrReallocPoints = attrReallocPoints;
        this.skillReallocPoints = skillReallocPoints;
        this.skillTreeReallocPoints = skillTreeReallocPoints;
        this.health = health;
        this.mana = mana;
        this.stamina = stamina;
        this.stellium = stellium;

        this.skillLevels = skillLevels;
        this.skillTreePoints = skillTreePoints;
        this.nodeLevels = nodeLevels;
        this.attributeLevels = attributeLevels;
        this.unlockedItems = unlockedItems;
        this.boundSkills = boundSkills;
    }

    public int getLevel() {
        return level;
    }

    @Override
    public double getExperience() {
        return 0;
    }

    @Override
    public double getHealth() {
        return health;
    }

    @Override
    public double getMana() {
        return mana;
    }

    @Override
    public double getStamina() {
        return stamina;
    }

    @Override
    public double getStellium() {
        return stellium;
    }

    @Override
    public int getSkillPoints() {
        return skillPoints;
    }

    public int getClassPoints() {
        return classPoints;
    }

    @Override
    public int getAttributePoints() {
        return attributePoints;
    }

    @Override
    public int getAttributeReallocationPoints() {
        return attrReallocPoints;
    }

    @Override
    public int getSkillReallocationPoints() {
        return skillReallocPoints;
    }

    @Override
    public int getSkillTreeReallocationPoints() {
        return skillTreeReallocPoints;
    }

    @Override
    public Map<String, Integer> mapSkillLevels() {
        return this.skillLevels;
    }

    @Override
    public Map<String, Integer> mapSkillTreePoints() {
        return this.skillTreePoints;
    }

    @Override
    public Map<String, Integer> getNodeLevels() {
        return this.nodeLevels;
    }

    @Override
    public Map<String, Integer> getNodeTimesClaimed() {
        return new HashMap<>();
    }

    @Override
    public Set<String> getUnlockedItems() {
        return this.unlockedItems;
    }

    @Override
    public Map<String, Integer> mapAttributeLevels() {
        return this.attributeLevels;
    }

    @Override
    public Map<Integer, String> mapBoundSkills() {
        return this.boundSkills;
    }

    public void apply(PlayerData player) {
        player.setLevel(level);
        player.setExperience(0);
        player.setClassPoints(classPoints);
        player.setSkillPoints(skillPoints);
        player.setAttributePoints(attributePoints);
        player.setAttributeReallocationPoints(attrReallocPoints);
        player.setSkillTreeReallocationPoints(skillTreeReallocPoints);
        player.setSkillReallocationPoints(skillReallocPoints);
        if (player.isOnline())
            player.getPlayer().setHealth(Math.min(health, player.getPlayer().getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue()));
        player.setMana(mana);
        player.setStamina(stamina);
        player.setStellium(stellium);

        skillLevels.forEach(player::setSkillLevel);
        skillTreePoints.forEach(player::setSkillTreePoints);
        nodeLevels.forEach((nodeId, level) -> player.setNodeLevel(MMOCore.plugin.skillTreeManager.getNode(nodeId), level));
        attributeLevels.forEach((attribute, level) -> player.getAttributes().getInstance(attribute).setBase(level));
        player.setUnlockedItems(unlockedItems);
        boundSkills.forEach((slot, skill) -> player.bindSkill(slot, player.getProfess().getSkill(skill)));
    }
}
