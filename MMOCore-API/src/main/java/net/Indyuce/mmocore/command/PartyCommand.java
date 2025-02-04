package net.Indyuce.mmocore.command;

import net.Indyuce.mmocore.MMOCore;
import net.Indyuce.mmocore.api.player.PlayerData;
import net.Indyuce.mmocore.command.api.RegisteredCommand;
import net.Indyuce.mmocore.command.api.ToggleableCommand;
import net.Indyuce.mmocore.manager.InventoryManager;
import net.Indyuce.mmocore.api.ConfigMessage;
import net.Indyuce.mmocore.api.event.MMOCommandEvent;
import net.Indyuce.mmocore.api.player.social.Request;
import net.Indyuce.mmocore.api.util.math.format.DelayFormat;
import net.Indyuce.mmocore.party.provided.MMOCorePartyModule;
import net.Indyuce.mmocore.party.provided.Party;
import net.Indyuce.mmocore.party.provided.PartyInvite;
import org.apache.commons.lang.Validate;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.Sound;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

public class PartyCommand extends RegisteredCommand {
    public PartyCommand(ConfigurationSection config) {
        super(config, ToggleableCommand.PARTY);
    }

    @Override
    public boolean execute(CommandSender sender, String label, String[] args) {
        if (!sender.hasPermission("mmocore.party"))
            return false;
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "This command is for players only.");
            return true;
        }
        if(!(MMOCore.plugin.partyModule instanceof MMOCorePartyModule)){
            sender.sendMessage(ChatColor.RED+"You can't use MMOCore party system as you delegated the party system to another plugin.");
            return true;
        }

        PlayerData data = PlayerData.get((OfflinePlayer) sender);
        MMOCommandEvent event = new MMOCommandEvent(data, "party");
        Bukkit.getServer().getPluginManager().callEvent(event);
        if (event.isCancelled()) return true;

        if (args.length > 0) {

            if (args[0].equalsIgnoreCase("invite")) {
                String input = args[1];
                Player target = Bukkit.getPlayer(input);
                Player player = (Player) sender;
                PlayerData playerData = PlayerData.get(player);
                Party party = (Party) playerData.getParty();
                if(party == null) {
                    ((MMOCorePartyModule) MMOCore.plugin.partyModule).newRegisteredParty(playerData); // Create a new party if no party
                    party = (Party) playerData.getParty();
                }
                if (party.getMembers().size() >= MMOCore.plugin.configManager.maxPartyPlayers) {
                    ConfigMessage.fromKey("party-is-full").send(player);
                    player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1, 1);
                    return true;
                }

                if (target == null) {
                    ConfigMessage.fromKey("not-online-player", "player", input).send(player);
                    player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1, 1);
                    return true;
                }

                long remaining = party.getLastInvite(target) + 60 * 2 * 1000 - System.currentTimeMillis();
                if (remaining > 0) {
                    ConfigMessage.fromKey("party-invite-cooldown", "player", target.getName(), "cooldown", new DelayFormat().format(remaining)).send(player);
                    return true;
                }

                PlayerData targetData = PlayerData.get(target);
                if (party.hasMember(target)) {
                    ConfigMessage.fromKey("already-in-party", "player", target.getName()).send(player);
                    player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1, 1);
                    return true;
                }

                int levelDifference = Math.abs(targetData.getLevel() - party.getLevel());
                if (levelDifference > MMOCore.plugin.configManager.maxPartyLevelDifference) {
                    ConfigMessage.fromKey("high-level-difference", "player", target.getName(), "diff", String.valueOf(levelDifference)).send(player);
                    player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1, 1);
                    return true;
                }

                party.sendInvite(playerData, targetData);
                ConfigMessage.fromKey("sent-party-invite", "player", target.getName()).send(player);
                player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1, 1);
                return true;
            }            
            final @Nullable PartyInvite invite;
            if (args.length > 1)

                // Search by request ID
                try {
                    final Request req = MMOCore.plugin.requestManager.getRequest(UUID.fromString(args[1]));
                    Validate.isTrue(req instanceof PartyInvite && !req.isTimedOut());
                    invite = (PartyInvite) req;
                    Validate.isTrue(((MMOCorePartyModule) MMOCore.plugin.partyModule).isRegistered(invite.getParty()));
                } catch (Exception exception) {
                    return true;
                }

                // Search by target player
            else
                invite = MMOCore.plugin.requestManager.findRequest(data, PartyInvite.class);

            // No invite found with given identifier/target player
            if (invite == null)
                return true;

            if (args[0].equalsIgnoreCase("accept"))
                invite.accept();
            else if (args[0].equalsIgnoreCase("deny"))
                invite.deny();
            return true;
        }

        if (data.getParty() != null)
            InventoryManager.PARTY_VIEW.newInventory(data).open();
        else
            InventoryManager.PARTY_CREATION.newInventory(data).open();
        return true;
    }
}
