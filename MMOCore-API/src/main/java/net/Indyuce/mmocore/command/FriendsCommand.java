package net.Indyuce.mmocore.command;

import net.Indyuce.mmocore.MMOCore;
import net.Indyuce.mmocore.api.player.PlayerData;
import net.Indyuce.mmocore.api.player.social.FriendRequest;
import net.Indyuce.mmocore.command.api.RegisteredCommand;
import net.Indyuce.mmocore.command.api.ToggleableCommand;
import net.Indyuce.mmocore.manager.InventoryManager;
import net.Indyuce.mmocore.api.ConfigMessage;
import net.Indyuce.mmocore.api.event.MMOCommandEvent;
import net.Indyuce.mmocore.api.player.social.Request;
import org.apache.commons.lang.Validate;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Sound;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

public class FriendsCommand extends RegisteredCommand {
    public FriendsCommand(ConfigurationSection config) {
        super(config, ToggleableCommand.FRIENDS);
    }

    @Override
    public boolean execute(CommandSender sender, String label, String[] args) {
        if (!sender.hasPermission("mmocore.friends"))
            return false;
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "This command is for players only.");
            return true;
        }

        PlayerData data = PlayerData.get((Player) sender);
        MMOCommandEvent event = new MMOCommandEvent(data, "friends");
        Bukkit.getServer().getPluginManager().callEvent(event);
        if (event.isCancelled()) return true;

        if (args.length > 1) {

            // Player wants to invite someone
            if (args[0].equalsIgnoreCase("invite")) {
                    String input = args[1];
                    Player target = Bukkit.getPlayer(input);
                    Player player = (Player) sender;
                    PlayerData playerData = PlayerData.get(player);
                    if (target == null) {
                        ConfigMessage.fromKey("not-online-player", "player", input).send(player);
                        player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1, 1);
                        return true;
                    }

                    if (playerData.hasFriend(target.getUniqueId())) {
                        ConfigMessage.fromKey("already-friends", "player", target.getName()).send(player);
                        player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1, 1);
                        return true;
                    }

                    if (playerData.getUniqueId().equals(target.getUniqueId())) {
                        ConfigMessage.fromKey("cant-request-to-yourself").send(player);
                        player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1, 1);
                        return true;
                    }

                    playerData.sendFriendRequest(PlayerData.get(target));
                    ConfigMessage.fromKey("sent-friend-request", "player", target.getName()).send(player);
                    player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1, 1);
                    return true;
            }

            final @Nullable FriendRequest invite;
            if (args.length > 1)

                // Search by request ID
                try {
                    final UUID uuid = UUID.fromString(args[1]);
                    final Request req = MMOCore.plugin.requestManager.getRequest(uuid);
                    Validate.isTrue(!req.isTimedOut() && req instanceof FriendRequest);
                    Validate.isTrue(!data.hasFriend(req.getCreator().getUniqueId()));
                    invite = (FriendRequest) req;
                } catch (Exception exception) {
                    return true;
                }

                // Search by target player
            else
                invite = MMOCore.plugin.requestManager.findRequest(data, FriendRequest.class);

            // No invite found with given identifier/target player
            if (invite == null)
                return true;

            if (args[0].equalsIgnoreCase("accept"))
                invite.accept();
            if (args[0].equalsIgnoreCase("deny"))
                invite.deny();
            return true;
        }

        InventoryManager.FRIEND_LIST.newInventory(data).open();
        return true;
    }
}
