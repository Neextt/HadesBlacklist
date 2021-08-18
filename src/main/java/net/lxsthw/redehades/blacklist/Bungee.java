package net.lxsthw.redehades.blacklist;

import net.lxsthw.redehades.blacklist.commands.Commands;
import net.lxsthw.redehades.blacklist.database.Backend;
import net.lxsthw.redehades.blacklist.listeners.Listeners;
import net.lxsthw.redehades.blacklist.types.PunishType;
import net.lxsthw.redehades.blacklist.utils.FileUtils;
import net.lxsthw.redehades.blacklist.utils.ModuleLogger;
import net.md_5.bungee.api.plugin.Plugin;
import net.md_5.bungee.config.Configuration;
import net.md_5.bungee.config.YamlConfiguration;

import java.io.*;
import java.util.logging.Level;

public class Bungee extends Plugin {

    private static Bungee instance;

    public static final ModuleLogger LOGGER = new ModuleLogger("HadesBlacklist");

    public Bungee() {
        instance = this;
    }

    @Override
    public void onEnable() {
        saveDefaultConfig();

        Backend.makeBackend();
        PunishType.makeTypes();

        Commands.makeCommands();
        Listeners.makeListeners();

        this.getLogger().info("O plugin foi ativado.");
    }

    @Override
    public void onDisable() {
        instance = null;
        this.getLogger().info("O plugin foi desativado.");
    }

    private Configuration config;

    public void saveDefaultConfig() {
        File file = new File("plugins/HadesBlacklist/config.yml");
        if (!file.exists()) {
            file.getParentFile().mkdirs();
            FileUtils.copyFile(Bungee.getInstance().getResourceAsStream("config.yml"), file);
        }

        try {
            this.config = YamlConfiguration.getProvider(YamlConfiguration.class)
                    .load(new InputStreamReader(new FileInputStream(file), "UTF-8"));
        } catch (UnsupportedEncodingException e) {
            this.getLogger().log(Level.WARNING, "Cannot load config.yml: ", e);
        } catch (FileNotFoundException e) {
            this.getLogger().log(Level.WARNING, "Cannot load config.yml: ", e);
        }
    }

    public Configuration getConfig() {
        return config;
    }

    public static Bungee getInstance() {
        return instance;
    }
}
