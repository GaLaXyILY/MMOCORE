package net.Indyuce.mmocore.gui.api.item;

import io.lumine.mythic.lib.api.MMOLineConfig;
import net.Indyuce.mmocore.MMOCore;
import net.Indyuce.mmocore.api.quest.trigger.Trigger;
import net.Indyuce.mmocore.gui.api.GeneratedInventory;
import org.bukkit.configuration.ConfigurationSection;

import java.util.List;

public class TriggerItem extends InventoryItem {
    private final List<Trigger> triggers;

    public TriggerItem(ConfigurationSection config, String format) {
        super(config);

        triggers = MMOCore.plugin.loadManager.loadTrigger(new MMOLineConfig(format));
    }

    @Override
    public Placeholders getPlaceholders(GeneratedInventory inv, int n) {
        return new Placeholders();
    }

    public List<Trigger> getTriggers() {
        return triggers;
    }
}
