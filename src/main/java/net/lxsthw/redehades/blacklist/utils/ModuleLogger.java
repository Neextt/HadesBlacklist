package net.lxsthw.redehades.blacklist.utils;

import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

public class ModuleLogger extends Logger {

    private static Logger logger;

    static {
        try {
            Class.forName("net.md_5.bungee.api.ProxyServer");
            logger = net.md_5.bungee.api.ProxyServer.getInstance().getLogger();
        } catch (ClassNotFoundException e) {
            logger = org.bukkit.Bukkit.getLogger();
        }
    }

    private String prefix;

    public ModuleLogger(String name) {
        this(logger, "[" + name + "] ");
    }

    public ModuleLogger(Logger parent, String name) {
        super(name, null);
        this.setParent(parent);
        this.setLevel(Level.ALL);
        this.prefix = name;
    }

    @Override
    public void log(LogRecord record) {
        record.setMessage(prefix + record.getMessage());
        super.log(record);
    }

    public ModuleLogger getModule(String module) {
        return new ModuleLogger(this.getParent(), prefix + "[" + module + "] ");
    }
}

