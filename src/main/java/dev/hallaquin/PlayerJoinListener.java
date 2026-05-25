package dev.hallaquin;

import org.bukkit.Bukkit;
import org.bukkit.Registry;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.permissions.PermissionAttachmentInfo;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PlayerJoinListener implements Listener {

    // Expresión regular para capturar el atributo y el valor base.
    // Ejemplo: permissionattributes.generic_max_health.26 -> Grupo 1: generic_max_health, Grupo 2: 26
    private final Pattern permissionPattern = Pattern.compile("^permissionattributes\\.([a-z_]+)\\.([0-9.-]+)$");

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();

        // 1. Reestablecer todos los atributos al valor por defecto humano antes de aplicar cambios.
        resetBaseAttributes(player);

        // 2. Iterar sobre todos los permisos calculados del jugador.
        for (PermissionAttachmentInfo permInfo : player.getEffectivePermissions()) {
            String permission = permInfo.getPermission().toLowerCase();

            Matcher matcher = permissionPattern.matcher(permission);
            if (matcher.matches()) {
                String attributeName = matcher.group(1);
                String valueString = matcher.group(2);

                try {
                    double value = Double.parseDouble(valueString);

                    // Buscar el atributo en el Registro de Spigot.
                    Attribute attribute = null;
                    for (Attribute attr : Registry.ATTRIBUTE) {
                        if (attr.getKey().getKey().equalsIgnoreCase(attributeName)) {
                            attribute = attr;
                            break;
                        }
                    }

                    if (attribute != null) {
                        applyBaseAttribute(player, attribute, value);
                    } else {
                        player.getServer().getLogger().warning("Attribute not found in Registry: " + attributeName);
                    }

                } catch (NumberFormatException e) {
                    player.getServer().getLogger().warning("Invalid numeric format in permission: " + permission);
                }
            }
        }
    }

    private void applyBaseAttribute(Player player, Attribute attribute, double amount) {
        AttributeInstance instance = player.getAttribute(attribute);

        if (instance != null) {
            instance.setBaseValue(amount);
        }
    }

    private void resetBaseAttributes(Player player) {
        for (Attribute attribute : Registry.ATTRIBUTE) {
            AttributeInstance instance = player.getAttribute(attribute);

            if (instance != null) {
                instance.setBaseValue(instance.getDefaultValue());
            }

            // internal default is 0.7, but real default value is 0.10000000149011612
            if (attribute.getKey().getKey().equals("movement_speed")) {
                instance.setBaseValue(0.10000000149011612);
            }
        }
    }
}