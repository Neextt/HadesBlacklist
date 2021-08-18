package net.lxsthw.redehades.blacklist.commands;

import net.lxsthw.redehades.blacklist.Bungee;
import net.lxsthw.redehades.blacklist.profile.HistoryLoader;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;

public class UnPunishCommand extends Commands {

    public UnPunishCommand() {
        super("unblacklist");
    }

    public void execute(CommandSender sender, String[] args) {
        if (!sender.hasPermission("group.master")) {
            sender.sendMessage(TextComponent.fromLegacyText("§cVocê precisa estar no grupo §6Master §cpara executar este comando."));
            return;
        }

        if (sender instanceof ProxiedPlayer
                && Bungee.getInstance().getConfig().getStringList("servers")
                .contains(((ProxiedPlayer) sender).getServer().getInfo().getName())) {
            return;
        }

        if (args.length == 0) {
            sender.sendMessage(TextComponent.fromLegacyText(" §cUtilize /blacklist <jogador>"));
            return;
        }

        String punisher = sender instanceof ProxiedPlayer ? ((ProxiedPlayer) sender).getName() : "CONSOLE";
        String reason = "Nenhum";
        if (args.length > 1) {
            reason = "";
            for (int i = 1; i < args.length; i++) {
                reason += args[i] + (i + 1 == args.length ? "" : " ");
            }
        }

        String msg = HistoryLoader.unpunishAll(args[0], punisher, reason);
        if (!msg.isEmpty()) {
            sender.sendMessage(TextComponent.fromLegacyText(msg));
        }

        return;
    }
}

