package net.Indyuce.mmocore.gui.social.friend;

import io.lumine.mythic.lib.gui.framework.EditableInventory;
import io.lumine.mythic.lib.gui.framework.GeneratedInventory;
import io.lumine.mythic.lib.gui.framework.item.InventoryItem;
import io.lumine.mythic.lib.gui.framework.item.Placeholders;
import net.Indyuce.mmocore.MMOCore;
import net.Indyuce.mmocore.api.player.PlayerData;
import net.Indyuce.mmocore.manager.data.OfflinePlayerData;
import org.bukkit.OfflinePlayer;
import org.bukkit.Sound;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.event.inventory.InventoryClickEvent;

public class EditableFriendRemoval extends EditableInventory<PlayerData> {
    public EditableFriendRemoval() {
        super("friend-removal");
    }

    @Override
    public InventoryItem loadItem(String function, ConfigurationSection config) {

        return new InventoryItem<ClassConfirmationInventory>(config) {

            @Override
            public Placeholders getPlaceholders(ClassConfirmationInventory inv, int n) {
                Placeholders holders = new Placeholders();
                holders.register("name", inv.friend.getName());
                return holders;
            }
        };
    }

    public GeneratedInventory newInventory(PlayerData data, OfflinePlayer friend, GeneratedInventory last) {
        return new ClassConfirmationInventory(data, this, friend, last);
    }

    public class ClassConfirmationInventory extends GeneratedInventory<PlayerData> {
        private final OfflinePlayer friend;
        private final GeneratedInventory last;

        public ClassConfirmationInventory(PlayerData playerData, EditableInventory editable, OfflinePlayer friend, GeneratedInventory last) {
            super(playerData, editable);

            this.friend = friend;
            this.last = last;
        }

        @Override
        public String applyNamePlaceholders(String s) {
            return s;
        }

        @Override
        public void whenClicked(InventoryClickEvent event, InventoryItem item) {
            if (item.getFunction().equals("yes")) {
                playerData.removeFriend(friend.getUniqueId());
                OfflinePlayerData.get(friend.getUniqueId()).removeFriend(playerData.getUniqueId());
                player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1, 1);
                MMOCore.plugin.configManager.getSimpleMessage("no-longer-friends", "unfriend", friend.getName()).send(player);
                last.open();
            }

            if (item.getFunction().equals("back"))
                last.open();
        }

        @Override
        public String calculateName() {
            return getName();
        }
    }
}
