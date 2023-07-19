package net.Indyuce.mmocore.manager;

import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import net.Indyuce.mmocore.MMOCore;
import net.Indyuce.mmocore.api.player.PlayerData;
import org.bukkit.entity.Player;
import org.bukkit.plugin.messaging.PluginMessageListener;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class PluginMessageManager implements MMOCoreManager, PluginMessageListener {
    private String serverName;
    private List<String> allServerNames;
    public final String BUNGEECORD_CHANNEL = "BungeeCord";

    @Override
    public void initialize(boolean clearBefore) {
        if (!clearBefore) {
            //Setup the channels.
            MMOCore.plugin.getServer().getMessenger().registerOutgoingPluginChannel(MMOCore.plugin, BUNGEECORD_CHANNEL);
            MMOCore.plugin.getServer().getMessenger().registerIncomingPluginChannel(MMOCore.plugin, BUNGEECORD_CHANNEL, this);

        }
    }

    public void teleportToOtherServer(PlayerData playerData, String server) {
        ByteArrayDataOutput out = ByteStreams.newDataOutput();
        out.writeUTF("Connect");
        out.writeUTF(server);
        playerData.getPlayer().sendPluginMessage(MMOCore.plugin, BUNGEECORD_CHANNEL, out.toByteArray());
    }


    @Override
    public void onPluginMessageReceived(@NotNull String channel, @NotNull Player player, @NotNull byte[] bytes) {
        if (!channel.equals(BUNGEECORD_CHANNEL)) return;
        ByteArrayDataInput in = ByteStreams.newDataInput(bytes);
        String subchannel = in.readUTF();
        if (subchannel.equals("GetServer")) {
            serverName = in.readUTF();
        } else if (subchannel.equals("GetServers")) {
            allServerNames = Arrays.asList(in.readUTF().split(", "));
        }
    }

    public String getServerName() {
        return serverName;
    }

    public List<String> getAllServerNames() {
        return new ArrayList(allServerNames);
    }
}
