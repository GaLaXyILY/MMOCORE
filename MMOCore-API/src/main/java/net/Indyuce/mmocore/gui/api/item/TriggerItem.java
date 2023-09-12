package net.Indyuce.mmocore.gui.api.item;

import io.lumine.mythic.lib.api.MMOLineConfig;
import io.lumine.mythic.lib.gui.framework.GeneratedInventory;
import io.lumine.mythic.lib.gui.framework.item.InventoryItem;
import io.lumine.mythic.lib.gui.framework.item.Placeholders;
import net.Indyuce.mmocore.MMOCore;
import net.Indyuce.mmocore.api.quest.trigger.Trigger;
import org.bukkit.configuration.ConfigurationSection;

public class TriggerItem extends InventoryItem {
    private final Trigger triggers;

    public TriggerItem(ConfigurationSection config, String format) {
        super(config);

        triggers = MMOCore.plugin.loadManager.loadTrigger(new MMOLineConfig(format));
    }

    @Override
    public Placeholders getPlaceholders(GeneratedInventory inv, int n) {
        return new Placeholders();
    }

    public Trigger getTrigger() {
        return triggers;
    }
}
