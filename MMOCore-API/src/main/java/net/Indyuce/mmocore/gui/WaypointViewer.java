package net.Indyuce.mmocore.gui;

import io.lumine.mythic.lib.gui.framework.EditableInventory;
import io.lumine.mythic.lib.gui.framework.GeneratedInventory;
import io.lumine.mythic.lib.gui.framework.item.InventoryItem;
import io.lumine.mythic.lib.gui.framework.item.Placeholders;
import io.lumine.mythic.lib.gui.framework.item.SimpleItem;
import net.Indyuce.mmocore.MMOCore;
import net.Indyuce.mmocore.api.player.PlayerActivity;
import net.Indyuce.mmocore.api.player.PlayerData;
import net.Indyuce.mmocore.waypoint.Waypoint;
import net.Indyuce.mmocore.waypoint.WaypointPath;
import org.apache.commons.lang.Validate;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class WaypointViewer extends EditableInventory<PlayerData> {
    public WaypointViewer() {
        super("waypoints");
    }

    @Override
    public InventoryItem loadItem(String function, ConfigurationSection config) {

        if (function.equals("waypoint"))
            return new WaypointItem(config);

        if (function.equals("previous"))
            return new SimpleItem<WaypointViewerInventory>(config) {

                @Override
                public boolean isDisplayed(WaypointViewerInventory inv) {
                    return inv.page > 0;
                }
            };

        if (function.equals("next"))
            return new SimpleItem<WaypointViewerInventory>(config) {

                @Override
                public boolean isDisplayed(WaypointViewerInventory inv) {
                    return inv.getEditable().getByFunction("waypoint").getSlots().size() * (inv.page + 1) < inv.waypoints.size();
                }
            };

        return new SimpleItem(config);
    }

    @Override
    public GeneratedInventory generate(PlayerData data, @Nullable GeneratedInventory generatedInventory) {
        return generate(data, null, null);
    }

    public GeneratedInventory generate(PlayerData data, Waypoint waypoint, @Nullable GeneratedInventory prev) {
        return new WaypointViewerInventory(data, this, waypoint, prev);
    }

    public class WaypointItem extends SimpleItem<WaypointViewerInventory> {
        private final SimpleItem noWaypoint, locked;
        private final WaypointItemHandler availWaypoint, noStellium, notLinked, notDynamic, currentWayPoint;

        public WaypointItem(ConfigurationSection config) {
            super(config, Material.BARRIER);

            Validate.notNull(config.getConfigurationSection("no-waypoint"), "Could not load 'no-waypoint' config");
            Validate.notNull(config.getConfigurationSection("locked"), "Could not load 'locked' config");
            Validate.notNull(config.getConfigurationSection("not-a-destination"), "Could not load 'not-a-destination' config");
            Validate.notNull(config.getConfigurationSection("not-dynamic"), "Could not load 'not-dynamic' config");
            Validate.notNull(config.getConfigurationSection("current-waypoint"), "Could not load 'current-waypoint' config");
            Validate.notNull(config.getConfigurationSection("not-enough-stellium"), "Could not load 'not-enough-stellium' config");
            Validate.notNull(config.getConfigurationSection("display"), "Could not load 'display' config");

            noWaypoint = new SimpleItem(config.getConfigurationSection("no-waypoint"));
            locked = new SimpleItem(config.getConfigurationSection("locked"));
            notLinked = new WaypointItemHandler(config.getConfigurationSection("not-a-destination"), true);
            notDynamic = new WaypointItemHandler(config.getConfigurationSection("not-dynamic"), true);
            currentWayPoint = new WaypointItemHandler(config.getConfigurationSection("current-waypoint"), true);
            noStellium = new WaypointItemHandler(config.getConfigurationSection("not-enough-stellium"), false);
            availWaypoint = new WaypointItemHandler(config.getConfigurationSection("display"), false);
        }

        @Override
        public boolean hasDifferentDisplay() {
            return true;
        }

        @Override
        public ItemStack getDisplayedItem(WaypointViewerInventory inv, int n) {

            int index = inv.page * inv.getEditable().getByFunction("waypoint").getSlots().size() + n;
            if (index >= inv.waypoints.size())
                return noWaypoint.getDisplayedItem(inv, n);

            // Locked waypoint?
            Waypoint waypoint = inv.waypoints.get(index);
            if (inv.current != null && inv.current.equals(waypoint))
                return currentWayPoint.getDisplayedItem(inv, n);

            if (!inv.getPlayerData().hasWaypoint(waypoint))
                return locked.getDisplayedItem(inv, n);

            // Waypoints are not linked
            if (inv.current != null && !inv.paths.containsKey(waypoint))
                return notLinked.getDisplayedItem(inv, n);

            // Not dynamic waypoint
            if (inv.current == null && !inv.paths.containsKey(waypoint))
                return notDynamic.getDisplayedItem(inv, n);

            //Normal cost
            if (inv.paths.get(waypoint).getCost() > inv.getPlayerData().getStellium())
                return noStellium.getDisplayedItem(inv, n);

            return availWaypoint.getDisplayedItem(inv, n);
        }
    }

    public class WaypointItemHandler extends InventoryItem<WaypointViewerInventory> {
        private final boolean onlyName;

        public WaypointItemHandler(ConfigurationSection config, boolean onlyName) {
            super(config);
            this.onlyName = onlyName;
        }

        @Override
        public ItemStack getDisplayedItem(WaypointViewerInventory inv, int n) {
            ItemStack disp = super.getDisplayedItem(inv, n);

            // If a player can teleport to another waypoint given his location
            Waypoint waypoint = inv.waypoints.get(inv.page * inv.getEditable().getByFunction("waypoint").getSlots().size() + n);
            ItemMeta meta = disp.getItemMeta();
            List<String> lore = new ArrayList<>();
            meta.getLore().forEach(string -> {
                if (string.contains("{lore}"))
                    lore.addAll(waypoint.getLore());
                else
                    lore.add(string);
            });
            meta.setLore(lore);
            PersistentDataContainer container = meta.getPersistentDataContainer();
            container.set(new NamespacedKey(MMOCore.plugin, "waypointId"), PersistentDataType.STRING, waypoint.getId());
            disp.setItemMeta(meta);
            return disp;
        }

        @Override
        public Placeholders getPlaceholders(WaypointViewerInventory inv, int n) {
            Placeholders holders = new Placeholders();

            Waypoint waypoint = inv.waypoints.get(inv.page * inv.getByFunction("waypoint").getSlots().size() + n);
            holders.register("name", waypoint.getName());

            if (!onlyName) {
                holders.register("current_cost", inv.paths.get(waypoint).getCost());
                holders.register("normal_cost", decimal.format(inv.paths.containsKey(waypoint) ? inv.paths.get(waypoint).getCost() : Double.POSITIVE_INFINITY));
                holders.register("dynamic_cost", decimal.format(waypoint.getDynamicCost()));
                holders.register("intermediary_waypoints", inv.paths.containsKey(waypoint) ? inv.paths.get(waypoint).displayIntermediaryWayPoints(inv.isDynamicUse()) : "None");
            }

            return holders;
        }
    }

    public class WaypointViewerInventory extends GeneratedInventory<PlayerData> {
        private final List<Waypoint> waypoints = new ArrayList<>(MMOCore.plugin.waypointManager.getAll());
        @Nullable
        private final Waypoint current;
        private final Map<Waypoint, WaypointPath> paths = new HashMap<>();

        private int page;

        public WaypointViewerInventory(PlayerData playerData, EditableInventory editable, Waypoint current, @Nullable GeneratedInventory prev) {
            super(playerData, editable, prev);

            this.current = current;
            if (current != null)
                for (WaypointPath pathInfo : current.getAllPath())
                    paths.put(pathInfo.getFinalWaypoint(), pathInfo);

            if (current == null) {

                //Iterate through all the dynamic points and find all the points it is linked to and the path
                HashMap<Waypoint, Double> dynamicPoints = new HashMap<>();
                //We first check all the dynamic waypoints
                for (Waypoint waypoint : waypoints) {
                    if (waypoint.mayBeUsedDynamically(playerData.getPlayer())) {
                        paths.put(waypoint, new WaypointPath(waypoint, waypoint.getDynamicCost()));
                        dynamicPoints.put(waypoint, waypoint.getDynamicCost());
                    }
                }
                for (Waypoint source : dynamicPoints.keySet()) {
                    for (WaypointPath target : source.getAllPath()) {
                        if (!paths.containsKey(target.getFinalWaypoint()) || paths.get(target.getFinalWaypoint()).getCost() > target.getCost() + dynamicPoints.get(source)) {
                            paths.put(target.getFinalWaypoint(), target.addCost(dynamicPoints.get(source)));
                        }
                    }
                }
            }
        }

        @Override
        public String applyNamePlaceholders(String s) {
            return s;
        }


        public boolean isDynamicUse() {
            return current == null;
        }

        @Override
        public void whenClicked(InventoryClickEvent event, InventoryItem item) {
            if (item.getFunction().equals("next")) {
                page++;
                open();
                return;
            }

            if (item.getFunction().equals("previous")) {
                page--;
                open();
                return;
            }

            if (item.getFunction().equals("waypoint")) {
                PersistentDataContainer container = event.getCurrentItem().getItemMeta().getPersistentDataContainer();
                String tag = container.has(new NamespacedKey(MMOCore.plugin, "waypointId"), PersistentDataType.STRING) ?
                        container.get(new NamespacedKey(MMOCore.plugin, "waypointId"), PersistentDataType.STRING) : "";

                if (tag.equals(""))
                    return;

                // Locked waypoint?
                Waypoint waypoint = MMOCore.plugin.waypointManager.get(tag);
                if (!playerData.hasWaypoint(waypoint)) {
                    MMOCore.plugin.configManager.getSimpleMessage("not-unlocked-waypoint").send(player);
                    return;
                }

                // Cannot teleport to current waypoint
                if (waypoint.equals(current)) {
                    MMOCore.plugin.configManager.getSimpleMessage("standing-on-waypoint").send(player);
                    return;
                }

                // Waypoint does not have target as destination
                if (current != null && current.getPath(waypoint) == null) {
                    MMOCore.plugin.configManager.getSimpleMessage("cannot-teleport-to").send(player);
                    return;
                }

                // Not dynamic waypoint
                if (current == null && !paths.containsKey(waypoint)) {
                    MMOCore.plugin.configManager.getSimpleMessage("not-dynamic-waypoint").send(player);
                    return;
                }

                // Stellium cost
                double withdraw = paths.get(waypoint).getCost();
                double left = withdraw - playerData.getStellium();
                if (left > 0) {
                    MMOCore.plugin.configManager.getSimpleMessage("not-enough-stellium", "more", decimal.format(left)).send(player);
                    return;
                }

                if (playerData.getActivityTimeOut(PlayerActivity.USE_WAYPOINT) > 0)
                    return;

                player.closeInventory();
                playerData.warp(waypoint, withdraw);

            }
        }
    }
}
