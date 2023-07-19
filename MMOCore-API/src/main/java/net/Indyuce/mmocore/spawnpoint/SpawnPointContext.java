package net.Indyuce.mmocore.spawnpoint;

import io.lumine.mythic.lib.gson.JsonObject;
import io.lumine.mythic.lib.util.Jsonable;
import net.Indyuce.mmocore.MMOCore;
import net.Indyuce.mmocore.api.player.PlayerData;
import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

public class SpawnPointContext implements Jsonable {
    private final String id;

    private final Optional<String> server;

    public SpawnPointContext(String id) {
        this.id = id;
        this.server = Optional.empty();
    }

    public SpawnPointContext(String id, Optional<String> server) {
        this.id = id;
        this.server = server;
    }

    public SpawnPointContext(JsonObject jsonObject) {
        this.id = jsonObject.get("id").getAsString();
        this.server = Optional.ofNullable(jsonObject.get("server")).map((jsonElement) -> jsonElement.getAsString());
    }

    public String getId() {
        return id;
    }

    public Optional<String> getServer() {
        return server;
    }


    public Location getLocation() {
        return MMOCore.plugin.spawnPointManager.getSpawnPoint(id).getLocation();
    }

    public boolean isOtherServer() {
        return !server.isEmpty() && server.get() != MMOCore.plugin.pluginMessageManager.getServerName();
    }

    public void whenRespawn(PlayerData playerData) {
        if (isOtherServer()) {
            MMOCore.plugin.pluginMessageManager.teleportToOtherServer(playerData, server.get());
        } else {
            playerData.setLastUsedSpawnPointContext(this);
            MMOCore.plugin.spawnPointManager.getSpawnPoint(id).whenRespawn(playerData);
        }
    }

    public void save(ConfigurationSection section) {
        section.set("id", id);
        server.ifPresent((server) -> section.set("server", server));
    }

    @Override
    public @NotNull JsonObject toJson() {
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("id", id);
        server.ifPresent((server) -> jsonObject.addProperty("server", server));
        return jsonObject;
    }
}
