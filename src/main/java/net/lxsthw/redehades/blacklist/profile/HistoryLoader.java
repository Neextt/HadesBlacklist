package net.lxsthw.redehades.blacklist.profile;

import com.google.common.collect.ImmutableList;
import net.lxsthw.redehades.blacklist.Bungee;
import net.lxsthw.redehades.blacklist.database.Backend;
import net.lxsthw.redehades.blacklist.types.Ban;
import net.lxsthw.redehades.blacklist.types.Mute;
import net.lxsthw.redehades.blacklist.types.Punish;
import net.lxsthw.redehades.blacklist.types.PunishType;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.PendingConnection;
import net.md_5.bungee.api.connection.ProxiedPlayer;

import javax.sql.rowset.CachedRowSet;
import java.sql.SQLException;
import java.util.*;
import java.util.stream.Collectors;

public class HistoryLoader {

    private static Map<UUID, PunishHistory> histories = new HashMap<>();

    public static String unpunishAll(String target, String unpunisher, String reason) {
        ProxiedPlayer player = ProxyServer.getInstance().getPlayer(target);
        PunishHistory ph = null;
        String ip = null;

        if (player != null && (ph = getHistory(player)) != null) {
            ip = player.getAddress().getHostString();
            ph.listBans().clear();
            ph.listMutes().clear();
        } else {
            CachedRowSet rs = Backend.getInstance().query("SELECT * FROM blackuser WHERE id = ?",
                    target.toLowerCase());
            if (rs != null) {
                try {
                    ip = rs.getString("ip");
                    target = rs.getString("name");
                } catch (SQLException e) {
                    return "§cFalha ao solicitar ip e nome registrado do jogador " + target + ".";
                }
            }
        }

        Backend.getInstance().execute("DELETE FROM blacklist WHERE identifier = ?",
                target.toLowerCase());
        if (ip != null) {
            Backend.getInstance().execute("DELETE FROM blacklist WHERE identifier = ?", ip);
            for (PunishHistory ph2 : histories.values()) {
                if (ph2.getPlayer() != null && ph2.getPlayer().getAddress().getHostString().equals(ip)) {
                    List<Punish> toRemove = new ArrayList<>();
                    for (Punish p : ph.listBans().stream().filter(p -> p.isIp())
                            .collect(Collectors.toList())) {
                        toRemove.add(p);
                    }

                    ph.listBans().removeAll(toRemove);
                    toRemove.clear();

                    for (Punish p : ph.listMutes().stream().filter(p -> p.isIp())
                            .collect(Collectors.toList())) {
                        toRemove.add(p);
                    }

                    ph.listMutes().removeAll(toRemove);
                    toRemove.clear();
                    toRemove = null;
                }
            }
        }

        for (String bc : Bungee.getInstance().getConfig().getStringList("revogar-all")) {
            ProxyServer.getInstance().broadcast(
                    TextComponent.fromLegacyText(bc.replace("&", "§").replace("%unpunisher%", unpunisher)
                            .replace("%target%", target).replace("%reason%", reason)));
        }
        return "";
    }

    public static String insertNewPunish(String target, String punisher, String proof,
                                         PunishType type) {
        ProxiedPlayer player = ProxyServer.getInstance().getPlayer(target);
        PunishHistory ph = null;
        String ip = null;

        long bannedOn = System.currentTimeMillis();
        Punish punish = null;
        if (player != null && (ph = getHistory(player)) != null) {
            ip = player.getAddress().getHostString();
            if (type.getSubType().name().startsWith("BAN")) {
                punish =
                        new Ban(-1, punisher, proof, type.getReason(), type.getSubType().name().contains("IP"),
                                bannedOn, type.getPunishTime() > 0 ? type.getPunishTime() + bannedOn : 0);
                player.disconnect(TextComponent.fromLegacyText(punish.getMessage()));
            } else {
                punish =
                        new Mute(-1, punisher, proof, type.getReason(), type.getSubType().name().contains("IP"),
                                bannedOn, type.getPunishTime() > 0 ? type.getPunishTime() + bannedOn : 0);
            }
        } else {
            CachedRowSet rs = Backend.getInstance().query("SELECT * FROM blackuser WHERE id = ?",
                    target.toLowerCase());
            if (type.getSubType().name().contains("IP") && rs == null) {
                return "§cO jogador " + target
                        + " não possui um IP registrado, não é possivel utilizar uma punição por IP.";
            }

            if (rs != null) {
                try {
                    ip = rs.getString("ip");
                    target = rs.getString("name");
                } catch (SQLException e) {
                    if (type.getSubType().name().contains("IP")) {
                        return "§cO jogador " + target
                                + " não possui um IP registrado, não é possivel punir por IP.";
                    } else {
                        return "§cFalha ao solicitar nome registrado do jogador " + target + ".";
                    }
                }
            }
        }

        if (type.getSubType().name().startsWith("BAN")) {
            boolean isTemp = type.getPunishTime() != 0;

            for (String bc : Bungee.getInstance().getConfig()
                    .getStringList(!isTemp ? "ban-broadcast" : "ban-broadcast-temp")) {
                ProxyServer.getInstance().broadcast(
                        TextComponent.fromLegacyText(bc.replace("&", "§").replace("%punisher%", punisher)
                                .replace("%target%", target).replace("%reason%", type.getReason())
                                .replace("%proof%", proof)
                                .replace("%expires%", Punish.getTimeUntil(type.getPunishTime() + bannedOn))));
            }
        } else {
            boolean isTemp = type.getPunishTime() != 0;

            for (String bc : Bungee.getInstance().getConfig()
                    .getStringList(!isTemp ? "mute-broadcast" : "muteip-broadcast")) {
                ProxyServer.getInstance().broadcast(
                        TextComponent.fromLegacyText(bc.replace("&", "§").replace("%punisher%", punisher)
                                .replace("%target%", target).replace("%reason%", type.getReason())
                                .replace("%proof%", proof)
                                .replace("%expires%", Punish.getTimeUntil(type.getPunishTime() + bannedOn))));
            }
        }

        if (type.getSubType() == PunishType.SubType.BANIP && ip != null) {
            for (ProxiedPlayer kick : ProxyServer.getInstance().getPlayers()) {
                if (kick.getAddress().getHostString().equals(ip)) {
                    if (punish == null) {
                        kick.disconnect(TextComponent.fromLegacyText("§cInternal error: 0x431"));
                    } else {
                        kick.disconnect(TextComponent.fromLegacyText(punish.getMessage()));
                    }
                }
            }
        }

        Backend.getInstance().execute(
                "INSERT INTO blacklist (identifier, punisher, type, proof, reason, bannedOn, expiresOn)"
                        + "VALUES (?, ?, ?, ?, ?, ?, ?)",
                type.getSubType().name().contains("IP") ? ip : target.toLowerCase(), punisher,
                type.getSubType().name().replace("IP", ""), proof, type.getReason(), bannedOn,
                type.getPunishTime() > 0 ? type.getPunishTime() + bannedOn : 0);

        if (type.getSubType().name().startsWith("MUTE") && punish != null) {
            if (type.getSubType().name().contains("IP")) {
                if (ip != null) {
                    for (PunishHistory ph2 : histories.values()) {
                        if (ph2.getPlayer() != null
                                && ph2.getPlayer().getAddress().getHostString().equals(ip)) {
                            ph2.listMutes().clear();
                            ph2.loadHistory();
                        }
                    }
                }
            } else if (ph != null) {
                ph.listMutes().clear();
                ph.loadHistory();
            }
        }

        return "";
    }

    public static PunishHistory loadHistory(PendingConnection player, String ip) {
        PunishHistory ph = new PunishHistory(player, ip);
        histories.put(player.getUniqueId(), ph);
        return ph;
    }

    public static PunishHistory unloadHistory(PendingConnection player) {
        return histories.remove(player.getUniqueId());
    }

    public static PunishHistory getHistory(ProxiedPlayer player) {
        return histories.get(player.getUniqueId());
    }

    public static Collection<PunishHistory> listHistories() {
        return ImmutableList.copyOf(histories.values());
    }
}
