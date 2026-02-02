package kr.toxicity.healthbar.api.item;

import kr.toxicity.healthbar.api.configuration.HealthBarConfiguration;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

public interface HealthBarItem extends HealthBarConfiguration {
    @NotNull
    ItemStack itemStack();

    @NotNull
    String name();
}
