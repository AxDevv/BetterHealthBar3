package kr.toxicity.healthbar.api.nms;

import org.bukkit.Location;
import org.bukkit.util.Vector;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

public interface VirtualItemDisplay {
    void spawn(@NotNull PacketBundler bundler);
    void update(@NotNull PacketBundler bundler);
    void teleport(@NotNull Location location);
    void remove(@NotNull PacketBundler bundler);
    void item(@NotNull ItemStack itemStack);
    void transformation(@NotNull Vector location, @NotNull Vector scale, float leftRotation, float rightRotation);
}
