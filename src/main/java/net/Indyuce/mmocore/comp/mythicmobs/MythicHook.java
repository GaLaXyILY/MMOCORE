package net.Indyuce.mmocore.comp.mythicmobs;

import io.lumine.xikage.mythicmobs.MythicMobs;
import io.lumine.xikage.mythicmobs.api.bukkit.events.MythicDropLoadEvent;
import io.lumine.xikage.mythicmobs.api.bukkit.events.MythicReloadedEvent;
import io.lumine.xikage.mythicmobs.skills.placeholders.Placeholder;
import net.Indyuce.mmocore.MMOCore;
import net.Indyuce.mmocore.api.player.PlayerData;
import net.Indyuce.mmocore.api.player.attribute.PlayerAttribute;
import net.Indyuce.mmocore.api.player.attribute.PlayerAttributes;
import net.Indyuce.mmocore.comp.mythicmobs.load.CurrencyItemDrop;
import net.Indyuce.mmocore.comp.mythicmobs.load.GoldPouchDrop;
import net.Indyuce.mmocore.skill.RegisteredSkill;
import org.apache.commons.lang.Validate;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

import java.util.Optional;
import java.util.logging.Level;

public class MythicHook implements Listener {
    public MythicHook() {
        registerPlaceholders();
    }

    @EventHandler
    public void a(MythicDropLoadEvent event) {

        // random gold pouches
        if (event.getDropName().equalsIgnoreCase("gold_pouch") || event.getDropName().equalsIgnoreCase("goldpouch"))
            event.register(new GoldPouchDrop(event.getConfig()));

        // gold coins
        if (event.getDropName().equalsIgnoreCase("gold_coin") || event.getDropName().equalsIgnoreCase("coin"))
            event.register(new CurrencyItemDrop("GOLD_COIN", event.getConfig()));

        // notes
        if (event.getDropName().equalsIgnoreCase("note") || event.getDropName().equalsIgnoreCase("banknote") || event.getDropName().equalsIgnoreCase("bank_note"))
            event.register(new CurrencyItemDrop("NOTE", event.getConfig()));
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void b(MythicReloadedEvent event) {

        // When MM is reloaded, reload placeholders because they are no longer registered
        registerPlaceholders();

        // Reload skills
        MMOCore.plugin.skillManager.initialize(true);
    }

    private void registerPlaceholders() {

        // Resource
        MythicMobs.inst().getPlaceholderManager().register("mana", Placeholder.meta((metadata, arg) -> {
            return String.valueOf((int) PlayerData.get(metadata.getCaster().getEntity().getUniqueId()).getMana());
        }));
        MythicMobs.inst().getPlaceholderManager().register("stamina", Placeholder.meta((metadata, arg) -> {
            return String.valueOf((int) PlayerData.get(metadata.getCaster().getEntity().getUniqueId()).getStamina());
        }));
        MythicMobs.inst().getPlaceholderManager().register("stellium", Placeholder.meta((metadata, arg) -> {
            return String.valueOf((int) PlayerData.get(metadata.getCaster().getEntity().getUniqueId()).getStellium());
        }));

        // Attributes
        MythicMobs.inst().getPlaceholderManager().register("attribute", Placeholder.meta((metadata, arg) -> {
            PlayerAttributes attributes = PlayerData.get(metadata.getCaster().getEntity().getUniqueId()).getAttributes();
            PlayerAttribute attribute = MMOCore.plugin.attributeManager.get(arg);
            return String.valueOf(attributes.getAttribute(attribute));
        }));

    }
}