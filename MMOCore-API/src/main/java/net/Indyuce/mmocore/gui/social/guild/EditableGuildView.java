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
import net.Indyuce.mmocore.manager.data.OfflinePlayerData;
import org.apache.commons.lang.Validate;
import org.bukkit.*;
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

public class EditableGuildView extends EditableInventory<PlayerData> {
    private static final NamespacedKey UUID_NAMESPACEDKEY = new NamespacedKey(MMOCore.plugin, "Uuid");

    public EditableGuildView() {
        super("guild-view");
    }

    @Override
    public GeneratedInventory generate(PlayerData playerData, @Nullable GeneratedInventory generatedInventory) {
        return new GuildViewInventory(playerData, this);
    }

    @Override
    public InventoryItem loadItem(String function, ConfigurationSection config) {
        return function.equals("member") ? new MemberItem(config) : (function.equals("next") || function.equals("previous") || function.equals("disband") || function.equals("invite")) ? new ConditionalItem(function, config) : new SimpleItem(config);
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
            UUID uuid = inv.members.get(n);
            Placeholders holders = new Placeholders();
            /*
             * Will never be null since a players name will always be recorded
             * if they've been in a guild
             */
            holders.register("name", Bukkit.getOfflinePlayer(uuid).getName());

            OfflinePlayerData offline = OfflinePlayerData.get(uuid);
            holders.register("class", offline.getProfess().getName());
            holders.register("level", offline.getLevel());
            holders.register("since", new DelayFormat(2).format(System.currentTimeMillis() - offline.getLastLogin()));

            return holders;
        }

        @Override
        public ItemStack getDisplayedItem(GuildViewInventory inv, int n) {
            UUID uuid = inv.members.get(n);

            ItemStack disp = super.getDisplayedItem(inv, n);
            ItemMeta meta = disp.getItemMeta();
            meta.getPersistentDataContainer().set(UUID_NAMESPACEDKEY, PersistentDataType.STRING, uuid.toString());

            if (meta instanceof SkullMeta)
                inv.dynamicallyUpdateItem(this, n, disp, current -> {
                    ((SkullMeta) meta).setOwningPlayer(Bukkit.getOfflinePlayer(uuid));
                    current.setItemMeta(meta);
                });

            disp.setItemMeta(meta);
            return disp;
        }
    }

    public class MemberItem extends SimpleItem<GuildViewInventory> {
        private final InventoryItem empty;
        private final MemberDisplayItem member;

        public MemberItem(ConfigurationSection config) {
            super(Material.BARRIER, config);

            Validate.notNull(config.contains("empty"), "Could not load empty config");
            Validate.notNull(config.contains("member"), "Could not load member config");

            empty = new SimpleItem(config.getConfigurationSection("empty"));
            member = new MemberDisplayItem(this, config.getConfigurationSection("member"));
        }

        @Override
        public ItemStack getDisplayedItem(GuildViewInventory inv, int n) {
            int index = n * inv.getPage();
            return inv.getPlayerData().getGuild().countMembers() > index ? member.getDisplayedItem(inv, index) : empty.getDisplayedItem(inv, index);
        }

        @Override
        public boolean hasDifferentDisplay() {
            return true;
        }
    }

    public class ConditionalItem extends SimpleItem<GuildViewInventory> {
        private final String function;

        public ConditionalItem(String func, ConfigurationSection config) {
            super(config);
            this.function = func;
        }

        @Override
        public ItemStack getDisplayedItem(GuildViewInventory inv, int n) {

            if (function.equals("next"))
                if (inv.getPage() == (inv.getPlayerData().getGuild().countMembers() + 20)
                        / inv.getByFunction("member").getSlots().size())
                    return null;
            if (function.equals("previous") && inv.getPage() == 1)
                return null;
            if ((function.equals("disband") || function.equals("invite")) && !inv.getPlayerData().getGuild().getOwner().equals(inv.getPlayer().getUniqueId()))
                return null;
            return super.getDisplayedItem(inv, n);
        }
    }


    public class GuildViewInventory extends GeneratedInventory<PlayerData> {
        private final int maxpages;

        private int page = 1;
        private List<UUID> members;

        public GuildViewInventory(PlayerData playerData, EditableInventory editable) {
            super(playerData, editable);

            maxpages = (playerData.getGuild().countMembers() + 20) / editable.getByFunction("member").getSlots().size();
        }

        @Override
        public void open() {
            members = playerData.getGuild().listMembers();
            super.open();
        }

        @Override
        public String applyNamePlaceholders(String s) {
            return s.replace("{online_players}", "" + getPlayerData().getGuild().countOnlineMembers())
                    .replace("{page}", "" + page).replace("{maxpages}", "" + maxpages)
                    .replace("{players}", String.valueOf(getPlayerData().getGuild().countMembers()))
                    .replace("{tag}", getPlayerData().getGuild().getTag())
                    .replace("{name}", getPlayerData().getGuild().getName());
        }

        @Override
        public void whenClicked(InventoryClickEvent event, InventoryItem item) {
            if (item.getFunction().equals("leave")) {
                playerData.getGuild().removeMember(playerData.getUniqueId());
                player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1, 1);
                player.closeInventory();
                return;
            }

            if (item.getFunction().equals("next") && page != maxpages) {
                page++;
                open();
                return;
            }

            if (item.getFunction().equals("previous") && page != 1) {
                page--;
                open();
                return;
            }

            if (item.getFunction().equals("disband")) {
                if (!playerData.getGuild().getOwner().equals(playerData.getUniqueId()))
                    return;
                MMOCore.plugin.dataProvider.getGuildManager().unregisterGuild(playerData.getGuild());
                player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1, 1);
                player.closeInventory();
                return;
            }

            if (item.getFunction().equals("invite")) {
                if (!playerData.getGuild().getOwner().equals(playerData.getUniqueId()))
                    return;

                /*
                 * if (playerData.getGuild().getMembers().count() >= max) {
                 * MMOCore.plugin.configManager.getSimpleMessage("guild-is-full").send(player);
                 * player.playSound(player.getLocation(),
                 * Sound.ENTITY_VILLAGER_NO, 1, 1); return; }
                 */

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
                        MMOCore.plugin.configManager.getSimpleMessage("guild-invite-cooldown", "player", target.getName(), "cooldown", new DelayFormat().format(remaining)).send(player);
                        open();
                        return;
                    }

                    PlayerData targetData = PlayerData.get(target);
                    if (playerData.getGuild().hasMember(targetData.getUniqueId())) {
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

                String tag = event.getCurrentItem().getItemMeta().getPersistentDataContainer().get(UUID_NAMESPACEDKEY, PersistentDataType.STRING);
                if (tag == null || tag.isEmpty())
                    return;

                OfflinePlayer target = Bukkit.getOfflinePlayer(UUID.fromString(tag));
                if (target.equals(player))
                    return;

                playerData.getGuild().removeMember(target.getUniqueId());
                MMOCore.plugin.configManager.getSimpleMessage("kick-from-guild", "player", target.getName()).send(player);
                player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1, 1);
            }
        }

        public int getPage() {
            return page;
        }


    }
}
