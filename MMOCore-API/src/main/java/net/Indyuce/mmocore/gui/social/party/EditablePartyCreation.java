package net.Indyuce.mmocore.gui.social.party;

import io.lumine.mythic.lib.gui.framework.EditableInventory;
import io.lumine.mythic.lib.gui.framework.GeneratedInventory;
import io.lumine.mythic.lib.gui.framework.item.InventoryItem;
import io.lumine.mythic.lib.gui.framework.item.SimpleItem;
import net.Indyuce.mmocore.MMOCore;
import net.Indyuce.mmocore.api.player.PlayerData;
import net.Indyuce.mmocore.manager.InventoryManager;
import net.Indyuce.mmocore.party.provided.MMOCorePartyModule;
import org.bukkit.Sound;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.jetbrains.annotations.Nullable;

public class EditablePartyCreation extends EditableInventory<PlayerData> {
    public EditablePartyCreation() {
        super("party-creation");
    }

    @Override
    public GeneratedInventory generate(PlayerData playerData, @Nullable GeneratedInventory generatedInventory) {
        return new ClassConfirmationInventory(playerData, this);
    }

    @Override
    public InventoryItem loadItem(String function, ConfigurationSection config) {
        return new SimpleItem(config);
    }

    public class ClassConfirmationInventory extends GeneratedInventory<PlayerData> {
        public ClassConfirmationInventory(PlayerData playerData, EditableInventory editable) {
            super(playerData, editable);
        }

        @Override
        public String applyNamePlaceholders(String s) {
            return s;
        }

        @Override
        public void whenClicked(InventoryClickEvent event, InventoryItem item) {

            if (item.getFunction().equals("create")) {
                ((MMOCorePartyModule) MMOCore.plugin.partyModule).newRegisteredParty(playerData);
                InventoryManager.PARTY_VIEW.generate(playerData, this).open();
                player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1, 1);
            }

            if (item.getFunction().equals("back"))
                player.closeInventory();
        }
    }
}
