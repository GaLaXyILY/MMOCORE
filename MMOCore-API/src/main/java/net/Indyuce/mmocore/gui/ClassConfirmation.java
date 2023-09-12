package net.Indyuce.mmocore.gui;

import io.lumine.mythic.lib.UtilityMethods;
import io.lumine.mythic.lib.gui.framework.EditableInventory;
import io.lumine.mythic.lib.gui.framework.GeneratedInventory;
import io.lumine.mythic.lib.gui.framework.PluginInventory;
import io.lumine.mythic.lib.gui.framework.item.InventoryItem;
import io.lumine.mythic.lib.gui.framework.item.Placeholders;
import io.lumine.mythic.lib.gui.framework.item.SimpleItem;
import net.Indyuce.mmocore.MMOCore;
import net.Indyuce.mmocore.api.SoundEvent;
import net.Indyuce.mmocore.api.event.PlayerChangeClassEvent;
import net.Indyuce.mmocore.api.player.PlayerData;
import net.Indyuce.mmocore.api.player.profess.PlayerClass;
import net.Indyuce.mmocore.api.player.profess.SavedClassInformation;
import net.Indyuce.mmocore.player.ClassDataContainer;
import org.apache.commons.lang.Validate;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.ItemStack;

import javax.annotation.Nullable;

public class ClassConfirmation extends EditableInventory<PlayerData> {
    private final PlayerClass playerClass;

    public ClassConfirmation(PlayerClass playerClass, boolean isDefault) {
        super("class-confirm-" + (isDefault ? "default" : UtilityMethods.ymlName(playerClass.getId())));

        this.playerClass = playerClass;
    }

    @Override
    public GeneratedInventory generate(PlayerData playerData, @Nullable GeneratedInventory generatedInventory) {
        return null;
    }

    @Override
    public InventoryItem loadItem(String function, ConfigurationSection config) {
        return function.equalsIgnoreCase("yes") ? new YesItem(config) : new SimpleItem(config);
    }

    public GeneratedInventory newInventory(PlayerData data, PluginInventory last, boolean setClass) {
        return newInventory(data, last, setClass, null);
    }

    public GeneratedInventory newInventory(PlayerData data, PluginInventory last, boolean setClass, @Nullable Runnable profileRunnable) {
        return new ClassConfirmationInventory(data, this, playerClass, last, setClass, profileRunnable);
    }

    public class UnlockedItem extends InventoryItem<ClassConfirmationInventory> {
        public UnlockedItem(ConfigurationSection config) {
            super(config);
        }

        @Override
        public Placeholders getPlaceholders(ClassConfirmationInventory inv, int n) {
            PlayerClass profess = inv.profess;
            ClassDataContainer info = inv.subclass ? inv.getPlayerData() : inv.getPlayerData().getClassInfo(profess);
            Placeholders holders = new Placeholders();

            final double nextLevelExp = inv.getPlayerData().getLevelUpExperience();
            final double ratio = info.getExperience() / nextLevelExp;

            StringBuilder bar = new StringBuilder("" + ChatColor.BOLD);
            int chars = (int) (ratio * 20);
            for (int j = 0; j < 20; j++)
                bar.append(j == chars ? "" + ChatColor.WHITE + ChatColor.BOLD : "").append("|");

            holders.register("percent", decimal.format(ratio * 100));
            holders.register("progress", bar.toString());
            holders.register("class", profess.getName());
            holders.register("unlocked_skills", info.mapSkillLevels().size());
            holders.register("class_skills", profess.getSkills().size());
            holders.register("next_level", "" + nextLevelExp);
            holders.register("level", info.getLevel());
            holders.register("exp", info.getExperience());
            holders.register("skill_points", info.getSkillPoints());

            return holders;
        }
    }

    public class YesItem extends SimpleItem<ClassConfirmationInventory> {
        private final InventoryItem unlocked, locked;

        public YesItem(ConfigurationSection config) {
            super(Material.BARRIER, config);

            Validate.isTrue(config.contains("unlocked"), "Could not load 'unlocked' config");
            Validate.isTrue(config.contains("locked"), "Could not load 'locked' config");

            unlocked = new UnlockedItem(config.getConfigurationSection("unlocked"));
            locked = new InventoryItem<ClassConfirmationInventory>(config.getConfigurationSection("locked")) {

                @Override
                public Placeholders getPlaceholders(ClassConfirmationInventory inv, int n) {
                    Placeholders holders = new Placeholders();
                    holders.register("class", inv.profess.getName());
                    return holders;
                }
            };
        }

        @Override
        public ItemStack getDisplayedItem(ClassConfirmationInventory inv, int n) {
            return inv.getPlayerData().hasSavedClass(inv.profess) ? unlocked.getDisplayedItem(inv, n) : locked.getDisplayedItem(inv, n);
        }
    }

    public class ClassConfirmationInventory extends GeneratedInventory<PlayerData> {
        private final PlayerClass profess;
        private final PluginInventory last;
        private final boolean subclass;

        @Nullable
        private final Runnable profileRunnable;

        private boolean canClose;

        public ClassConfirmationInventory(PlayerData playerData, EditableInventory editable, PlayerClass profess, PluginInventory last, boolean subclass, @Nullable Runnable profileRunnable) {
            super(playerData, editable);

            this.profess = profess;
            this.last = last;
            this.subclass = subclass;
            this.profileRunnable = profileRunnable;
        }

        @Override
        public void whenClicked(InventoryClickEvent event, InventoryItem item) {
            if (item.getFunction().equals("back")) {
                canClose = true;
                last.open();
            } else if (item.getFunction().equals("yes")) {

                PlayerChangeClassEvent called = new PlayerChangeClassEvent(playerData, profess);
                Bukkit.getPluginManager().callEvent(called);
                if (called.isCancelled())
                    return;

                canClose = true;
                playerData.giveClassPoints(-1);
                if (subclass)
                    playerData.setClass(profess);
                else
                    (playerData.hasSavedClass(profess) ? playerData.getClassInfo(profess)
                            : new SavedClassInformation(MMOCore.plugin.dataProvider.getDataManager().getDefaultData())).load(profess, playerData);
                MMOCore.plugin.configManager.getSimpleMessage("class-select", "class", profess.getName()).send(player);
                MMOCore.plugin.soundManager.getSound(SoundEvent.SELECT_CLASS).playTo(player);
                player.closeInventory();
                if (profileRunnable != null) profileRunnable.run();
            }
        }

        @Override
        public void open() {
            canClose = false;
            super.open();
        }

        @Override
        public void whenClosed(InventoryCloseEvent event) {
            if (profileRunnable != null && !canClose)
                Bukkit.getScheduler().runTaskLater(MMOCore.plugin, () -> open(), 2 * 20);
        }

        @Override
        public String calculateName() {
            return getName().replace("{class}", profess.getName());
        }
    }
}
