package net.lxsthw.redehades.blacklist.database;

import net.lxsthw.redehades.blacklist.Bungee;
import net.lxsthw.redehades.blacklist.utils.ModuleLogger;

import javax.sql.rowset.CachedRowSet;
import java.util.logging.Logger;

public abstract class Backend {

    public abstract void closeConnection();

    public abstract void update(String sql, Object... vars);

    public abstract void execute(String sql, Object... vars);

    public abstract CachedRowSet query(String sql, Object... vars);

    private static Backend instance;

    public static final ModuleLogger LOGGER = Bungee.LOGGER.getModule("DATABASE");

    public static void makeBackend() {
        if (Bungee.getInstance().getConfig().getBoolean("mysql.use", true)) {
            instance = new MySQLBackend();
        }
    }

    public static Backend getInstance() {
        return instance;
    }
}

