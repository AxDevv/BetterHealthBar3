package kr.toxicity.healthbar.layout

import kr.toxicity.healthbar.api.event.HealthBarCreateEvent
import kr.toxicity.healthbar.api.item.HealthBarItem
import kr.toxicity.healthbar.api.layout.ItemLayout
import kr.toxicity.healthbar.api.renderer.ItemRenderer
import kr.toxicity.healthbar.manager.ItemManagerImpl
import kr.toxicity.healthbar.util.ifNull
import kr.toxicity.healthbar.util.toCondition
import org.bukkit.configuration.ConfigurationSection

class ItemLayoutImpl(
    private val parent: LayoutGroupImpl,
    private val name: String,
    layer: Int,
    section: ConfigurationSection
): ItemLayout, LayoutImpl(layer, section) {
    private val itemRef: HealthBarItem = section.getString("item")?.let { itemName ->
        ItemManagerImpl.item(itemName)
    }?.ifNull { "Unable to find this item: ${section.getString("item")}" } ?: throw RuntimeException("Unable to find 'item' configuration.")
    private val duration = section.getInt("duration", -1)
    private val scaleX = section.getDouble("scale-x", 1.0)
    private val scaleY = section.getDouble("scale-y", 1.0)
    private val scaleZ = section.getDouble("scale-z", 1.0)
    private val leftRotation = section.getDouble("left-rotation", 0.0).toFloat()
    private val rightRotation = section.getDouble("right-rotation", 0.0).toFloat()
    private val condition = section.toCondition()

    override fun item(): HealthBarItem = itemRef

    override fun duration(): Int = duration

    override fun createItemRenderer(pair: HealthBarCreateEvent): ItemRenderer {
        return Renderer(pair, itemRef)
    }

    private inner class Renderer(
        private val pair: HealthBarCreateEvent,
        private val healthBarItem: HealthBarItem
    ): ItemRenderer {
        private var d = 0
        private var currentItem = healthBarItem.itemStack()

        override fun hasNext(): Boolean {
            return duration < 0 || ++d <= duration
        }

        override fun canRender(): Boolean {
            val result = condition().apply(pair)
            if (result) {
                val maxHealth = pair.entity.entity().getAttribute(
                    org.bukkit.attribute.Attribute.MAX_HEALTH
                )?.value ?: 1.0
                val healthPercentage = pair.entity.entity().health / maxHealth
                currentItem = ItemManagerImpl.getIconForPercentage(healthPercentage)
            }
            return result
        }

        override fun layer(): Int = this@ItemLayoutImpl.layer()

        override fun getItem() = currentItem.clone()

        override fun scaleX(): Double = this@ItemLayoutImpl.scaleX * scale()
        override fun scaleY(): Double = this@ItemLayoutImpl.scaleY * scale()
        override fun scaleZ(): Double = this@ItemLayoutImpl.scaleZ * scale()
    }
}
