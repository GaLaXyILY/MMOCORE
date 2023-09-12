package net.Indyuce.mmocore.gui.social.guild;

import io.lumine.mythic.lib.gui.framework.EditableInventory;
import io.lumine.mythic.lib.gui.framework.GeneratedInventory;
import io.lumine.mythic.lib.gui.framework.item.InventoryItem;
import io.lumine.mythic.lib.gui.framework.item.Placeholders;
import io.lumine.mythic.lib.gui.framework.item.SimpleItem;
import net.Indyuce.mmocore.MMOCore;
import net.Indyuce.mmocore.api.player.PlayerData;
import net.Indyuce.mmocore.api.util.math.format.DelayFormat;
import net.Indyuce.mmocore.api.util.input.ChatInput;
import net.Indyuce.mmocore.api.util.input.PlayerInput;
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
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.UUID;

public class EditableGuildAdmin extends EditableInventory<PlayerData> {
    private static final NamespacedKey UUID_NAMESPACEDKEY = new NamespacedKey(MMOCore.plugin, "Uuid");

    public EditableGuildAdmin() {
        super("guild-admin");
    }

    @Override
    public GeneratedInventory generate(PlayerData playerData, @Nullable GeneratedInventory generatedInventory) {
        return new GuildViewInventory(playerData, this);

    }

    @Override
    public InventoryItem loadItem(String function, ConfigurationSection config) {
        return function.equals("member") ? new MemberItem(config) : new SimpleItem(config);
    }

    public static class MemberDisplayItem extends InventoryItem<GuildViewInventory> {
        public MemberDisplayItem(MemberItem memberItem, ConfigurationSection config) {
            super(memberItem, config);
        }

        @Override
        public boolean hasDifferentDisplay() {
            return true;
        }

        @Override
        public Placeholders getPlaceholders(GuildViewInventory inv, int n) {
            PlayerData member = PlayerData.get(inv.members.get(n));

            Placeholders holders = new Placeholders();

            if (member.isOnline())
                holders.register("name", member.getPlayer().getName());
            holders.register("class", member.getProfess().getName());
            holders.register("level", "" + member.getLevel());
            holders.register("since", new DelayFormat(2).format(System.currentTimeMillis() - member.getLastLogin()));
            return holders;
        }

        @Override
        public ItemStack getDisplayedItem(GuildViewInventory inv, int n) {
            UUID uuid = inv.members.get(n);
            OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(uuid);

            ItemStack disp = super.getDisplayedItem(inv, n);
            ItemMeta meta = disp.getItemMeta();
            meta.getPersistentDataContainer().set(UUID_NAMESPACEDKEY, PersistentDataType.STRING, uuid.toString());

            if (meta instanceof SkullMeta && offlinePlayer != null)
                inv.dynamicallyUpdateItem(this, n, disp, current -> {
                    ((SkullMeta) meta).setOwningPlayer(offlinePlayer);
                    current.setItemMeta(meta);
                });

            disp.setItemMeta(meta);
            return disp;
        }
    }

    public static class MemberItem extends SimpleItem<GuildViewInventory> {
        private final InventoryItem empty;
        private final MemberDisplayItem member;

        public MemberItem(ConfigurationSection config) {
            super(config);

            Validate.notNull(config.contains("empty"), "Could not load empty config");
            Validate.notNull(config.contains("member"), "Could not load member config");

            empty = new SimpleItem(config.getConfigurationSection("empty"));
            member = new MemberDisplayItem(this, config.getConfigurationSection("member"));
        }

        @Override
        public ItemStack getDisplayedItem(GuildViewInventory inv, int n) {
            return inv.getPlayerData().getGuild().countMembers() > n ? member.getDisplayedItem(inv, n) : empty.getDisplayedItem(inv, n);
        }

        @Override
        public boolean hasDifferentDisplay() {
            return true;
        }
    }

    public class GuildViewInventory extends GeneratedInventory<PlayerData> {
        private final int max;

        private List<UUID> members;

        public GuildViewInventory(PlayerData playerData, EditableInventory editable) {
            super(playerData, editable);

            max = editable.getByFunction("member").getSlots().size();
        }

        @Override
        public void open() {
            members = playerData.getGuild().listMembers();
            super.open();
        }

        @Override
        public String applyNamePlaceholders(String s) {
            return s.replace("{max}", "" + max)
                    .replace("{players}", "" + getPlayerData().getGuild().countMembers());

        }

        @Override
        public void whenClicked(InventoryClickEvent event, InventoryItem item) {

            if (item.getFunction().equals("leave")) {
                playerData.getGuild().removeMember(playerData.getUniqueId());
                player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1, 1);
                player.closeInventory();
                return;
            }

            if (item.getFunction().equals("invite")) {

                if (playerData.getGuild().countMembers() >= max) {
                    MMOCore.plugin.configManager.getSimpleMessage("guild-is-full").send(player);
                    player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1, 1);
                    return;
                }

                new ChatInput(player, PlayerInput.InputType.GUILD_INVITE, this, input -> {
                    Player target = Bukkit.getPlayer(input);
                    if (target == null) {
                        MMOCore.plugin.configManager.getSimpleMessage("not-online-player", "player", input).send(player);
                        player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1, 1);
                        open();
                        return;
                    }

                    long remaining = playerData.getGuild().getLastInvite(target) + 60 * 2 * 1000 - System.currentTimeMillis();
                    if (remaining > 0) {
                        MMOCore.plugin.configManager.getSimpleMessage("guild-invite-cooldown", "player", target.getName(), "cooldown",
                                new DelayFormat().format(remaining)).send(player);
                        open();
                        return;
                    }

                    PlayerData targetData = PlayerData.get(target);
                    if (playerData.getGuild().hasMember(target.getUniqueId())) {
                        MMOCore.plugin.configManager.getSimpleMessage("already-in-guild", "player", target.getName()).send(player);
                        player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1, 1);
                        open();
                        return;
                    }

                    playerData.getGuild().sendGuildInvite(playerData, targetData);
                    MMOCore.plugin.configManager.getSimpleMessage("sent-guild-invite", "player", target.getName()).send(player);
                    player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1, 1);
                    open();
                });
            }

            if (item.getFunction().equals("member") && event.getClick() == ClickType.RIGHT) {
                if (!playerData.getGuild().getOwner().equals(playerData.getUniqueId()))
                    return;

                OfflinePlayer target = Bukkit.getOfflinePlayer(UUID.fromString(event.getCurrentItem().getItemMeta().getPersistentDataContainer().get(UUID_NAMESPACEDKEY, PersistentDataType.STRING)));
                if (target.equals(player))
                    return;

                playerData.getGuild().removeMember(target.getUniqueId());
                MMOCore.plugin.configManager.getSimpleMessage("kick-from-guild", "player", target.getName()).send(player);
                player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1, 1);
            }
        }
    }
}
