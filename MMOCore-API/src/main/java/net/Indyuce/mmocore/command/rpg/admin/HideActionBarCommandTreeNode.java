package net.Indyuce.mmocore.command.rpg.admin;

import io.lumine.mythic.lib.command.api.CommandTreeNode;
import io.lumine.mythic.lib.command.api.Parameter;
import net.Indyuce.mmocore.api.player.PlayerData;
import net.Indyuce.mmocore.player.CooldownType;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class HideActionBarCommandTreeNode extends CommandTreeNode {
    public HideActionBarCommandTreeNode(CommandTreeNode parent) {
        super(parent, "hideab");

        addParameter(Parameter.PLAYER);
        addParameter(Parameter.AMOUNT);
    }

    @Override
    public CommandResult execute(CommandSender sender, String[] args) {
        if (args.length < 4)
            return CommandResult.THROW_USAGE;

        Player player = Bukkit.getPlayer(args[2]);
        if (player == null) {
            sender.sendMessage(ChatColor.RED + "Could not find the player called " + args[2] + ".");
            return CommandResult.FAILURE;
        }

        int amount;
        try {
            amount = Integer.parseInt(args[3]);
        } catch (Exception e) {
            sender.sendMessage(ChatColor.RED + args[3] + " is not a valid number.");
            return CommandResult.FAILURE;
        }

        PlayerData.get(player).getCooldownMap().applyCooldown(CooldownType.ACTION_BAR_MESSAGE, (double) amount / 20d);
        return CommandResult.SUCCESS;
    }
}
