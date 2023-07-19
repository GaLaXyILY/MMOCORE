package net.Indyuce.mmocore.command.rpg.admin;

import io.lumine.mythic.lib.command.api.CommandTreeNode;
import io.lumine.mythic.lib.command.api.Parameter;
import net.Indyuce.mmocore.MMOCore;
import net.Indyuce.mmocore.api.player.PlayerData;
import net.Indyuce.mmocore.skill.binding.SkillSlot;
import net.Indyuce.mmocore.command.api.CommandVerbose;
import net.Indyuce.mmocore.skill.ClassSkill;
import net.Indyuce.mmocore.spawnpoint.SpawnPoint;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;


public class SpawnPointCommandTreeNode extends CommandTreeNode {
    public SpawnPointCommandTreeNode(CommandTreeNode parent) {
        super(parent, "slot");
        addChild(new LockSpawnPointCommand(this, "lock", true));
        addChild(new LockSpawnPointCommand(this, "unlock", false));
    }

    public class LockSpawnPointCommand extends CommandTreeNode {
        private final boolean lock;

        public LockSpawnPointCommand(CommandTreeNode parent, String id, boolean lock) {
            super(parent, id);
            this.lock = lock;
            addParameter(Parameter.PLAYER);
            addParameter(new Parameter("spawnpoint",
                    (explorer, list) -> MMOCore.plugin.spawnPointManager.getAll().stream().map(spawnPoint -> spawnPoint.getId()).forEach(list::add)));

        }

        @Override
        public CommandResult execute(CommandSender sender, String[] args) {
            if (args.length < 5)
                return CommandResult.THROW_USAGE;
            Player player = Bukkit.getPlayer(args[3]);
            if (player == null) {
                sender.sendMessage(ChatColor.RED + "Could not find the player called " + args[3] + ".");
                return CommandResult.FAILURE;
            }
            PlayerData playerData = PlayerData.get(player);

            if (!MMOCore.plugin.spawnPointManager.isSpawnPoint(args[4])) {
                sender.sendMessage(ChatColor.RED + "Could not find the spawnpoint called " + args[4] + ".");
                return CommandResult.FAILURE;
            }
            SpawnPoint spawnPoint = MMOCore.plugin.spawnPointManager.getSpawnPoint(args[4]);
            if (lock) {
                if (!playerData.hasUnlocked(spawnPoint)) {
                    CommandVerbose.verbose(sender, CommandVerbose.CommandType.SKILL, ChatColor.RED + "The spawn point " +
                            spawnPoint.getId() + " is already locked for " + player.getName());
                    return CommandResult.SUCCESS;
                }
                playerData.lock(spawnPoint);

            } else {
                if (playerData.hasUnlocked(spawnPoint)) {
                    CommandVerbose.verbose(sender, CommandVerbose.CommandType.SKILL, ChatColor.RED + "The spawn point " +
                            spawnPoint.getId() + " is already unlocked for " + player.getName());
                    return CommandResult.SUCCESS;
                }
                playerData.unlock(spawnPoint);
            }
            CommandVerbose.verbose(sender, CommandVerbose.CommandType.SPAWN_POINT, ChatColor.GOLD + "The spawn point " +
                    spawnPoint.getId() + " is now " + (lock ? "locked" : "unlocked  for " + player.getName()));
            return CommandResult.SUCCESS;
        }
    }


    @Override
    public CommandResult execute(CommandSender sender, String[] args) {
        return CommandResult.THROW_USAGE;
    }
}


