package net.lxsthw.redehades.blacklist.commands;

import net.lxsthw.redehades.blacklist.Bungee;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.plugin.Command;

public abstract class Commands extends Command {

    public Commands(String name, String... aliases) {
        super(name, null, aliases);
        ProxyServer.getInstance().getPluginManager().registerCommand(Bungee.getInstance(), this);
    }

    public static void makeCommands() {
        new PunishCommand();
        new UnPunishCommand();
    }
}
