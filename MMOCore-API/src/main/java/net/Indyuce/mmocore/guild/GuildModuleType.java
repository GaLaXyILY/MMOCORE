package net.Indyuce.mmocore.guild;

import net.Indyuce.mmocore.guild.compat.GuildsGuildModule;
import net.Indyuce.mmocore.guild.compat.KingdomsXGuildModule;
import net.Indyuce.mmocore.guild.compat.UltimateClansGuildModule;
import net.Indyuce.mmocore.guild.provided.MMOCoreGuildModule;
import net.Indyuce.mmocore.guild.provided.NoneGuildModule;
import org.bukkit.Bukkit;

import javax.inject.Provider;

public enum GuildModuleType {
    // Useless since MythicLib already supports FactionBridge
    // FACTIONS("Factions", FactionsGuildModule::new),
    MMOCORE("MMOCore", MMOCoreGuildModule::new),
    NONE("MMOCore", NoneGuildModule::new),
    GUILDS("Guilds", GuildsGuildModule::new),
    KINGDOMSX("Kingdoms", KingdomsXGuildModule::new),
    ULTIMATE_CLANS("UltimateClans", UltimateClansGuildModule::new),
    ;

    private final String pluginName;
    private final Provider<GuildModule> provider;

    GuildModuleType(String pluginName, Provider<GuildModule> provider) {
        this.pluginName = pluginName;
        this.provider = provider;
    }

    public String getPluginName() {
        return pluginName;
    }

    public boolean isValid() {
        return Bukkit.getPluginManager().getPlugin(pluginName) != null;
    }

    public GuildModule provideModule() {
        return provider.get();
    }
}
