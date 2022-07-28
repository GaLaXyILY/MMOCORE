package net.Indyuce.mmocore.api.load;

import io.lumine.mythic.lib.api.MMOLineConfig;
import net.Indyuce.mmocore.api.block.BlockType;
import net.Indyuce.mmocore.api.quest.trigger.Trigger;
import net.Indyuce.mmocore.loot.chest.condition.Condition;
import net.Indyuce.mmocore.loot.droptable.dropitem.DropItem;
import net.Indyuce.mmocore.experience.dispenser.ExperienceDispenser;
import net.Indyuce.mmocore.experience.source.type.ExperienceSource;
import net.Indyuce.mmocore.api.quest.objective.Objective;
import org.bukkit.configuration.ConfigurationSection;

import java.util.List;

/**
 * MMOLoader was initially an interface but it is now a
 * class so devs do not have to add a new method
 * everytime the class is updated.
 */
public class MMOLoader {

    public List<Condition> loadCondition(MMOLineConfig config) {
        return null;
    }

    public List<Trigger> loadTrigger(MMOLineConfig config) {
        return null;
    }

    public DropItem loadDropItem(MMOLineConfig config) {
        return null;
    }

    public Objective loadObjective(MMOLineConfig config, ConfigurationSection section) {
        return null;
    }

    public List<ExperienceSource<?>> loadExperienceSource(MMOLineConfig config, ExperienceDispenser dispenser) {
        return null;
    }

    public BlockType loadBlockType(MMOLineConfig config) {
        return null;
    }
}
