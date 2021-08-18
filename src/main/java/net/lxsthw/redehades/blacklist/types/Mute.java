package net.lxsthw.redehades.blacklist.types;

import net.lxsthw.redehades.blacklist.Bungee;

import static net.lxsthw.redehades.blacklist.types.Ban.sdf;

public class Mute extends Punish {

    private String message;

    public Mute(int id, String punisher, String proof, String reason, boolean ip, long bannedOn,
                long expiresOn) {
        super(id, punisher, proof, reason, ip, bannedOn, expiresOn);

        boolean isTemp = getExpiresOn() != 0;

        this.message = Bungee.getInstance().getConfig().getString(isTemp ? "mute-msg-temp" : "mute-msg")
                .replace("\\n", "\n").replace("&", "§");
        this.message =
                this.message.replace("%id%", "" + (id > -1 ? id : "?")).replace("%punisher%", punisher)
                        .replace("%reason%", reason).replace("%proof%", proof).replace("%date%", sdf.format(bannedOn).replace("-", "�s"));
    }

    @Override
    public String getMessage() {
        return this.message.replace("%expires%", Punish.getTimeUntil(getExpiresOn()));
    }
}

