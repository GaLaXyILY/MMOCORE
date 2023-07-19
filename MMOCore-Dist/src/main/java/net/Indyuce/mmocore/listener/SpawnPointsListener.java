package net.Indyuce.mmocore.listener;

import net.Indyuce.mmocore.MMOCore;
import net.Indyuce.mmocore.api.player.PlayerData;
import net.Indyuce.mmocore.spawnpoint.SpawnPoint;
import net.Indyuce.mmocore.spawnpoint.def.DefaultSpawnOption;
import org.bukkit.Bukkit;
import org.bukkit.Location;
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
            getLastSpawnPoint(event.getFrom(), playerData).ifPresent(playerData::setLastSpawnPoint);
        }
    }

    @EventHandler
    public void onDeath(PlayerDeathEvent event) {
        PlayerData playerData = PlayerData.get(event.getEntity());
        //TODO: Only when module enabled
        getLastSpawnPoint(playerData).ifPresent((spawnPoint) -> playerData.setLastSpawnPoint(spawnPoint));

    }

    @EventHandler
    public void onSpawn(PlayerRespawnEvent event) {
        PlayerData playerData = PlayerData.get(event.getPlayer());
        getLastSpawnPoint(playerData).ifPresent((spawnPoint) ->
        {
            if (!spawnPoint.isOtherServer())
                event.setRespawnLocation(spawnPoint.getLocation());

            spawnPoint.whenRespawn(playerData);
        });
    }

    public Optional<SpawnPoint> getLastSpawnPoint(Location location, PlayerData playerData) {
        World world = location.getWorld();
        List<SpawnPoint> reachableSpawnPoints = MMOCore.plugin.spawnPointManager.
                getAll()
                .stream()
                .filter(spawnPoint -> spawnPoint.getLocation().getWorld().equals(world) && playerData.hasUnlocked(spawnPoint))
                .toList();
        if (!reachableSpawnPoints.isEmpty()) {

            double minDistance = Double.MAX_VALUE;
            SpawnPoint closestSpawnPoint = null;
            for (SpawnPoint spawnPoint : reachableSpawnPoints) {
                double distance = spawnPoint.getLocation().distance(location);
                distance = distance / spawnPoint.getStrength();
                if (distance < minDistance) {
                    minDistance = distance;
                    closestSpawnPoint = spawnPoint;
                }
            }
            return Optional.of(closestSpawnPoint);
        } else
            for (DefaultSpawnOption defaultSpawnOption : MMOCore.plugin.spawnPointManager.getDefaultSpawnOptions()) {
                Bukkit.broadcastMessage("world: " + world.getName() + " matches: " + defaultSpawnOption.matches(world));
                if (defaultSpawnOption.matches(world)) {
                    return Optional.of(defaultSpawnOption.getSpawnPoint(playerData));
                }
            }

        return Optional.empty();
    }

    public Optional<SpawnPoint> getLastSpawnPoint(PlayerData playerData) {
        return getLastSpawnPoint(playerData.getPlayer().getLocation(), playerData);
    }
}
