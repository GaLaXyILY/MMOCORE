package net.Indyuce.mmocore.gui.social.friend;

import io.lumine.mythic.lib.gui.framework.EditableInventory;
import io.lumine.mythic.lib.gui.framework.GeneratedInventory;
import io.lumine.mythic.lib.gui.framework.item.InventoryItem;
import io.lumine.mythic.lib.gui.framework.item.Placeholders;
import io.lumine.mythic.lib.gui.framework.item.SimpleItem;
import net.Indyuce.mmocore.MMOCore;
import net.Indyuce.mmocore.api.player.PlayerActivity;
import net.Indyuce.mmocore.api.player.PlayerData;
import net.Indyuce.mmocore.api.util.input.ChatInput;
import net.Indyuce.mmocore.api.util.input.PlayerInput.InputType;
import net.Indyuce.mmocore.api.util.math.format.DelayFormat;
import net.Indyuce.mmocore.manager.InventoryManager;
import org.apache.commons.lang.Validate;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.OfflinePlayer;
import org.bukkit.Sound;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

public class EditableFriendList extends EditableInventory<PlayerData> {
    private static final NamespacedKey UUID_NAMESPACEDKEY = new NamespacedKey(MMOCore.plugin, "Uuid");

    public EditableFriendList() {
        super("friend-list");
    }

    @Override
    public InventoryItem loadItem(String function, ConfigurationSection config) {

        if (function.equals("friend"))
            return new FriendItem(config);

        if (function.equals("previous"))
            return new SimpleItem<FriendListInventory>(config) {

                @Override
                public boolean isDisplayed(FriendListInventory inv) {
                    return inv.page > 0;
                }
            };

        if (function.equals("next"))
            return new SimpleItem<FriendListInventory>(config) {

                @Override
                public boolean isDisplayed(FriendListInventory inv) {
                    return inv.getEditable().getByFunction("friend").getSlots().size() * inv.page < inv.getPlayerData().getFriends().size();
                }
            };

        return new SimpleItem(config);
    }

    @Override
    public GeneratedInventory generate(PlayerData playerData, @Nullable GeneratedInventory generatedInventory) {
        return new FriendListInventory(playerData, this);
    }


    public static class OfflineFriendItem extends InventoryItem<FriendListInventory> {
        public OfflineFriendItem(FriendItem parent, ConfigurationSection config) {
            super(parent, config);
        }

        @Override
        public boolean hasDifferentDisplay() {
            return true;
        }

        @NotNull
        @Override
        public OfflinePlayer getEffectivePlayer(FriendListInventory inv, int n) {
            return Bukkit.getOfflinePlayer(inv.getPlayerData().getFriends().get(n));
        }

        @Override
        public Placeholders getPlaceholders(FriendListInventory inv, int n) {
            OfflinePlayer friend = getEffectivePlayer(inv, n);

            Placeholders holders = new Placeholders();
            holders.register("name", friend.getName());
            holders.register("last_seen", new DelayFormat(2).format(System.currentTimeMillis() - friend.getLastPlayed()));
            return holders;
        }
    }

    public static class OnlineFriendItem extends SimpleItem<FriendListInventory> {
        public OnlineFriendItem(FriendItem parent, ConfigurationSection config) {
            super(parent, config);
        }

        @Override
        public boolean hasDifferentDisplay() {
            return true;
        }

        @NotNull
        @Override
        public OfflinePlayer getEffectivePlayer(FriendListInventory inv, int n) {
            return Bukkit.getOfflinePlayer(inv.getPlayerData().getFriends().get(n));
        }

        @Deprecated
        @Override
        public Placeholders getPlaceholders(FriendListInventory inv, int n) {
            final PlayerData friendData = PlayerData.get(getEffectivePlayer(inv, n));

            Placeholders holders = new Placeholders();
            if (friendData.isOnline())
                holders.register("name", friendData.getPlayer().getName());
            holders.register("class", friendData.getProfess().getName());
            holders.register("level", friendData.getLevel());
            holders.register("online_since", new DelayFormat(2).format(System.currentTimeMillis() - friendData.getLastLogin()));
            return holders;
        }
    }

    public static class FriendItem extends SimpleItem<FriendListInventory> {
        private final OnlineFriendItem online;
        private final OfflineFriendItem offline;

        public FriendItem(ConfigurationSection config) {
            super(config);

            Validate.notNull(config.contains("online"), "Could not load online config");
            Validate.notNull(config.contains("offline"), "Could not load offline config");

            online = new OnlineFriendItem(this, config.getConfigurationSection("online"));
            offline = new OfflineFriendItem(this, config.getConfigurationSection("offline"));
        }

        @Override
        public ItemStack getDisplayedItem(FriendListInventory inv, int n) {
            if (inv.getPlayerData().getFriends().size() <= n)
                return super.getDisplayedItem(inv, n);

            final OfflinePlayer friend = Bukkit.getOfflinePlayer(inv.getPlayerData().getFriends().get(n));
            ItemStack disp = (friend.isOnline() ? online : offline).getDisplayedItem(inv, n);
            ItemMeta meta = disp.getItemMeta();
            meta.getPersistentDataContainer().set(UUID_NAMESPACEDKEY, PersistentDataType.STRING, friend.getUniqueId().toString());
            if (meta instanceof SkullMeta)
                inv.dynamicallyUpdateItem(this, n, disp, current -> {
                    ((SkullMeta) meta).setOwningPlayer(friend);
                    current.setItemMeta(meta);
                });

            disp.setItemMeta(meta);
            return disp;
        }

        @Override
        public boolean hasDifferentDisplay() {
            return true;
        }

        @Override
        public boolean isDisplayed(FriendListInventory inv) {
            return true;
        }
    }

    public class FriendListInventory extends GeneratedInventory<PlayerData> {
        private int page;

        public FriendListInventory(PlayerData playerData, EditableInventory editable) {
            super(playerData, editable);
        }

        @Override
        public String applyNamePlaceholders(String s) {
            return s;
        }

        @Override
        public void whenClicked(InventoryClickEvent event, InventoryItem item) {
            if (item.getFunction().equals("previous")) {
                page--;
                open();
                return;
            }

            if (item.getFunction().equals("next")) {
                page++;
                open();
                return;
            }

            if (item.getFunction().equals("request")) {

                long remaining = playerData.getActivityTimeOut(PlayerActivity.FRIEND_REQUEST);
                if (remaining > 0) {
                    MMOCore.plugin.configManager.getSimpleMessage("friend-request-cooldown", "cooldown", new DelayFormat().format(remaining))
                            .send(player);
                    return;
                }

                new ChatInput(player, InputType.FRIEND_REQUEST, this, input -> {
                    Player target = Bukkit.getPlayer(input);
                    if (target == null) {
                        MMOCore.plugin.configManager.getSimpleMessage("not-online-player", "player", input).send(player);
                        player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1, 1);
                        open();
                        return;
                    }

                    if (playerData.hasFriend(target.getUniqueId())) {
                        MMOCore.plugin.configManager.getSimpleMessage("already-friends", "player", target.getName()).send(player);
                        player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1, 1);
                        open();
                        return;
                    }

                    if (playerData.getUniqueId().equals(target.getUniqueId())) {
                        MMOCore.plugin.configManager.getSimpleMessage("cant-request-to-yourself").send(player);
                        player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1, 1);
                        open();
                        return;
                    }

                    playerData.sendFriendRequest(PlayerData.get(target));
                    MMOCore.plugin.configManager.getSimpleMessage("sent-friend-request", "player", target.getName()).send(player);
                    player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1, 1);
                    open();
                });
            }

            if (item.getFunction().equals("friend") && event.getClick() == ClickType.RIGHT) {
                String tag = event.getCurrentItem().getItemMeta().getPersistentDataContainer().get(UUID_NAMESPACEDKEY, PersistentDataType.STRING);
                if (tag == null || tag.isEmpty())
                    return;

                InventoryManager.FRIEND_REMOVAL.generate(playerData, Bukkit.getOfflinePlayer(UUID.fromString(tag)), this).open();
            }
        }
    }
}
