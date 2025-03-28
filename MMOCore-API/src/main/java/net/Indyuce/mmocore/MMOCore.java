package net.Indyuce.mmocore;

import io.lumine.mythic.lib.MythicLib;
import io.lumine.mythic.lib.UtilityMethods;
import io.lumine.mythic.lib.data.sql.SQLDataSource;
import io.lumine.mythic.lib.metrics.bukkit.Metrics;
import io.lumine.mythic.lib.player.modifier.PlayerModifier;
import io.lumine.mythic.lib.util.MMOPlugin;
import io.lumine.mythic.lib.version.SpigotPlugin;
import net.Indyuce.mmocore.api.player.PlayerData;
import net.Indyuce.mmocore.api.player.attribute.AttributeModifier;
import net.Indyuce.mmocore.api.player.profess.resource.PlayerResource;
import net.Indyuce.mmocore.command.MMOCoreCommandTreeRoot;
import net.Indyuce.mmocore.command.api.ToggleableCommand;
import net.Indyuce.mmocore.comp.citizens.CitizenInteractEventListener;
import net.Indyuce.mmocore.comp.citizens.CitizensMMOLoader;
import net.Indyuce.mmocore.comp.mythicmobs.MythicHook;
import net.Indyuce.mmocore.comp.mythicmobs.MythicMobsMMOLoader;
import net.Indyuce.mmocore.comp.placeholder.DefaultParser;
import net.Indyuce.mmocore.comp.placeholder.PlaceholderAPIParser;
import net.Indyuce.mmocore.comp.placeholder.PlaceholderParser;
import net.Indyuce.mmocore.comp.region.DefaultRegionHandler;
import net.Indyuce.mmocore.comp.region.RegionHandler;
import net.Indyuce.mmocore.comp.region.WorldGuardMMOLoader;
import net.Indyuce.mmocore.comp.region.WorldGuardRegionHandler;
import net.Indyuce.mmocore.comp.region.pvpmode.PvPModeListener;
import net.Indyuce.mmocore.comp.vault.VaultEconomy;
import net.Indyuce.mmocore.comp.vault.VaultMMOLoader;
import net.Indyuce.mmocore.guild.GuildModule;
import net.Indyuce.mmocore.guild.GuildModuleType;
import net.Indyuce.mmocore.guild.GuildRelationHandler;
import net.Indyuce.mmocore.guild.provided.Guild;
import net.Indyuce.mmocore.guild.provided.MMOCoreGuildModule;
import net.Indyuce.mmocore.guild.provided.YAMLGuildDataManager;
import net.Indyuce.mmocore.manager.*;
import net.Indyuce.mmocore.manager.data.DataProvider;
import net.Indyuce.mmocore.manager.data.GuildDataManager;
import net.Indyuce.mmocore.manager.data.LegacyDataProvider;
import net.Indyuce.mmocore.manager.data.PlayerDataManager;
import net.Indyuce.mmocore.manager.data.sql.SQLDataHandler;
import net.Indyuce.mmocore.manager.profession.*;
import net.Indyuce.mmocore.manager.social.BoosterManager;
import net.Indyuce.mmocore.manager.social.PartyManager;
import net.Indyuce.mmocore.manager.social.RequestManager;
import net.Indyuce.mmocore.party.PartyModule;
import net.Indyuce.mmocore.party.PartyModuleType;
import net.Indyuce.mmocore.party.PartyRelationHandler;
import net.Indyuce.mmocore.party.provided.MMOCorePartyModule;
import net.Indyuce.mmocore.script.mechanic.ExperienceMechanic;
import net.Indyuce.mmocore.script.mechanic.ManaMechanic;
import net.Indyuce.mmocore.script.mechanic.StaminaMechanic;
import net.Indyuce.mmocore.script.mechanic.StelliumMechanic;
import net.Indyuce.mmocore.skill.cast.SkillCastingMode;
import net.Indyuce.mmocore.skill.trigger.MMOCoreTriggerType;
import org.apache.commons.lang.Validate;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.event.EventPriority;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.logging.Level;

public class MMOCore extends MMOPlugin {
    public static MMOCore plugin;
    public final WaypointManager waypointManager = new WaypointManager();
    public final SoundManager soundManager = new SoundManager();
    public final RequestManager requestManager = new RequestManager();
    public final ConfigItemManager configItems = new ConfigItemManager();
    public final ActionBarManager actionBarManager = new ActionBarManager();
    public final SkillManager skillManager = new SkillManager();
    public final ClassManager classManager = new ClassManager();
    public final DropTableManager dropTableManager = new DropTableManager();
    public final BoosterManager boosterManager = new BoosterManager();
    public final AttributeManager attributeManager = new AttributeManager();
    public final PartyManager partyManager = new PartyManager();
    public final QuestManager questManager = new QuestManager();
    public final ProfessionManager professionManager = new ProfessionManager();
    public final ExperienceManager experience = new ExperienceManager();
    public final LootChestManager lootChests = new LootChestManager();
    public final MMOLoadManager loadManager = new MMOLoadManager();
    public final RestrictionManager restrictionManager = new RestrictionManager();
    public final SkillTreeManager skillTreeManager = new SkillTreeManager();
    public final StatManager statManager = new StatManager();
    public final GuildDataManager nativeGuildManager = new YAMLGuildDataManager();
    public final PlayerDataManager playerDataManager = new PlayerDataManager(this);
    @Deprecated
    public final DataProvider dataProvider = new LegacyDataProvider();

    // Profession managers
    public final CustomBlockManager mineManager = new CustomBlockManager();
    public final FishingManager fishingManager = new FishingManager();
    public final AlchemyManager alchemyManager = new AlchemyManager();
    public final EnchantManager enchantManager = new EnchantManager();
    public final SmithingManager smithingManager = new SmithingManager();

    @NotNull
    public ConfigManager configManager;
    public VaultEconomy economy;
    public RegionHandler regionHandler = new DefaultRegionHandler();
    public PlaceholderParser placeholderParser = new DefaultParser();

    // Modules
    @NotNull
    public PartyModule partyModule;
    public GuildModule guildModule;

    public MMOCore() {
        plugin = this;
    }

    @Override
    public void onLoad() {
        getLogger().log(Level.INFO, "Plugin file is called '" + getFile().getName() + "'");

        // Register MMOCore-specific objects
        MythicLib.plugin.getEntities().registerRelationHandler(new PartyRelationHandler());
        MythicLib.plugin.getEntities().registerRelationHandler(new GuildRelationHandler());
        PlayerModifier.registerPlayerModifierType("attribute", configObject -> new AttributeModifier(configObject));
        MMOCoreTriggerType.registerAll();

        // Custom scripts
        MythicLib.plugin.getSkills().registerMechanic("mana", config -> new ManaMechanic(config));
        MythicLib.plugin.getSkills().registerMechanic("stamina", config -> new StaminaMechanic(config));
        MythicLib.plugin.getSkills().registerMechanic("stellium", config -> new StelliumMechanic(config));
        MythicLib.plugin.getSkills().registerMechanic("mmocore_experience", config -> new ExperienceMechanic(config));

        // Register extra objective, drop items...
        if (Bukkit.getPluginManager().getPlugin("WorldGuard") != null)
            loadManager.registerLoader(new WorldGuardMMOLoader());

        if (Bukkit.getPluginManager().getPlugin("Citizens") != null)
            loadManager.registerLoader(new CitizensMMOLoader());

        if (Bukkit.getPluginManager().getPlugin("Vault") != null) loadManager.registerLoader(new VaultMMOLoader());

        if (Bukkit.getPluginManager().getPlugin("MythicMobs") != null)
            loadManager.registerLoader(new MythicMobsMMOLoader());
    }

    @Override
    public void onEnable() {
        new SpigotPlugin(70575, this).checkForUpdate();
        new Metrics(this);
        saveDefaultConfig();

        final int configVersion = getConfig().contains("config-version", true) ? getConfig().getInt("config-version") : -1;
        final int defConfigVersion = getConfig().getDefaults().getInt("config-version");
        if (configVersion != defConfigVersion) {
            getLogger().warning("You may be using an outdated config.yml!");
            getLogger().warning("(Your config version: '" + configVersion + "' | Expected config version: '" + defConfigVersion + "')");
        }

        if (getConfig().isConfigurationSection("mysql") && getConfig().getBoolean("mysql.enabled")) {
            final SQLDataSource dataSource = new SQLDataSource(this);
            playerDataManager.setDataHandler(new SQLDataHandler(dataSource));
        }

        if (getConfig().isConfigurationSection("default-playerdata"))
            playerDataManager.loadDefaultData(getConfig().getConfigurationSection("default-playerdata"));

        if (Bukkit.getPluginManager().getPlugin("Vault") != null) economy = new VaultEconomy();

        if (Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null) {
            placeholderParser = new PlaceholderAPIParser();
            getLogger().log(Level.INFO, "Hooked onto PlaceholderAPI");
        }

        if (Bukkit.getPluginManager().getPlugin("Citizens") != null) {
            Bukkit.getPluginManager().registerEvents(new CitizenInteractEventListener(), this);
            getLogger().log(Level.INFO, "Hooked onto Citizens");
        }

        if (Bukkit.getPluginManager().getPlugin("WorldGuard") != null) {
            regionHandler = new WorldGuardRegionHandler();
            if (getConfig().getBoolean("pvp_mode.enabled"))
                Bukkit.getPluginManager().registerEvents(new PvPModeListener(), this);
            getLogger().log(Level.INFO, "Hooked onto WorldGuard");
        }

        if (Bukkit.getPluginManager().getPlugin("MythicMobs") != null) {
            Bukkit.getServer().getPluginManager().registerEvents(new MythicHook(), this);
            MMOCore.plugin.getLogger().log(Level.INFO, "Hooked onto MythicMobs");
        }

        /*
         * Resource regeneration. Must check if entity is dead otherwise regen
         * will make the 'respawn' button glitched plus HURT entity effect bug
         */
        new BukkitRunnable() {
            public void run() {
                for (PlayerData player : PlayerData.getAll())
                    if (player.isOnline() && !player.getPlayer().isDead())
                        for (PlayerResource resource : PlayerResource.values()) {
                            double regenAmount = player.getProfess().getHandler(resource).getRegen(player);
                            if (regenAmount != 0)
                                resource.regen(player, regenAmount);
                        }
            }
        }.runTaskTimer(MMOCore.plugin, 100, 20);

        /*
         * For the sake of the lord, make sure they aren't using MMOItems Mana and
         * Stamina Addon...This should prevent a couple error reports produced by people
         * not reading the installation guide...
         */
        if (Bukkit.getPluginManager().getPlugin("MMOMana") != null) {
            getLogger().log(Level.SEVERE, ChatColor.DARK_RED + "MMOCore is not meant to be used with MMOItems ManaAndStamina");
            getLogger().log(Level.SEVERE, ChatColor.DARK_RED + "Please read the installation guide!");
            return;
        }

        initializePlugin(false);

        // Load party module
        try {
            String partyPluginName = UtilityMethods.enumName(getConfig().getString("party-plugin"));
            PartyModuleType moduleType = PartyModuleType.valueOf(partyPluginName);
            Validate.isTrue(moduleType.isValid(), "Plugin " + moduleType.getPluginName() + " is not installed");
            partyModule = moduleType.provideModule();
            getLogger().log(Level.INFO, "Hooked parties onto " + moduleType.getPluginName());
        } catch (Throwable exception) {
            getLogger().log(Level.WARNING, "Could not initialize party module: " + exception.getMessage());
            partyModule = new MMOCorePartyModule();
        }

        // Load guild module
        try {
            String pluginName = UtilityMethods.enumName(getConfig().getString("guild-plugin"));
            GuildModuleType moduleType = GuildModuleType.valueOf(pluginName);
            Validate.isTrue(moduleType.isValid(), "Plugin '" + moduleType.name() + "' is not installed");
            guildModule = moduleType.provideModule();
            getLogger().log(Level.INFO, "Hooked guilds onto " + moduleType.getPluginName());
        } catch (RuntimeException exception) {
            getLogger().log(Level.WARNING, "Could not initialize guild module: " + exception.getMessage());
            guildModule = new MMOCoreGuildModule();
        }

        // Skill casting
        try {
            final SkillCastingMode mode = SkillCastingMode.valueOf(UtilityMethods.enumName(getConfig().getString("skill-casting.mode")));
            mode.setCurrent(getConfig().getConfigurationSection("skill-casting"));
        } catch (RuntimeException exception) {
            getLogger().log(Level.WARNING, "Could not load skill casting: " + exception.getMessage());
        }

        // Load MMOCore-Bukkit module
        try {
            Class.forName("net.Indyuce.mmocore.MMOCoreBukkit").getConstructor(MMOCore.class).newInstance(this);
        } catch (Throwable exception) {
            exception.printStackTrace();
            throw new RuntimeException("Cannot run an API build on Spigot!");
        }

        /*
         * Initialize player data from all online players. This is very important to do
         * that after registering all the professses otherwise the player datas can't
         * recognize what profess the player has and professes will be lost
         */
        playerDataManager.initialize(EventPriority.LOW, EventPriority.NORMAL);

        // load guild data after loading player data
        nativeGuildManager.load();

        // Toggleable Commands
        ToggleableCommand.register();

        // Register MMOCore command what soever
        MMOCoreCommandTreeRoot mmoCoreCommand = new MMOCoreCommandTreeRoot();
        getCommand("mmocore").setExecutor(mmoCoreCommand);
        getCommand("mmocore").setTabCompleter(mmoCoreCommand);
    }

    @Override
    public void onDisable() {

        // Save guild info
        for (Guild guild : nativeGuildManager.getAll())
            nativeGuildManager.save(guild);

        // Close player data manager
        playerDataManager.close();

        // Reset active blocks
        mineManager.resetRemainingBlocks();

        // Clear spawned loot chests
        lootChests.getActive().forEach(chest -> chest.expire(false));
    }

    /**
     * Called either when the server starts when initializing the manager for
     * the first time, or when issuing a plugin reload; in that case, stuff
     * like listeners must all be cleared before.
     * <p>
     * Also see {@link MMOCoreManager}
     *
     * @param clearBefore True when issuing a plugin reload
     */
    public void initializePlugin(boolean clearBefore) {
        if (clearBefore)
            reloadConfig();

        configManager = new ConfigManager();

        skillManager.initialize(clearBefore);
        mineManager.initialize(clearBefore);
        partyManager.initialize(clearBefore);
        attributeManager.initialize(clearBefore);

        // Experience must be loaded before professions and classes
        experience.initialize(clearBefore);

        // Drop tables must be loaded before professions
        dropTableManager.initialize(clearBefore);
        statManager.initialize(clearBefore);
        professionManager.initialize(clearBefore);

        skillTreeManager.initialize(clearBefore);
        classManager.initialize(clearBefore);
        questManager.initialize(clearBefore);
        lootChests.initialize(clearBefore);
        restrictionManager.initialize(clearBefore);
        waypointManager.initialize(clearBefore);
        requestManager.initialize(clearBefore);
        soundManager.initialize(clearBefore);
        configItems.initialize(clearBefore);
        //Needs to be loaded after the class manager.
        InventoryManager.load();

        if (getConfig().isConfigurationSection("action-bar"))
            actionBarManager.reload(getConfig().getConfigurationSection("action-bar"));

        if (clearBefore)
            PlayerData.getAll().forEach(PlayerData::reload);
    }

    public static void log(String message) {
        log(Level.INFO, message);
    }

    public static void log(Level level, String message) {
        plugin.getLogger().log(level, message);
    }

    public File getJarFile() {
        return getFile();
    }

    public boolean hasEconomy() {
        return economy != null && economy.isValid();
    }
}
