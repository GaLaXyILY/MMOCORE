package net.Indyuce.mmocore.listener;

import net.Indyuce.mmocore.MMOCore;
import net.Indyuce.mmocore.api.player.PlayerData;
import net.Indyuce.mmocore.spawnpoint.SpawnPoint;
import net.Indyuce.mmocore.spawnpoint.SpawnPointContext;
import net.Indyuce.mmocore.spawnpoint.def.DefaultSpawnOption;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.event.player.PlayerTeleportEvent;

import java.util.List;
import java.util.Optional;

public class SpawnPointsListener implements Listener {

    @EventHandler
    public void onTeleport(PlayerTeleportEvent event) {
        if (!event.getFrom().getWorld().equals(event.getTo().getWorld())) {
            PlayerData playerData = PlayerData.get(event.getPlayer());
            getLastSpawnPointContext(playerData).ifPresent(playerData::setLastSpawnPointContext);
        }
    }

    @EventHandler
    public void onDeath(PlayerDeathEvent event) {
        PlayerData playerData = PlayerData.get(event.getEntity());
        //TODO: Only when module enabled
        Optional<SpawnPointContext> context = getLastSpawnPointContext(playerData);
        if (context.isPresent()) {
            SpawnPointContext spawnPointContext = context.get();
            playerData.setLastSpawnPointContext(spawnPointContext);
        }
    }

    @EventHandler
    public void onSpawn(PlayerRespawnEvent event) {
        PlayerData playerData = PlayerData.get(event.getPlayer());
        Optional<SpawnPointContext> context = getLastSpawnPointContext(playerData);
        if (context.isPresent()) {
            SpawnPointContext spawnPointContext = context.get();
            if (!spawnPointContext.isOtherServer()){
                event.setRespawnLocation(spawnPointContext.getLocation());
            }

            spawnPointContext.whenRespawn(playerData);
        }
    }

    public Optional<SpawnPointContext> getLastSpawnPointContext(PlayerData playerData) {
        World world = playerData.getPlayer().getWorld();
        List<SpawnPoint> reachableSpawnPoints = MMOCore.plugin.spawnPointManager.
                getAll()
                .stream()
                .filter(spawnPoint -> spawnPoint.getLocation().getWorld().equals(world) && playerData.hasUnlocked(spawnPoint))
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
            return Optional.of(new SpawnPointContext(closestSpawnPoint.getId()));
        } else
            for (DefaultSpawnOption defaultSpawnOption : MMOCore.plugin.spawnPointManager.getDefaultSpawnOptions())
                if (defaultSpawnOption.matches(world)) {
                    return Optional.of(defaultSpawnOption.getSpawnPointContext(playerData));
                }
        return Optional.empty();
    }
}
