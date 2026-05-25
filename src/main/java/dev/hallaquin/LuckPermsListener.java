package dev.hallaquin;

import net.luckperms.api.event.user.UserDataRecalculateEvent;
import net.luckperms.api.model.user.User;
import org.bukkit.Bukkit;
import org.bukkit.Registry;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.entity.Player;
import org.bukkit.permissions.PermissionAttachmentInfo;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class LuckPermsListener {

    private final JavaPlugin plugin;
    private final Pattern permissionPattern = Pattern.compile("^permissionattributes\\.([a-z_]+)\\.([0-9.-]+)$");

    public LuckPermsListener(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    public void onPermissionChange(UserDataRecalculateEvent event) {
        User user = event.getUser();
        UUID uuid = user.getUniqueId();

        Player player = Bukkit.getPlayer(uuid);
        if (player == null) {
            return;
        }

        Bukkit.getScheduler().runTask(plugin, () -> {
            updatePlayerAttributes(player);
        });
    }

    private void updatePlayerAttributes(Player player) {
        // reset to default attributes
        for (Attribute attribute : Registry.ATTRIBUTE) {
            AttributeInstance instance = player.getAttribute(attribute);
            if (instance != null) {
                String attributeKey = attribute.getKeyOrThrow().getKey();
                if (attributeKey.equals("movement_speed")) {
                    instance.setBaseValue(0.10000000149011612);
                } else {
                    instance.setBaseValue(instance.getDefaultValue());
                }
            }
        }

        for (PermissionAttachmentInfo permInfo : player.getEffectivePermissions()) {
            String permission = permInfo.getPermission().toLowerCase();

            Matcher matcher = permissionPattern.matcher(permission);
            if (matcher.matches()) {
                String attributeName = matcher.group(1);
                String valueString = matcher.group(2);

                try {
                    double value = Double.parseDouble(valueString);

                    Attribute attribute = null;
                    for (Attribute attr : Registry.ATTRIBUTE) {
                        if (attr.getKeyOrThrow().getKey().equalsIgnoreCase(attributeName)) {
                            attribute = attr;
                            break;
                        }
                    }

                    if (attribute != null) {
                        AttributeInstance instance = player.getAttribute(attribute);
                        if (instance != null) {
                            instance.setBaseValue(value);

                            if (attribute.getKeyOrThrow().getKey().equals("max_health")) {
                                player.setHealth(instance.getBaseValue());
                            }
                        }
                    }

                } catch (NumberFormatException e) {
                    Bukkit.getLogger().warning("Wrong number format on " + permission);
                }
            }
        }
    }
}