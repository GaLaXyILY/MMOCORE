package net.Indyuce.mmocore.gui.api.packets;


import io.lumine.mythic.lib.gui.framework.PluginInventory;
import net.Indyuce.mmocore.MMOCore;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.wrappers.WrappedChatComponent;

public class InventoryPacketListener {


    public InventoryPacketListener() {

        ProtocolLibrary.getProtocolManager()
                .addPacketListener(new PacketAdapter(MMOCore.plugin, PacketType.Play.Server.OPEN_WINDOW) {

                    @Override
                    public void onPacketSending(PacketEvent event) {
                        Player player = event.getPlayer();

                        // Check if the packet's title contains the specific keyword
                        WrappedChatComponent title = event.getPacket().getChatComponents().read(0);
                        if (title != null) {
                            Bukkit.broadcastMessage("IN");
                            Bukkit.broadcastMessage("is null" + (player.getOpenInventory().getTopInventory().getHolder() == null));
                            Bukkit.broadcastMessage("is generated" + (player.getOpenInventory().getTopInventory().getHolder() instanceof PluginInventory));
                            Bukkit.broadcastMessage("Window Id" + event.getPacket().getIntegers().read(0));
                            Bukkit.getScheduler().runTaskLater(MMOCore.plugin, () -> {
                                Bukkit.broadcastMessage("is generated" + (player.getOpenInventory().getTopInventory().getHolder() instanceof PluginInventory));
                            }, 1);

                            // The packet is related to the aquatic shop extension
                            // You can modify the packet as needed
                            event.getPacket().getChatComponents().write(0, WrappedChatComponent.fromText("Modified Title"));
                        }
                    }
                });


                /*addPacketListener(new PacketAdapter(this, PacketType.Play.Server.WINDOW_ITEMS) {

                    @Override
                    public void onPacketSending(PacketEvent event) {
                        Player player = event.getPlayer();

                        // Check if the player is in a specific state (e.g., searching)
                        if (!plugin.getSearchHandler().isSearch(player.getUniqueId(), player.getOpenInventory().getTopInventory())) {
                            // Player is not in the expected state, allow the packet to be sent as-is
                            return;
                        }

                        // Modify or cancel the packet as needed for the specific use case
                        event.setCancelled(true); // Cancelling the packet prevents it from being sent
                    }
                });
                /*
                    boolean existed = plugin.getShopSessionHandler().getSessions().containsKey(player.getUniqueId());
                    ShopSession session = plugin.getShopSessionHandler().getOrCreateSession(player);
                    if (existed) {
                        session.setInventory(holder.getInventory());
                    }
                    String category;
                    String[] strs = holder.getTitle().split("%aquaticshopextension%");
                    if (strs.length < 2) {
                        category = "Blocks";
                    } else {
                        category = strs[1];
                    }
                    StringBuilder title = new StringBuilder();

                    title.append("§f");
                    title.append("\uF000".repeat(106));
                    title.append("\uF042");
                    title.append("\uF041");
                    if (ShopExtensionPlugin.subCategories.contains(category)) {
                        title.append("\uF000".repeat(214)).append("\uF053");
                        category = session.getSelectedCategory();
                    } else {
                        session.setPreviousCategory(null);
                    }

                    session.setSelectedCategory(category);

                    plugin.updateInventory(player,invHolder.getInventory(),session);
                    PacketCDH.super.write(ctx, new ClientboundOpenScreenPacket(packet.getContainerId(),packet.getType(),Component.literal(title.toString())),promise);
                }
            } catch (Exception ignored) {
                PacketCDH.super.write(ctx,packetObj,promise);
            }
        } else if(pkt instanceof ClientboundContainerSetContentPacket packet) {
            if (!plugin.getSearchHandler().isSearch(player.getUniqueId(),player.getOpenInventory().getTopInventory())) {
                super.write(ctx,packetObj,promise);
                return;
            }
            return;
        } else {
            super.write(ctx,packetObj,promise);
        }*/


    }
}


