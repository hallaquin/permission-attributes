# PermissionAttributes
Minecraft Plugin that creates permission nodes to modify attributes of players and group members.

## Usage
- The syntaxis of the permission nodes is:
```permissionattributes.{player_attribute}.{value}```

- Create any permission node in a group or user, using your permission manager (eg. LuckPerms):
```/lp group wizard permission set permissionattributes.max_health.16```

## Features
- Hooking into LuckPerms Event Bus, making live attribute changes.
- Easy to use permission structure.
