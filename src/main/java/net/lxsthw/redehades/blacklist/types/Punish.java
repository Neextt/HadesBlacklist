package net.lxsthw.redehades.blacklist.types;

public abstract class Punish {

    private int id;
    private String proof;
    private String punisher;
    private String reason;
    private boolean ip;
    private long bannedOn;
    private long expiresOn;

    public Punish(int id, String punisher, String proof, String reason, boolean ip, long bannedOn, long expiresOn) {
        this.id = id;
        this.proof = proof;
        this.punisher = punisher;
        this.reason = reason;
        this.ip = ip;
        this.bannedOn = bannedOn;
        this.expiresOn = expiresOn;
    }

    public abstract String getMessage();

    public int getId() {
        return id;
    }

    public String getPunisher() {
        return punisher;
    }

    public String getProof() {
        return proof;
    }

    public String getReason() {
        return reason;
    }

    public long getBannedOn() {
        return bannedOn;
    }

    public long getExpiresOn() {
        return expiresOn;
    }

    public boolean isIp() {
        return ip;
    }

    public boolean isExpired() {
        return expiresOn != 0 && expiresOn <= System.currentTimeMillis();
    }

    public static String getTimeUntil(long epoch) {
        epoch -= System.currentTimeMillis();
        return getTime(epoch);
    }

    public static String getTime(long epoch) {
        long ms = epoch / 1000;
        if (ms <= 0) {
            return "Permanente";
        }

        String result = "";
        long days = ms / 86400;
        if (days > 0) {
            result += days + " dia" + (days > 1 ? "s " : " ");
            ms -= days * 86400;
        }
        long hours = ms / 3600;
        if (hours > 0) {
            result += hours + " hora" + (hours > 1 ? "s " : " ");
            ms -= hours * 3600;
        }
        long minutes = ms / 60;
        if (minutes > 0) {
            result += minutes + " minuto" + (minutes > 1 ? "s " : " ");
            ms -= minutes * 60;
        }
        if (ms > 0) {
            result += ms + " segundo" + (ms > 1 ? "s " : " ");
            ms -= ms;
        }

        return result.substring(0, result.length() - 1);
    }
}
