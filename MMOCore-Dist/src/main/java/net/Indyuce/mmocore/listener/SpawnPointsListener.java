package net.Indyuce.mmocore.listener;

import net.Indyuce.mmocore.MMOCore;
import net.Indyuce.mmocore.api.player.PlayerData;
import net.Indyuce.mmocore.spawnpoint.SpawnPoint;
import net.Indyuce.mmocore.spawnpoint.def.DefaultSpawnOption;
import org.bukkit.World;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerTeleportEvent;

import java.util.List;

public class SpawnPointsListener implements Listener {

    @EventHandler
    public void onTeleport(PlayerTeleportEvent event) {
        event.getFrom()
    }

    @EventHandler
    public void onDeath(PlayerDeathEvent event) {
        //TODO Only when module enabled
        PlayerData playerData = PlayerData.get(event.getEntity());
        World world = playerData.getPlayer().getWorld();
        List<SpawnPoint> reachableSpawnPoints = MMOCore.plugin.spawnPointManager.
                getSpawnPoints(world)
                .stream()
                .filter(spawnPoint -> playerData.hasUnlocked(spawnPoint))
                .toList();
        if (!reachableSpawnPoints.isEmpty()) {
            double minDistance = Double.MAX_VALUE;
            SpawnPoint closestSpawnPoint = null;
            for (SpawnPoint spawnPoint : reachableSpawnPoints) {
                double distance = spawnPoint.getLocation().distance(playerData.getPlayer().getLocation());
                distance = distance / spawnPoint.getStrength();
                if (distance < minDistance) {
                    minDistance = distance;
                    closestSpawnPoint = spawnPoint;
                }
            }
            playerData.getPlayer().teleport(closestSpawnPoint.getLocation());
            closestSpawnPoint.whenRespawn(playerData);
        } else
            for (DefaultSpawnOption defaultSpawnOption : MMOCore.plugin.spawnPointManager.getDefaultSpawnOptions())
                if (defaultSpawnOption.matches(world)) {
                    playerData.getPlayer().teleport(defaultSpawnOption.getSpawnLocation(playerData));
                    defaultSpawnOption.whenRespawn();
                    return;
                }
    }
}
