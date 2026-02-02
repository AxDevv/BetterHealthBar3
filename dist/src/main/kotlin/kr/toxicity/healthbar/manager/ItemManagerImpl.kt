package kr.toxicity.healthbar.manager

import kr.toxicity.healthbar.api.item.HealthBarItem
import kr.toxicity.healthbar.api.manager.ItemManager
import kr.toxicity.healthbar.item.HealthBarItemImpl
import kr.toxicity.healthbar.pack.PackResource
import kr.toxicity.healthbar.util.forEachAllYaml
import kr.toxicity.healthbar.util.putSync
import kr.toxicity.healthbar.util.runWithHandleException
import kr.toxicity.healthbar.util.subFolder
import org.bukkit.Material
import org.bukkit.inventory.ItemStack
import java.util.concurrent.ConcurrentHashMap

object ItemManagerImpl : ItemManager, BetterHealthBerManager {

    private val itemMap = ConcurrentHashMap<String, HealthBarItemImpl>()
    private val percentageList = mutableListOf<HealthBarItemImpl>()

    override fun item(name: String): HealthBarItem? {
        return itemMap[name]
    }

    fun itemImpl(name: String): HealthBarItemImpl? {
        return itemMap[name]
    }

    override fun itemByPercentage(percentage: Double): HealthBarItem? {
        val clamped = percentage.coerceIn(0.0, 1.0)
        return percentageList.firstOrNull { it.matches(clamped) }
    }

    override fun getIconForPercentage(percentage: Double): ItemStack {
        return itemByPercentage(percentage)?.itemStack() ?: fallbackItem(percentage)
    }

    private fun fallbackItem(percentage: Double): ItemStack {
        return when {
            percentage >= 1.0 -> ItemStack(Material.LIME_WOOL)
            percentage >= 0.75 -> ItemStack(Material.GREEN_WOOL)
            percentage >= 0.5 -> ItemStack(Material.YELLOW_WOOL)
            percentage >= 0.25 -> ItemStack(Material.ORANGE_WOOL)
            else -> ItemStack(Material.RED_WOOL)
        }
    }

    override fun start() {
    }

    override fun reload(resource: PackResource) {
        itemMap.clear()
        percentageList.clear()

        resource.dataFolder.subFolder("items").forEachAllYaml { file, s, configurationSection ->
            runWithHandleException("Unable to load this item: $s in ${file.path}") {
                val item = HealthBarItemImpl(
                    s,
                    file.path,
                    configurationSection
                )
                itemMap.putSync("item", s) {
                    item
                }
            }
        }

        percentageList.addAll(itemMap.values.sortedBy { it.minPercentage() })
    }
}
