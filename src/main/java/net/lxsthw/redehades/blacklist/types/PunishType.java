package net.lxsthw.redehades.blacklist.types;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import com.google.common.collect.ImmutableList;
import net.lxsthw.redehades.blacklist.Bungee;
import net.lxsthw.redehades.blacklist.utils.FileUtils;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.config.Configuration;
import net.md_5.bungee.config.YamlConfiguration;

public class PunishType {

    private String name;
    private SubType subType;
    private PunishTime punishTime;
    private String reason;
    private String permission;

    public PunishType(String name, String permission, SubType type, PunishTime time, String reason) {
        this.name = name;
        this.subType = type;
        this.punishTime = time;
        this.reason = reason;
        this.permission = permission;
        types.put(name.toLowerCase(), this);
    }

    public String getName() {
        return name;
    }

    public SubType getSubType() {
        return subType;
    }

    public String getReason() {
        return reason;
    }

    public long getPunishTime() {
        return punishTime.getTime();
    }

    public boolean hasPermission(ProxiedPlayer player) {
        return permission.isEmpty() || player.hasPermission(permission);
    }

    private static Map<String, PunishType> types = new LinkedHashMap<>();

    public static void makeTypes() {
        File file = new File("plugins/HadesBlacklist/blacklist.yml");
        if (!file.exists()) {
            file.getParentFile().mkdirs();
            FileUtils.copyFile(Bungee.getInstance().getResourceAsStream("blacklist.yml"), file);
        }

        try {
            Configuration config = YamlConfiguration.getProvider(YamlConfiguration.class).load(new InputStreamReader(new FileInputStream(file), "UTF-8"));

            for (String key : config.getSection("types").getKeys()) {
                try {
                    String reason = config.getString("types." + key + ".reason");
                    if (reason == null) {
                        continue;
                    }

                    SubType type = SubType.from(config.getString("types." + key + ".type"));
                    if (type == null) {
                        continue;
                    }

                    PunishTime time = PunishTime.parsePunishTime(config.getString("types." + key + ".time"));
                    if (time == null) {
                        continue;
                    }

                    new PunishType(key, config.getString("types." + key + ".permission", ""), type, time, reason);
                } catch (Exception e) {
                }
            }
        } catch (UnsupportedEncodingException e) {
        } catch (FileNotFoundException e) {
        }
    }

    public static PunishType getType(String name) {
        return types.get(name.toLowerCase());
    }

    public static List<PunishType> listTypes() {
        return ImmutableList.copyOf(types.values());
    }

    public static enum SubType {
        BAN, BANIP, MUTE, MUTEIP;

        public static SubType from(String name) {
            for (SubType st : values()) {
                if (st.name().equals(name.toUpperCase())) {
                    return st;
                }
            }

            return null;
        }

        @Override
        public String toString() {
            return name().toString().substring(0, 1).toUpperCase()
                    + name().toString().substring(1).toLowerCase();
        }
    }

    public static class PunishTime {

        private long time;

        public PunishTime(long time) {
            this.time = time;
        }

        public long getTime() {
            return time;
        }

        public static PunishTime parsePunishTime(String parse) {
            if (parse.equalsIgnoreCase("PERMANENTE")) {
                return new PunishTime(0);
            }

            String[] split = parse.split(":");
            if (split.length == 2) {
                int modifier = 0;
                String arg = split[1].toLowerCase();

                if (arg.startsWith("hora")) {
                    modifier = 3600;
                } else if (arg.startsWith("minuto")) {
                    modifier = 60;
                } else if (arg.startsWith("segundo")) {
                    modifier = 1;
                } else if (arg.startsWith("semana")) {
                    modifier = 604800;
                } else if (arg.startsWith("dia")) {
                    modifier = 86400;
                } else if (arg.startsWith("ano")) {
                    modifier = 31449600;
                } else if (arg.startsWith("mes")) {
                    modifier = 2620800;
                }

                double time = 0;
                try {
                    time = Double.parseDouble(split[0]);
                } catch (NumberFormatException e) {
                }
                long timeLong = (long) (modifier * time) * 1000;
                return new PunishTime(timeLong);
            }

            return null;
        }
    }
}

