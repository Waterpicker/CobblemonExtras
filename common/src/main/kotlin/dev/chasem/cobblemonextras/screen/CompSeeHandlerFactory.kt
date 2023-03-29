package dev.chasem.cobblemonextras.screen

import com.cobblemon.mod.common.Cobblemon.storage
import com.cobblemon.mod.common.api.storage.NoPokemonStoreException
import com.cobblemon.mod.common.api.storage.pc.PCStore
import dev.chasem.cobblemonextras.util.ItemBuilder
import dev.chasem.cobblemonextras.util.PokemonUtility
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.entity.player.PlayerInventory
import net.minecraft.inventory.Inventory
import net.minecraft.inventory.SimpleInventory
import net.minecraft.item.ItemStack
import net.minecraft.item.Items
import net.minecraft.screen.GenericContainerScreenHandler
import net.minecraft.screen.NamedScreenHandlerFactory
import net.minecraft.screen.ScreenHandler
import net.minecraft.screen.ScreenHandlerType
import net.minecraft.screen.slot.Slot
import net.minecraft.screen.slot.SlotActionType
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.text.Text
import net.minecraft.util.Formatting

class CompSeeHandlerFactory : NamedScreenHandlerFactory {
    private var toView: ServerPlayerEntity? = null
    private var boxNumber = 0

    constructor(toView: ServerPlayerEntity?) {
        this.toView = toView
    }

    constructor(toView: ServerPlayerEntity?, boxNumber: Int) {
        this.toView = toView
        this.boxNumber = boxNumber
    }

    constructor()

    override fun getDisplayName(): Text {
        return Text.of(toView!!.entityName + " PC : Box " + (boxNumber + 1))
    }

    fun rows(): Int {
        return 5
    }

    private fun size(): Int {
        return rows() * 9
    }

    override fun createMenu(syncId: Int, inv: PlayerInventory, player: PlayerEntity): ScreenHandler? {
        val inventory = SimpleInventory(size())
        for (i in 0 until size()) {
            inventory.setStack(i, ItemStack(Items.GRAY_STAINED_GLASS_PANE).setCustomName(Text.of(" ")))
        }
        var serverPlayer: ServerPlayerEntity? = player as ServerPlayerEntity
        if (toView != null) {
            serverPlayer = toView
        } else {
            toView = serverPlayer
        }
        var pcStore: PCStore? = null
        pcStore = try {
            storage.getPC(serverPlayer!!.uuid)
        } catch (e: NoPokemonStoreException) {
            return null
        }
        val box = pcStore?.boxes?.get(boxNumber) ?: return null
        for (i in 0..29) {
            val pokemon = box[i]
            val row = Math.floor(i.toDouble() / 6.0)
            val index = i % 6
            if (pokemon != null) {
                val item = PokemonUtility.pokemonToItem(pokemon)
                inventory.setStack((row * 9).toInt() + index, item)
            } else {
                inventory.setStack(
                    (row * 9).toInt() + index, ItemStack(Items.RED_STAINED_GLASS_PANE).setCustomName(
                        Text.literal("Empty").formatted(
                            Formatting.GRAY
                        )
                    )
                )
            }
        }
        if (boxNumber < 29) {
            inventory.setStack(
                44,
                ItemBuilder(Items.ARROW).hideAdditional().setCustomName(Text.literal("Next Box")).build()
            )
        }
        if (boxNumber > 0) {
            inventory.setStack(
                42,
                ItemBuilder(Items.ARROW).hideAdditional().setCustomName(Text.literal("Previous Box")).build()
            )
        }
        return object : GenericContainerScreenHandler(ScreenHandlerType.GENERIC_9X5, syncId, inv, inventory, rows()) {
            override fun onSlotClick(slotIndex: Int, button: Int, actionType: SlotActionType, player: PlayerEntity) {
                if (slotIndex == 44 && boxNumber < 29) {
                    player.openHandledScreen(CompSeeHandlerFactory(toView, boxNumber + 1))
                }
                if (slotIndex == 42 && boxNumber > 0) {
                    player.openHandledScreen(CompSeeHandlerFactory(toView, boxNumber - 1))
                }
            }

            override fun transferSlot(player: PlayerEntity, index: Int): ItemStack {
                return ItemStack.EMPTY
            }

            override fun canInsertIntoSlot(slot: Slot): Boolean {
                return false
            }

            override fun dropInventory(player: PlayerEntity, inventory: Inventory) {}
        }
    }
}