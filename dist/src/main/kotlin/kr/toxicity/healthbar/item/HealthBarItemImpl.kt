package kr.toxicity.healthbar.item

import kr.toxicity.healthbar.api.item.HealthBarItem
import org.bukkit.configuration.ConfigurationSection
import org.bukkit.inventory.ItemStack

class HealthBarItemImpl(
    private val name: String,
    private val path: String,
    section: ConfigurationSection
): HealthBarItem {
    private val item = section.getItemStack("item").let { stack ->
        stack?.clone()?.apply {
            val meta = itemMeta
            meta?.setDisplayName("")
            itemMeta = meta
        } ?: throw RuntimeException("Item not found in $path")
    }
    private val percentage = section.getDouble("percentage", -1.0)
    private val minPercentage = section.getDouble("min-percentage", percentage)
    private val maxPercentage = section.getDouble("max-percentage", if (percentage < 0) 1.0 else percentage + (1.0 / 20.0))

    override fun itemStack(): ItemStack = item.clone()
    override fun name(): String = name
    override fun path(): String = path

    fun minPercentage(): Double = minPercentage.coerceIn(0.0, 1.0)
    fun maxPercentage(): Double = maxPercentage.coerceIn(0.0, 1.0)

    fun matches(healthPercentage: Double): Boolean {
        return healthPercentage >= minPercentage() && healthPercentage < maxPercentage()
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as HealthBarItemImpl
        return name == other.name
    }

    override fun hashCode(): Int {
        return name.hashCode()
    }
}
