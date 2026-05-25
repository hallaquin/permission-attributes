package dev.hallaquin;

import dev.hallaquin.LuckPermsListener;
import dev.hallaquin.PlayerJoinListener;
import net.luckperms.api.LuckPerms;
import net.luckperms.api.event.EventBus;
import net.luckperms.api.event.user.UserDataRecalculateEvent;
import org.bukkit.Bukkit;
import org.bukkit.Registry;
import org.bukkit.attribute.Attribute;
import org.bukkit.permissions.Permission;
import org.bukkit.permissions.PermissionDefault;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

public final class PermissionAttributes extends JavaPlugin {

    @Override
    public void onEnable() {
        registerAttributesWithSpigot();
        getServer().getPluginManager().registerEvents(new PlayerJoinListener(), this);

        RegisteredServiceProvider<LuckPerms> provider = Bukkit.getServicesManager().getRegistration(LuckPerms.class);
        if (provider != null) {
            LuckPerms luckPerms = provider.getProvider();

            EventBus eventBus = luckPerms.getEventBus();
            eventBus.subscribe(this, UserDataRecalculateEvent.class, new LuckPermsListener(this)::onPermissionChange);

            Bukkit.getLogger().info("Successfully hooked into LuckPerms Event Bus.");
        } else {
            Bukkit.getLogger().severe("LuckPerms API not found! Dynamic permission updates will not work.");
        }

        Bukkit.getLogger().info("PermissionAttributes has been successfully enabled.");
    }
    private void registerAttributesWithSpigot() {
        PluginManager pm = getServer().getPluginManager();
        int registeredCount = 0;

        for (Attribute attribute : Registry.ATTRIBUTE) {
            String attributeName = attribute.getKeyOrThrow().getKey();
            String permissionNode = "permissionattributes." + attributeName;

            if (pm.getPermission(permissionNode) == null) {
                Permission attributePermission = new Permission(
                        permissionNode,
                        "Allows dynamic base modification for the " + attributeName + " attribute.",
                        PermissionDefault.OP
                );

                pm.addPermission(attributePermission);
                registeredCount++;
            }
        }

        Bukkit.getLogger().info("Registered " + registeredCount + " dynamic attribute permissions into Spigot's PluginManager.");
    }
    @Override
    public void onDisable() {
        Bukkit.getLogger().info("PermissionAttributes has been disabled.");
    }
}