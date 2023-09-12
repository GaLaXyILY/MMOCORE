package net.Indyuce.mmocore.gui.social.guild;

import io.lumine.mythic.lib.gui.framework.EditableInventory;
import io.lumine.mythic.lib.gui.framework.GeneratedInventory;
import io.lumine.mythic.lib.gui.framework.item.InventoryItem;
import io.lumine.mythic.lib.gui.framework.item.SimpleItem;
import net.Indyuce.mmocore.MMOCore;
import net.Indyuce.mmocore.api.player.PlayerData;
import net.Indyuce.mmocore.manager.InventoryManager;
import net.Indyuce.mmocore.manager.data.GuildDataManager;
import net.Indyuce.mmocore.api.util.input.ChatInput;
import net.Indyuce.mmocore.api.util.input.PlayerInput;
import org.bukkit.Sound;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.jetbrains.annotations.Nullable;

public class EditableGuildCreation extends EditableInventory<PlayerData> {
    public EditableGuildCreation() {
        super("guild-creation");
    }

    @Override
    public GeneratedInventory generate(PlayerData playerData, @Nullable GeneratedInventory generatedInventory) {
        return new GuildCreationInventory(playerData, this);

    }

    @Override
    public InventoryItem loadItem(String function, ConfigurationSection config) {
        return new SimpleItem(config);
    }

    public class GuildCreationInventory extends GeneratedInventory<PlayerData> {
        public GuildCreationInventory(PlayerData playerData, EditableInventory editable) {
            super(playerData, editable);
        }

        @Override
        public String applyNamePlaceholders(String s) {
            return s;
        }

        @Override
        public void whenClicked(InventoryClickEvent event, InventoryItem item) {

            if (item.getFunction().equals("create")) {
                new ChatInput(player, PlayerInput.InputType.GUILD_CREATION_TAG, this, input -> {
                    if (MMOCore.plugin.dataProvider.getGuildManager().getConfig().shouldUppercaseTags())
                        input = input.toUpperCase();

                    if (check(player, input, MMOCore.plugin.dataProvider.getGuildManager().getConfig().getTagRules())) {
                        String tag = input;

                        new ChatInput(player, PlayerInput.InputType.GUILD_CREATION_NAME, this, name -> {
                            if (check(player, name, MMOCore.plugin.dataProvider.getGuildManager().getConfig().getNameRules())) {
                                MMOCore.plugin.dataProvider.getGuildManager().newRegisteredGuild(playerData.getUniqueId(), name, tag);
                                MMOCore.plugin.dataProvider.getGuildManager().getGuild(tag.toLowerCase()).addMember(playerData.getUniqueId());

                                InventoryManager.GUILD_VIEW.generate(playerData, this).open();
                                player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1, 1);
                            }
                        });
                    }
                });

                return;
            }

            if (item.getFunction().equals("back"))
                player.closeInventory();
        }
    }

    public boolean check(Player player, String input, GuildDataManager.GuildConfiguration.NamingRules rules) {
        String reason;

        if (input.length() <= rules.getMax() && input.length() >= rules.getMin())
            if (input.matches(rules.getRegex()))
                if (!MMOCore.plugin.dataProvider.getGuildManager().isRegistered(input))
                    return true;
                else
                    reason = MMOCore.plugin.configManager.getSimpleMessage("guild-creation.reasons.already-taken").message();
            else
                reason = MMOCore.plugin.configManager.getSimpleMessage("guild-creation.reasons.invalid-characters").message();
        else
            reason = MMOCore.plugin.configManager.getSimpleMessage("guild-creation.reasons.invalid-length", "min", "" + rules.getMin(), "max", "" + rules.getMax()).message();

        MMOCore.plugin.configManager.getSimpleMessage("guild-creation.failed", "reason", reason).send(player);
        return false;
    }
}
