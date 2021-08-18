package net.lxsthw.redehades.blacklist.commands;

import net.lxsthw.redehades.blacklist.Bungee;
import net.lxsthw.redehades.blacklist.profile.HistoryLoader;
import net.lxsthw.redehades.blacklist.types.Punish;
import net.lxsthw.redehades.blacklist.types.PunishType;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.HoverEvent.Action;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;

public class PunishCommand extends Commands {

    public PunishCommand() {
        super("blacklist", "listanegra");
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (!(sender instanceof ProxiedPlayer)) {
            sender.sendMessage(
                    TextComponent.fromLegacyText("§cO comando /blacklist funciona apenas para jogadores."));
            return;
        }

        ProxiedPlayer player = (ProxiedPlayer) sender;
        if (player.hasPermission("blacklist.cmd.punir")) {
            if (Bungee.getInstance().getConfig().getStringList("servers").contains(player.getServer().getInfo().getName())) {
                return;
            }

            if (args.length <= 1) {
                player.sendMessage(TextComponent.fromLegacyText("§cUtilize /blacklist <jogador> <prova>"));
                return;
            }

            String target = args[0];
            String proof = args[1];
            if (args.length > 2) {
                String typeName = "";
                for (int i = 2; i < args.length; i++) {
                    typeName += args[i] + (i + 1 == args.length ? "" : " ");
                }

                PunishType type = PunishType.getType(typeName);
                if (type == null) {
                    player.sendMessage(TextComponent.fromLegacyText("§cTipo de motivo invalido!"));
                    return;
                }

                if (!type.hasPermission(player)) {
                    player.sendMessage(TextComponent.fromLegacyText("§cTipo de punição invalida!"));
                    return;
                }

                String msg = HistoryLoader.insertNewPunish(target, player.getName(), proof, type);
                if (!msg.isEmpty()) {
                    player.sendMessage(TextComponent.fromLegacyText(msg));
                }

                return;
            }

            TextComponent all = new TextComponent("\n§eLista de motivos: \n");
            for (PunishType type : PunishType.listTypes()) {
                if (!type.hasPermission(player)) {
                    continue;
                }

                TextComponent extra = new TextComponent("§l§e▸ §r§f " + type.getName());
                extra.setHoverEvent(new HoverEvent(Action.SHOW_TEXT,
                        TextComponent.fromLegacyText("§fDuração: §7" + Punish.getTime(type.getPunishTime())
                                + "\n§fTipo de punição: §7" + type.getSubType())));
                extra.setClickEvent(new ClickEvent(net.md_5.bungee.api.chat.ClickEvent.Action.RUN_COMMAND,
                        "/blacklist " + target + " " + proof + " " + type.getName()));
                all.addExtra(extra);
                all.addExtra(new TextComponent("\n "));
            }

            player.sendMessage(all);
            all = null;
        } else {
            player.sendMessage(
                    TextComponent.fromLegacyText("§cVocê não possui permissão para utilizar este comando."));
        }
    }
}
