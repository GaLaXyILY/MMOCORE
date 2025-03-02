package net.Indyuce.mmocore.guild.provided;

import io.lumine.mythic.lib.comp.interaction.relation.Relationship;
import net.Indyuce.mmocore.api.player.PlayerData;
import net.Indyuce.mmocore.guild.AbstractGuild;
import net.Indyuce.mmocore.guild.GuildModule;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class NoneGuildModule implements GuildModule {
    @Override
    public @Nullable AbstractGuild getGuild(PlayerData playerData) {
        return null;
    }

    @Override
    public @NotNull Relationship getRelationship(Player player, Player target) {
        return Relationship.GUILD_NEUTRAL;
    }
}
