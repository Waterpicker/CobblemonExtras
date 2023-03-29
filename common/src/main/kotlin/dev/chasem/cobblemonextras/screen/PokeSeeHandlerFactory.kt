package dev.chasem.cobblemonextras.screen

import com.cobblemon.mod.common.Cobblemon.storage
import com.cobblemon.mod.common.entity.pokemon.PokemonEntity
import dev.chasem.cobblemonextras.util.PokemonUtility.pokemonToItem
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

class PokeSeeHandlerFactory : NamedScreenHandlerFactory {
    private var toView: ServerPlayerEntity? = null

    constructor(toView: ServerPlayerEntity?) {
        this.toView = toView
    }

    constructor()

    override fun getDisplayName(): Text {
        return Text.of("PokeSee Screen")
    }

    fun rows(): Int {
        return 4
    }

    fun size(): Int {
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
        }
        val storage = storage.getParty(
            serverPlayer!!
        )
        for (i in 0..5) {
            val pokemon = storage.get(i)
            if (pokemon != null) {
                val item = pokemonToItem(pokemon)
                inventory.setStack(12 + i + if (i >= 3) 6 else 0, item)
            } else {
                inventory.setStack(
                    12 + i + if (i >= 3) 6 else 0, ItemStack(Items.RED_STAINED_GLASS_PANE).setCustomName(
                        Text.literal("Empty").formatted(Formatting.GRAY)
                    )
                )
            }
        }
        return object : GenericContainerScreenHandler(ScreenHandlerType.GENERIC_9X4, syncId, inv, inventory, rows()) {
            override fun onSlotClick(slotIndex: Int, button: Int, actionType: SlotActionType, player: PlayerEntity) {}
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