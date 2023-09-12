package net.Indyuce.mmocore.gui.api.packets;
/*
import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.events.PacketContainer;
import net.Indyuce.mmocore.api.player.PlayerData;
import net.Indyuce.mmocore.gui.api.EditableInventory;
import net.Indyuce.mmocore.gui.api.GeneratedInventory;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;

public abstract class FullGeneratedInventory extends GeneratedInventory {

    public FullGeneratedInventory(PlayerData playerData, EditableInventory editable) {
        super(playerData, editable);
    }

    @Override
    public void open() {
        super.open();
        resetPlayerInventory();
    }

    private void resetPlayerInventory() {
        // Create an empty inventory with 36 empty slots
        ItemStack[] emptyInventory = new ItemStack[45];
        for (int i = 9; i < emptyInventory.length; i++) {
            emptyInventory[i] = new ItemStack(Material.AIR);
        }

        // Create a WINDOW_ITEMS packet to update the player's inventory
        PacketContainer packet = new PacketContainer(PacketType.Play.Server.WINDOW_ITEMS);
        packet.getIntegers().write(0, 0); // Window ID, 0 for player inventory
        packet.getItemListModifier().write(0, Arrays.asList(emptyInventory));

        // Send the packet to the player
        try {
            ProtocolLibrary.getProtocolManager().sendServerPacket(player, packet);
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }
    }
}*/
