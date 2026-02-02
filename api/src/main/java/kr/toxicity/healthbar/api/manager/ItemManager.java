package kr.toxicity.healthbar.api.manager;

import kr.toxicity.healthbar.api.item.HealthBarItem;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface ItemManager {
    @Nullable
    HealthBarItem item(@NotNull String name);

    @Nullable
    HealthBarItem itemByPercentage(double percentage);

    @NotNull
    ItemStack getIconForPercentage(double percentage);
}
