package net.lxsthw.redehades.blacklist.profile;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import javax.sql.rowset.CachedRowSet;

import net.lxsthw.redehades.blacklist.database.Backend;
import net.lxsthw.redehades.blacklist.types.Ban;
import net.lxsthw.redehades.blacklist.types.Mute;
import net.lxsthw.redehades.blacklist.types.Punish;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.connection.PendingConnection;
import net.md_5.bungee.api.connection.ProxiedPlayer;

public class PunishHistory {

    private String id, name, ip;
    private List<Punish> bans = new ArrayList<>(), mutes = new ArrayList<>();

    public PunishHistory(PendingConnection player, String ip) {
        this.id = player.getName().toLowerCase();
        this.name = player.getName();
        this.ip = ip;

        this.loadHistory();
    }

    public ProxiedPlayer getPlayer() {
        return name == null ? null : ProxyServer.getInstance().getPlayer(name);
    }

    public void destroy() {
        this.id = null;
        this.name = null;
        this.ip = null;

        this.bans.clear();
        this.mutes.clear();
        this.bans = mutes = null;
    }

    public void updateIP() {
        ip = getPlayer().getAddress().getAddress().getHostAddress();
        CachedRowSet rs = Backend.getInstance().query("SELECT * FROM blackuser WHERE id = ?", id);
        if (rs != null) {
            try {
                if (!rs.getString("ip").equals(ip)) {
                    Backend.getInstance().execute("UPDATE blackuser SET ip = ? WHERE id = ?", ip, id);
                }
            } catch (SQLException e) {
                Backend.LOGGER.log(Level.WARNING,
                        "Cannot check ip from identifier \"" + id + "\" and name \"" + name + "\"");
            }
        } else {
            Backend.getInstance().execute("INSERT INTO blackuser (id, name, ip) VALUES (?, ?, ?)", id, name, ip);
        }
    }

    public void loadHistory() {
        CachedRowSet rs = Backend.getInstance().query("SELECT * FROM blackuser WHERE id = ?", id);
        if (rs != null) {
            try {
                ip = rs.getString("ip");
            } catch (SQLException e) {
                Backend.LOGGER.log(Level.WARNING,
                        "Cannot check ip from identifier \"" + id + "\" and name \"" + name + "\"");
            }
        }

        rs =
                Backend.getInstance().query("SELECT * FROM blacklist WHERE identifier = ?", id);
        if (rs != null) {
            try {
                rs.beforeFirst();
                while (rs.next()) {
                    int id = rs.getInt("id");
                    String punisher = rs.getString("punisher");
                    String proof = rs.getString("proof");
                    String type = rs.getString("type");
                    String reason = rs.getString("reason");
                    long bannedOn = rs.getLong("bannedOn");
                    long expiresOn = rs.getLong("expiresOn");

                    if (type.equals("BAN")) {
                        bans.add(new Ban(id, punisher, proof, reason, false, bannedOn, expiresOn));
                    } else if (type.equals("MUTE")) {
                        mutes.add(new Mute(id, punisher, proof, reason, false, bannedOn, expiresOn));
                    }
                }
            } catch (SQLException e) {
                Backend.LOGGER.log(Level.WARNING,
                        "Cannot load PunishHistory from identifier \"" + id + "\" and name \"" + name + "\"",
                        e);
            }
            rs = null;
        }

        rs = Backend.getInstance().query("SELECT * FROM blacklist WHERE identifier = ?", ip);
        if (rs != null) {
            try {
                rs.beforeFirst();
                while (rs.next()) {
                    int id = rs.getInt("id");
                    String punisher = rs.getString("punisher");
                    String proof = rs.getString("proof");
                    String type = rs.getString("type");
                    String reason = rs.getString("reason");
                    long bannedOn = rs.getLong("bannedOn");
                    long expiresOn = rs.getLong("expiresOn");

                    if (type.equals("BAN")) {
                        bans.add(new Ban(id, punisher, proof, reason, true, bannedOn, expiresOn));
                    } else if (type.equals("MUTE")) {
                        mutes.add(new Mute(id, punisher, proof, reason, true, bannedOn, expiresOn));
                    }
                }
            } catch (SQLException e) {
                Backend.LOGGER.log(Level.WARNING,
                        "Cannot load PunishHistory from identifier \"" + id + "\" and name \"" + name + "\"",
                        e);
            }
            rs = null;
        }
    }

    public List<Punish> listBans() {
        return bans;
    }

    public List<Punish> listMutes() {
        return mutes;
    }
}

