package kr.toxicity.healthbar.api.renderer;

import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

public interface ItemRenderer extends Renderer {
    @NotNull
    ItemStack getItem();

    int layer();

    double scaleX();

    double scaleY();

    double scaleZ();
}
