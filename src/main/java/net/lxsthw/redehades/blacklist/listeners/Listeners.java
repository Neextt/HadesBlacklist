package net.lxsthw.redehades.blacklist.listeners;

import net.lxsthw.redehades.blacklist.Bungee;
import net.lxsthw.redehades.blacklist.profile.HistoryLoader;
import net.lxsthw.redehades.blacklist.profile.PunishHistory;
import net.lxsthw.redehades.blacklist.types.Punish;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.PendingConnection;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.ChatEvent;
import net.md_5.bungee.api.event.LoginEvent;
import net.md_5.bungee.api.event.PlayerDisconnectEvent;
import net.md_5.bungee.api.event.ServerConnectedEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;
import net.md_5.bungee.event.EventPriority;

public class Listeners implements Listener {

    public static void makeListeners() {
        ProxyServer.getInstance().getPluginManager().registerListener(Bungee.getInstance(),
                new Listeners());
    }

    @EventHandler
    public void onPlayerQuit(PlayerDisconnectEvent evt) {
        HistoryLoader.unloadHistory(evt.getPlayer().getPendingConnection());
    }

    @EventHandler
    public void onPlayerLogin(LoginEvent evt) {
        PendingConnection player = evt.getConnection();

        PunishHistory ph =
                HistoryLoader.loadHistory(player, player.getAddress().getAddress().getHostAddress());
        if (ph != null) {
            for (Punish punish : ph.listBans()) {
                if (!punish.isExpired()) {
                    evt.setCancelled(true);
                    evt.setCancelReason(TextComponent.fromLegacyText(punish.getMessage()));
                    break;
                }
            }
        }

        if (evt.isCancelled()) {
            HistoryLoader.unloadHistory(player);
        }

        ph = null;
        player = null;
    }

    @EventHandler
    public void onPlayerJoin(ServerConnectedEvent evt) {
        ProxiedPlayer player = evt.getPlayer();
        PunishHistory ph = HistoryLoader.getHistory(player);
        if (ph != null) {
            ph.updateIP();
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onAsyncPlayerChat(ChatEvent evt) {
        if (!(evt.getSender() instanceof ProxiedPlayer)) {
            return;
        }

        ProxiedPlayer player = (ProxiedPlayer) evt.getSender();
        PunishHistory ph = HistoryLoader.getHistory(player);
        if (ph != null) {
            if (evt.isCommand()) {
                String command = evt.getMessage().split(" ")[0].replace("/", "").toLowerCase();
                if (command.equals("tell")) {
                    for (Punish punish : ph.listMutes()) {
                        if (!punish.isExpired()) {
                            evt.setCancelled(true);
                            player.sendMessage(TextComponent.fromLegacyText(punish.getMessage()));
                            break;
                        }
                    }
                }
            } else {
                for (Punish punish : ph.listMutes()) {
                    if (!punish.isExpired()) {
                        evt.setCancelled(true);
                        player.sendMessage(TextComponent.fromLegacyText(punish.getMessage()));
                        break;
                    }
                }
            }
        }

        ph = null;
        player = null;
    }
}

