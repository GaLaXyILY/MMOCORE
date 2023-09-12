package net.Indyuce.mmocore.gui.social.party;

import io.lumine.mythic.lib.gui.framework.EditableInventory;
import io.lumine.mythic.lib.gui.framework.GeneratedInventory;
import io.lumine.mythic.lib.gui.framework.item.InventoryItem;
import io.lumine.mythic.lib.gui.framework.item.Placeholders;
import io.lumine.mythic.lib.gui.framework.item.SimpleItem;
import net.Indyuce.mmocore.MMOCore;
import net.Indyuce.mmocore.api.player.PlayerData;
import net.Indyuce.mmocore.api.util.math.format.DelayFormat;
import net.Indyuce.mmocore.party.provided.Party;
import net.Indyuce.mmocore.api.util.input.ChatInput;
import net.Indyuce.mmocore.api.util.input.PlayerInput;
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
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

public class EditablePartyView extends EditableInventory<PlayerData> {
    private static final NamespacedKey UUID_NAMESPACEDKEY = new NamespacedKey(MMOCore.plugin, "Uuid");

    public EditablePartyView() {
        super("party-view");
    }


    @Override
    public InventoryItem loadItem(String function, ConfigurationSection config) {
        return function.equals("member") ? new MemberItem(config) : new SimpleItem(config);
    }

    @Override
    public GeneratedInventory generate(PlayerData playerData, @Nullable GeneratedInventory generatedInventory) {
        return new PartyViewInventory(playerData, this);

    }


    public static class MemberDisplayItem extends InventoryItem<PartyViewInventory> {
        public MemberDisplayItem(MemberItem memberItem, ConfigurationSection config) {
            super(memberItem, config);
        }

        @Override
        public boolean hasDifferentDisplay() {
            return true;
        }

        @Override
        public Placeholders getPlaceholders(PartyViewInventory inv, int n) {
            Party party = (Party) inv.getPlayerData().getParty();
            PlayerData member = party.getMembers().get(n);

            Placeholders holders = new Placeholders();
            if (member.isOnline())
                holders.register("name", member.getPlayer().getName());
            holders.register("class", member.getProfess().getName());
            holders.register("level", "" + member.getLevel());
            holders.register("since", new DelayFormat(2).format(System.currentTimeMillis() - member.getLastLogin()));
            return holders;
        }

        @NotNull
        @Override
        public Player getEffectivePlayer(PartyViewInventory inv, int n) {
            return ((Party) inv.getPlayerData().getParty()).getMembers().get(n).getPlayer();
        }

        @Override
        public ItemStack getDisplayedItem(PartyViewInventory inv, int n) {
            final Player member = getEffectivePlayer(inv, n);

            ItemStack disp = super.getDisplayedItem(inv, n);
            ItemMeta meta = disp.getItemMeta();
            meta.getPersistentDataContainer().set(UUID_NAMESPACEDKEY, PersistentDataType.STRING, member.getUniqueId().toString());

            if (meta instanceof SkullMeta)
                inv.dynamicallyUpdateItem(this, n, disp, current -> {
                    ((SkullMeta) meta).setOwningPlayer(member);
                    current.47
                setItemMeta(meta);
                });

            disp.setItemMeta(meta);
            return disp;
        }
    }

    public static class MemberItem extends SimpleItem<PartyViewInventory> {
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
        public ItemStack getDisplayedItem(PartyViewInventory inv, int n) {
            Party party = (Party) inv.getPlayerData().getParty();
            return party.getMembers().size() > n ? member.getDisplayedItem(inv, n) : empty.getDisplayedItem(inv, n);
        }

        @Override
        public boolean hasDifferentDisplay() {
            return true;
        }

        @Override
        public boolean isDisplayed(PartyViewInventory inv) {
            return true;
        }
    }


    public class PartyViewInventory extends GeneratedInventory<PlayerData> {

        public PartyViewInventory(PlayerData playerData, EditableInventory editable) {
            super(playerData, editable);
        }

        @Override
        public String applyNamePlaceholders(String s) {
            Party party = (Party) getPlayerData().getParty();
            return s.replace("{max}", "" + MMOCore.plugin.configManager.maxPartyPlayers)
                    .replace("{players}", "" + party.getMembers().size());

        }

        @Override
        public void whenClicked(InventoryClickEvent event, InventoryItem item) {
            Party party = (Party) playerData.getParty();

            if (item.getFunction().equals("leave")) {
                party.removeMember(playerData);
                player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1, 1);
                player.closeInventory();
                return;
            }

            if (item.getFunction().equals("invite")) {

                if (party.getMembers().size() >= MMOCore.plugin.configManager.maxPartyPlayers) {
                    MMOCore.plugin.configManager.getSimpleMessage("party-is-full").send(player);
                    player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1, 1);
                    return;
                }

                new ChatInput(player, PlayerInput.InputType.PARTY_INVITE, this, input -> {
                    Player target = Bukkit.getPlayer(input);
                    if (target == null) {
                        MMOCore.plugin.configManager.getSimpleMessage("not-online-player", "player", input).send(player);
                        player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1, 1);
                        open();
                        return;
                    }

                    long remaining = party.getLastInvite(target) + 60 * 2 * 1000 - System.currentTimeMillis();
                    if (remaining > 0) {
                        MMOCore.plugin.configManager.getSimpleMessage("party-invite-cooldown", "player", target.getName(), "cooldown", new DelayFormat().format(remaining)).send(player);
                        open();
                        return;
                    }

                    PlayerData targetData = PlayerData.get(target);
                    if (party.hasMember(target)) {
                        MMOCore.plugin.configManager.getSimpleMessage("already-in-party", "player", target.getName()).send(player);
                        player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1, 1);
                        open();
                        return;
                    }

                    int levelDifference = Math.abs(targetData.getLevel() - party.getLevel());
                    if (levelDifference > MMOCore.plugin.configManager.maxPartyLevelDifference) {
                        MMOCore.plugin.configManager.getSimpleMessage("high-level-difference", "player", target.getName(), "diff", String.valueOf(levelDifference)).send(player);
                        player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1, 1);
                        open();
                        return;
                    }

                    party.sendInvite(playerData, targetData);
                    MMOCore.plugin.configManager.getSimpleMessage("sent-party-invite", "player", target.getName()).send(player);
                    player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1, 1);
                    open();
                });
            }

            if (item.getFunction().equals("member") && event.getClick() == ClickType.RIGHT) {
                if (!party.getOwner().equals(playerData))
                    return;

                OfflinePlayer target = Bukkit.getOfflinePlayer(UUID.fromString(event.getCurrentItem().getItemMeta().getPersistentDataContainer().get(UUID_NAMESPACEDKEY, PersistentDataType.STRING)));
                if (target.equals(player))
                    return;

                party.removeMember(PlayerData.get(target));
                MMOCore.plugin.configManager.getSimpleMessage("kick-from-party", "player", target.getName()).send(player);
                player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1, 1);
            }
        }
    }
}
