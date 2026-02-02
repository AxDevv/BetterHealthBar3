package kr.toxicity.healthbar.api.layout;

import kr.toxicity.healthbar.api.event.HealthBarCreateEvent;
import kr.toxicity.healthbar.api.item.HealthBarItem;
import kr.toxicity.healthbar.api.renderer.ItemRenderer;
import org.jetbrains.annotations.NotNull;

public interface ItemLayout extends Layout {
    @NotNull
    HealthBarItem item();

    @NotNull
    ItemRenderer createItemRenderer(@NotNull HealthBarCreateEvent entity);

    int duration();
}
