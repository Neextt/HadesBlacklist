package net.lxsthw.redehades.blacklist.database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.logging.Level;
import javax.sql.rowset.CachedRowSet;
import javax.sql.rowset.RowSetProvider;
import net.lxsthw.redehades.blacklist.Bungee;
import net.md_5.bungee.config.Configuration;

public class MySQLBackend extends Backend {

    private Connection connection;
    private ExecutorService executor;
    private String host, port, database, username, password;

    public MySQLBackend() {
        Configuration config = Bungee.getInstance().getConfig();
        this.host = config.getString("mysql.host");
        this.port = config.get("mysql.port").toString();
        this.database = config.getString("mysql.database");
        this.username = config.getString("mysql.username");
        this.password = config.getString("mysql.password");

        this.executor = Executors.newCachedThreadPool();
        openConnection();
        update("CREATE TABLE IF NOT EXISTS blackuser ("
                + "id VARCHAR(36),"
                + "name VARCHAR(36),"
                + "ip VARCHAR(36),"
                + "PRIMARY KEY(id)) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE utf8_bin;");
        update("CREATE TABLE IF NOT EXISTS tpunishs ("
                + "id INT AUTO_INCREMENT,"
                + "identifier VARCHAR(36),"
                + "punisher VARCHAR(50),"
                + "type TEXT,"
                + "proof VARCHAR(255) DEFAULT 'Nenhuma',"
                + "reason TEXT,"
                + "bannedOn LONG,"
                + "expiresOn LONG, PRIMARY KEY(id)) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE utf8_bin;");
        if (query("SHOW COLUMNS FROM `blacklist` LIKE 'proof'") == null) {
            update("ALTER TABLE blacklist "
                    + "ADD COLUMN proof VARCHAR(255) DEFAULT 'Nenhuma' AFTER type;");
        }
    }

    public Connection getConnection() {
        if (!isConnected()) {
            openConnection();
        }

        return connection;
    }

    public void closeConnection() {
        if (isConnected()) {
            try {
                connection.close();
            } catch (SQLException e) {
                LOGGER.log(Level.SEVERE, "Cannot close MySQL connection: ", e);
            }
        }
    }

    public boolean isConnected() {
        try {
            return (connection == null || connection.isClosed() || !connection.isValid(5)) ? false : true;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "MySQL error: ", e);
        }

        return false;
    }

    public void openConnection() {
        if (!isConnected()) {
            try {
                boolean bol = connection == null;
                connection = DriverManager.getConnection("jdbc:mysql://" + host + ":" + port + "/" + database
                                + "?verifyServerCertificate=false&useSSL=false&useUnicode=yes&characterEncoding=UTF-8",
                        username, password);
                if (bol) {
                    LOGGER.info("Conectado ao MySQL!");
                    return;
                }

                LOGGER.info("Reconectado ao MySQL!");
            } catch (SQLException e) {
                LOGGER.log(Level.SEVERE, "Cannot open MySQL connection: ", e);
            }
        }
    }

    public void update(String sql, Object... vars) {
        try {
            PreparedStatement ps = prepareStatement(sql, vars);
            ps.execute();
            ps.close();
        } catch (SQLException e) {
            LOGGER.log(Level.WARNING, "Cannot execute SQL: ", e);
        }
    }

    public void execute(String sql, Object... vars) {
        executor.execute(() -> {
            update(sql, vars);
        });
    }

    public PreparedStatement prepareStatement(String query, Object... vars) {
        try {
            PreparedStatement ps = getConnection().prepareStatement(query);
            for (int i = 0; i < vars.length; i++) {
                ps.setObject(i + 1, vars[i]);
            }
            return ps;
        } catch (SQLException e) {
            LOGGER.log(Level.WARNING, "Cannot Prepare Statement: ", e);
        }

        return null;
    }

    public CachedRowSet query(String query, Object... vars) {
        CachedRowSet rowSet = null;
        try {
            Future<CachedRowSet> future = executor.submit(new Callable<CachedRowSet>() {

                @Override
                public CachedRowSet call() {
                    try {
                        PreparedStatement ps = prepareStatement(query, vars);

                        ResultSet rs = ps.executeQuery();
                        CachedRowSet crs = RowSetProvider.newFactory().createCachedRowSet();
                        crs.populate(rs);
                        rs.close();
                        ps.close();

                        if (crs.next()) {
                            return crs;
                        }
                    } catch (Exception e) {
                        LOGGER.log(Level.WARNING, "Cannot execute Query: ", e);
                    }

                    return null;
                }
            });

            if (future.get() != null) {
                rowSet = future.get();
            }
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Cannot call FutureTask: ", e);
        }

        return rowSet;
    }
}
