package net.lxsthw.redehades.blacklist.types;

import net.lxsthw.redehades.blacklist.Bungee;

import java.text.SimpleDateFormat;

public class Ban extends Punish {

    private String message;
    public static final SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy - hh:mm");

    public Ban(int id, String punisher, String proof, String reason, boolean ip, long bannedOn, long expiresOn) {
        super(id, punisher, proof, reason, ip, bannedOn, expiresOn);
        boolean isTemp = getExpiresOn() != 0;

        this.message = Bungee.getInstance().getConfig().getString(isTemp ? "ban-kick-temp" : "ban-kick")
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

