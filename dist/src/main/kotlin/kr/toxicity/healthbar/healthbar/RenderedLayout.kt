package kr.toxicity.healthbar.healthbar

import kr.toxicity.healthbar.api.healthbar.GroupIndex
import kr.toxicity.healthbar.api.event.HealthBarCreateEvent
import kr.toxicity.healthbar.api.layout.LayoutGroup
import kr.toxicity.healthbar.api.nms.PacketBundler
import kr.toxicity.healthbar.api.nms.VirtualTextDisplay
import kr.toxicity.healthbar.api.nms.VirtualItemDisplay
import kr.toxicity.healthbar.api.renderer.ImageRenderer
import kr.toxicity.healthbar.api.renderer.ItemRenderer
import kr.toxicity.healthbar.api.renderer.PixelRenderer
import kr.toxicity.healthbar.util.*
import org.bukkit.Location
import org.bukkit.util.Vector

class RenderedLayout(group: LayoutGroup, pair: HealthBarCreateEvent) {
    val group = group.group()
    val images = group.images().map {
        it.createImageRenderer(pair)
    }.toMutableList()
    val texts = group.texts().map {
        it.createRenderer(pair)
    }.toMutableList()
    val items = group.items().map {
        it.createItemRenderer(pair)
    }.toMutableList()

    fun createPool(data: HealthBarCreateEvent, indexes: Map<String, GroupIndex>) = RenderedEntityPool(data, indexes)

    inner class RenderedEntityPool(
        private val data: HealthBarCreateEvent,
        private val indexes: Map<String, GroupIndex>
    ) {
        private val entities = (images.map {
            RenderedEntity(it)
        } + texts.map {
            RenderedEntity(it)
        }).toMutableList()
        private val itemEntities = items.map {
            RenderedItem(it)
        }.toMutableList()

        fun displays() = entities.mapNotNull {
            it.entity
        }

        var max = 0

        fun update(bundler: PacketBundler): Boolean {
            entities.removeIf {
                !it.hasNext()
            }
            itemEntities.removeIf {
                !it.hasNext()
            }
            val imageMap = entities.filter {
                it.canBeRendered(bundler)
            }
            val itemMap = itemEntities.filter {
                it.canBeRendered(bundler)
            }
            if (imageMap.isEmpty() && itemMap.isEmpty()) return false
            val count = group?.let { s ->
                indexes[s]
            }?.next() ?: 0
            max = 0
            imageMap.forEach {
                it.update(count)
            }
            itemMap.forEach {
                it.update()
            }
            return true
        }

        fun create(loc: Location, max: Int, bundler: PacketBundler) {
            val imageMap = entities.filter {
                it.canBeRendered(bundler)
            }
            val itemMap = itemEntities.filter {
                it.canBeRendered(bundler)
            }
            imageMap.forEach {
                it.create(max, loc, bundler)
            }
            itemMap.forEach {
                it.create(loc, bundler)
            }
        }

        fun remove(bundler: PacketBundler) {
            entities.forEach {
                it.remove(bundler)
            }
            itemEntities.forEach {
                it.remove(bundler)
            }
        }

        private inner class RenderedEntity(
            private val renderer: PixelRenderer
        ) {
            var entity: VirtualTextDisplay? = null
            var comp = EMPTY_PIXEL_COMPONENT

            fun remove(bundler: PacketBundler) {
                entity?.remove(bundler)
            }

            fun canBeRendered(bundler: PacketBundler): Boolean {
                val result = renderer.canRender()
                if (!result) {
                    remove(bundler)
                    entity = null
                }
                return result
            }

            fun hasNext() = renderer.hasNext()

            fun create(max: Int, loc: Location, bundler: PacketBundler) {
                val length = comp.pixel + comp.component.width
                val finalComp = comp.pixel.toSpaceComponent() + comp.component + (-length + max).toSpaceComponent() + NEW_LAYER
                entity = entity?.apply {
                    teleport(loc)
                    text(finalComp.component.build())
                    update(bundler)
                } ?: data.createEntity(loc, finalComp, renderer.layer()).apply {
                    spawn(bundler)
                }
            }

            fun update(count: Int) {
                comp = renderer.render(count)
                val length = comp.pixel + comp.component.width
                if (renderer is ImageRenderer && renderer.isBackground && max < length) max = length
            }
        }

        private inner class RenderedItem(
            private val renderer: ItemRenderer
        ) {
            var entity: VirtualItemDisplay? = null

            fun remove(bundler: PacketBundler) {
                entity?.remove(bundler)
            }

            fun canBeRendered(bundler: PacketBundler): Boolean {
                val result = renderer.canRender()
                if (!result) {
                    remove(bundler)
                    entity = null
                }
                return result
            }

            fun hasNext() = renderer.hasNext()

            fun create(loc: Location, bundler: PacketBundler) {
                val item = renderer.getItem()
                val existingEntity = entity
                entity = existingEntity?.apply {
                    teleport(loc)
                    item(item)
                    update(bundler)
                } ?: PLUGIN.nms().createItemDisplay(loc, item).apply {
                    transformation(Vector(0, 0, 0), Vector(renderer.scaleX(), renderer.scaleY(), renderer.scaleZ()).multiply(data.healthBar.scale()), 0f, 0f)
                    spawn(bundler)
                }
            }

            fun update() {
                entity?.item(renderer.getItem())
            }
        }
    }
}