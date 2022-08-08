package net.Indyuce.mmocore.gui;

import io.lumine.mythic.lib.MythicLib;
import io.lumine.mythic.lib.api.item.ItemTag;
import io.lumine.mythic.lib.api.item.NBTItem;
import net.Indyuce.mmocore.MMOCore;
import net.Indyuce.mmocore.api.SoundEvent;
import net.Indyuce.mmocore.gui.api.InventoryClickContext;
import net.Indyuce.mmocore.gui.api.item.InventoryItem;
import net.Indyuce.mmocore.gui.api.item.SimplePlaceholderItem;
import net.Indyuce.mmocore.api.player.PlayerData;
import net.Indyuce.mmocore.api.util.MMOCoreUtils;
import net.Indyuce.mmocore.gui.api.EditableInventory;
import net.Indyuce.mmocore.gui.api.GeneratedInventory;
import net.Indyuce.mmocore.gui.api.item.Placeholders;
import net.Indyuce.mmocore.skill.ClassSkill;
import net.Indyuce.mmocore.skill.RegisteredSkill;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

public class SkillList extends EditableInventory {
    public SkillList() {
        super("skill-list");
    }

    @Override
    public InventoryItem load(String function, ConfigurationSection config) {

        if (function.equals("skill"))
            return new SkillItem(config);

        if (function.equals("level"))
            return new LevelItem(config);

        if (function.equals("upgrade"))
            return new UpgradeItem(config);

        if (function.equals("reallocation")) {

            return new InventoryItem(config) {

                @Override
                public Placeholders getPlaceholders(GeneratedInventory inv, int n) {
                    Placeholders holders = new Placeholders();
                    holders.register("skill_points", inv.getPlayerData().getSkillPoints());
                    holders.register("points", inv.getPlayerData().getSkillReallocationPoints());
                    holders.register("total", inv.getPlayerData().countSkillPointsWhenReallocate());
                    return holders;
                }
            };
        }



        if (function.equals("slot"))
            return new InventoryItem<SkillViewerInventory>(config) {
                private final String none = MythicLib.plugin.parseColors(config.getString("no-skill"));
                private final Material emptyMaterial = Material
                        .valueOf(config.getString("empty-item").toUpperCase().replace("-", "_").replace(" ", "_"));
                private final int emptyCMD = config.getInt("empty-custom-model-data", getModelData());

                @Override
                public Placeholders getPlaceholders(SkillViewerInventory inv, int n) {
                    RegisteredSkill selected = inv.selected == null ? null : inv.selected.getSkill();
                    RegisteredSkill skill = inv.getPlayerData().hasSkillBound(n) ? inv.getPlayerData().getBoundSkill(n).getSkill() : null;

                    Placeholders holders = new Placeholders();

                    holders.register("skill", skill == null ? none : skill.getName());
                    holders.register("index", "" + (n + 1));
                    holders.register("slot", MMOCoreUtils.intToRoman(n + 1));
                    holders.register("selected", selected == null ? none : selected.getName());

                    return holders;
                }

                @Override
                public ItemStack display(SkillViewerInventory inv, int n) {
                    if (n >= inv.getPlayerData().getProfess().getMaxBoundSkills()) {
                        return new ItemStack(Material.AIR);
                    }
                    ItemStack item = super.display(inv, n);
                    if (!inv.getPlayerData().hasSkillBound(n)) {
                        item.setType(emptyMaterial);

                        if (MythicLib.plugin.getVersion().isStrictlyHigher(1, 13)) {
                            ItemMeta meta = item.getItemMeta();
                            meta.setCustomModelData(emptyCMD);
                            item.setItemMeta(meta);
                        }
                    }
                    return item;
                }

                @Override
                public boolean hasDifferentDisplay() {
                    return true;
                }
            };
        if (function.equals("previous"))
            return new SimplePlaceholderItem<SkillViewerInventory>(config) {

                @Override
                public boolean canDisplay(SkillViewerInventory inv) {
                    return inv.page > 0;
                }
            };
        if (function.equals("next")) {
            return new SimplePlaceholderItem<SkillViewerInventory>(config) {

                @Override
                public boolean canDisplay(SkillViewerInventory inv) {
                    final int perPage = inv.skillSlots.size();
                    return inv.page < (inv.skills.size() - 1) / perPage;
                }
            };
        }

        return new SimplePlaceholderItem(config);
    }

    public GeneratedInventory newInventory(PlayerData data) {
        return new SkillViewerInventory(data, this);
    }

    public class LevelItem extends InventoryItem<SkillViewerInventory> {
        private final int offset;

        public LevelItem(ConfigurationSection config) {
            super(config);

            offset = config.getInt("offset");
        }

        @Override
        public boolean hasDifferentDisplay() {
            return true;
        }

        @Override
        public ItemStack display(SkillViewerInventory inv, int n) {

            ClassSkill skill = inv.selected;
            int skillLevel = inv.getPlayerData().getSkillLevel(skill.getSkill()) + n - offset;
            if (skillLevel < 1)
                return new ItemStack(Material.AIR);

            List<String> lore = new ArrayList<>(getLore());
            int index = lore.indexOf("{lore}");
            lore.remove(index);
            List<String> skillLore = skill.calculateLore(inv.getPlayerData(), skillLevel);
            for (int j = 0; j < skillLore.size(); j++)
                lore.add(index + j, skillLore.get(j));

            for (int j = 0; j < lore.size(); j++)
                lore.set(j, ChatColor.GRAY + MythicLib.plugin.parseColors(lore.get(j)));

            ItemStack item = new ItemStack(getMaterial());
            ItemMeta meta = item.getItemMeta();
            meta.setDisplayName(MythicLib.plugin.parseColors(getName().replace("{skill}", skill.getSkill().getName())
                    .replace("{roman}", MMOCoreUtils.intToRoman(skillLevel)).replace("{level}", "" + skillLevel)));
            meta.addItemFlags(ItemFlag.values());
            meta.setLore(lore);
            if (MythicLib.plugin.getVersion().isStrictlyHigher(1, 13))
                meta.setCustomModelData(getModelData());
            item.setItemMeta(meta);

            return NBTItem.get(item).addTag(new ItemTag("skillId", skill.getSkill().getHandler().getId())).toItem();
        }

        @Override
        public Placeholders getPlaceholders(SkillViewerInventory inv, int n) {
            return new Placeholders();
        }
    }

    public class SkillItem extends InventoryItem<SkillViewerInventory> {
        public SkillItem(ConfigurationSection config) {
            super(Material.BARRIER, config);
        }

        @Override
        public boolean hasDifferentDisplay() {
            return true;
        }

        @Override
        public ItemStack display(SkillViewerInventory inv, int n) {

            // Calculate placeholders
            int index = n + inv.skillSlots.size() * inv.page;
            if (index >= inv.skills.size())
                return new ItemStack(Material.AIR);

            ClassSkill skill = inv.skills.get(index);
            Placeholders holders = getPlaceholders(inv.getPlayerData(), skill);

            List<String> lore = new ArrayList<>(getLore());

            index = lore.indexOf("{lore}");
            lore.remove(index);
            List<String> skillLore = skill.calculateLore(inv.getPlayerData());
            for (int j = 0; j < skillLore.size(); j++)
                lore.add(index + j, skillLore.get(j));

            boolean unlocked = skill.getUnlockLevel() <= inv.getPlayerData().getLevel();

            lore.removeIf(next -> (next.startsWith("{unlocked}") && !unlocked) || (next.startsWith("{locked}") && unlocked) || (next.startsWith("{max_level}") && (!skill.hasMaxLevel() || skill.getMaxLevel() > inv.getPlayerData().getSkillLevel(skill.getSkill()))));

            for (int j = 0; j < lore.size(); j++)
                lore.set(j, ChatColor.GRAY + holders.apply(inv.getPlayer(), lore.get(j)));

            // Generate item
            ItemStack item = skill.getSkill().getIcon();
            ItemMeta meta = item.getItemMeta();
            meta.setDisplayName(holders.apply(inv.getPlayer(), getName()));
            meta.addItemFlags(ItemFlag.values());
            meta.setLore(lore);
            item.setItemMeta(meta);

            return NBTItem.get(item).addTag(new ItemTag("skillId", skill.getSkill().getHandler().getId())).toItem();
        }

        public Placeholders getPlaceholders(PlayerData player, ClassSkill skill) {
            Placeholders holders = new Placeholders();
            holders.register("skill", skill.getSkill().getName());
            holders.register("unlock", "" + skill.getUnlockLevel());
            holders.register("level", "" + player.getSkillLevel(skill.getSkill()));
            return holders;
        }

        @Override
        public Placeholders getPlaceholders(SkillViewerInventory inv, int n) {
            return new Placeholders();
        }
    }

    public class UpgradeItem extends InventoryItem<SkillViewerInventory> {
        private int shiftCost = 1;

        public UpgradeItem(ConfigurationSection config) {
            super(config);
            if (config.contains("shift-cost")) {
                this.shiftCost = config.getInt("shift-cost");
                if (shiftCost < 1) {
                    MMOCore.log(Level.WARNING, "Upgrade shift-cost cannot be less than 1. Using default value: 1");
                    shiftCost = 1;
                }
            }

        }

        @Override
        public Placeholders getPlaceholders(SkillViewerInventory inv, int n) {
            RegisteredSkill selected = inv.selected == null ? null : inv.selected.getSkill();
            Placeholders holders = new Placeholders();

            holders.register("skill_caps", selected.getName().toUpperCase());
            holders.register("skill", selected.getName());
            holders.register("skill_points", "" + inv.getPlayerData().getSkillPoints());
            holders.register("shift_points", shiftCost);
            return holders;
        }
    }

    public class SkillViewerInventory extends GeneratedInventory {

        // Cached information
        private final List<ClassSkill> skills;
        private final List<Integer> skillSlots;
        private final List<Integer> slotSlots;

        //The skill the player Selected
        private ClassSkill selected;
        private int page = 0;

        public SkillViewerInventory(PlayerData playerData, EditableInventory editable) {
            super(playerData, editable);

            skills = new ArrayList<>(playerData.getProfess().getSkills());
            skillSlots = getEditable().getByFunction("skill").getSlots();
            slotSlots = getEditable().getByFunction("slot").getSlots();
            selected = skills.get(page * skillSlots.size());
        }

        @Override
        public String calculateName() {
            return getName().replace("{skill}", selected.getSkill().getName());
        }

        @Override
        public void open() {
            super.open();
        }

        @Override
        public void whenClicked(InventoryClickContext context, InventoryItem item) {

            /*
            if (skillSlots.contains(event.getRawSlot())
                    && event.getRawSlot() != ((SkillItem) getEditable().getByFunction("skill")).selectedSkillSlot) {
                player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1, 2);
                playerData.skillGuiDisplayOffset = (playerData.skillGuiDisplayOffset + (event.getRawSlot() - 13)) % skills.size();
                open();
                return;
            }
            */

            if (item.getFunction().equals("skill")) {
                int index = skillSlots.size() * page + skillSlots.indexOf(context.getSlot());
                player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1, 2);
                selected = skills.get(index);
                open();
                return;
            }

            if(item.getFunction().equals("reallocation")) {



                int spent= getPlayerData().countSkillPointsWhenReallocate();

                if (spent < 1) {
                    MMOCore.plugin.configManager.getSimpleMessage("no-skill-points-spent").send(player);
                    MMOCore.plugin.soundManager.getSound(SoundEvent.NOT_ENOUGH_POINTS).playTo(getPlayer());
                    return;
                }

                if (playerData.getSkillReallocationPoints() < 1) {
                    MMOCore.plugin.configManager.getSimpleMessage("not-skill-reallocation-point").send(player);
                    MMOCore.plugin.soundManager.getSound(SoundEvent.NOT_ENOUGH_POINTS).playTo(getPlayer());
                    return;
                }


                for(ClassSkill skill:playerData.getProfess().getSkills()) {
                    playerData.setSkillLevel(skill.getSkill(), 1);
                }
                playerData.giveSkillPoints(spent);
                playerData.setSkillReallocationPoints(playerData.getSkillReallocationPoints()-1);
                MMOCore.plugin.configManager.getSimpleMessage("skill-points-reallocated", "points", "" + playerData.getSkillPoints()).send(player);
                MMOCore.plugin.soundManager.getSound(SoundEvent.RESET_SKILLS).playTo(getPlayer());
                open();
            }

            if (item.getFunction().equals("previous")) {
                player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1, 2);
                page--;
                open();
                return;
            }

            if (item.getFunction().equals("next")) {
                player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1, 2);
                page++;
                open();
                return;
            }

            /*
             * binding or unbinding skills.
             */
            if (item.getFunction().equals("slot")) {
                int index = slotSlots.indexOf(context.getSlot());

                // unbind if there is a current spell.
                if (context.getClickType() == ClickType.RIGHT) {
                    if (!playerData.hasSkillBound(index)) {
                        MMOCore.plugin.configManager.getSimpleMessage("no-skill-bound").send(player);
                        player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1, 2);
                        return;
                    }
                    player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1, 2);
                    playerData.unbindSkill(index);
                    open();
                    return;
                }

                if (selected == null)
                    return;

                if (selected.getSkill().getTrigger().isPassive()) {
                    MMOCore.plugin.configManager.getSimpleMessage("not-active-skill").send(player);
                    player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1, 2);
                    return;
                }


                if (!playerData.hasSkillUnlocked(selected)) {
                    MMOCore.plugin.configManager.getSimpleMessage("not-unlocked-skill").send(player);
                    player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1, 2);
                    return;
                }

                player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1, 2);
                playerData.setBoundSkill(index, selected);
                open();
                return;
            }


            /*
             * upgrading a player skill
             */
            if (item.getFunction().equals("upgrade")) {
                int shiftCost = ((UpgradeItem) item).shiftCost;

                if (!playerData.hasSkillUnlocked(selected)) {
                    MMOCore.plugin.configManager.getSimpleMessage("not-unlocked-skill").send(player);
                    player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1, 2);
                    return;
                }

                if (playerData.getSkillPoints() < 1) {
                    MMOCore.plugin.configManager.getSimpleMessage("not-enough-skill-points").send(player);
                    player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1, 2);
                    return;
                }

                if (selected.hasMaxLevel() && playerData.getSkillLevel(selected.getSkill()) >= selected.getMaxLevel()) {
                    MMOCore.plugin.configManager.getSimpleMessage("skill-max-level-hit").send(player);
                    player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1, 2);
                    return;
                }

                if (context.getClickType().isShiftClick()) {
                    if (playerData.getSkillPoints() < shiftCost) {
                        MMOCore.plugin.configManager.getSimpleMessage("not-enough-skill-points-shift", "shift_points", "" + shiftCost).send(player);
                        player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1, 2);
                        return;
                    }

                    playerData.giveSkillPoints(-shiftCost);
                    playerData.setSkillLevel(selected.getSkill(), playerData.getSkillLevel(selected.getSkill()) + shiftCost);
                } else {
                    playerData.giveSkillPoints(-1);
                    playerData.setSkillLevel(selected.getSkill(), playerData.getSkillLevel(selected.getSkill()) + 1);
                }

                MMOCore.plugin.configManager.getSimpleMessage("upgrade-skill", "skill", selected.getSkill().getName(), "level",
                        "" + playerData.getSkillLevel(selected.getSkill())).send(player);
                player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1, 2);
                open();
            }
        }

    }

    private int mod(int x, int n) {
        return x < 0 ? (x + n) : (x % n);
    }
}