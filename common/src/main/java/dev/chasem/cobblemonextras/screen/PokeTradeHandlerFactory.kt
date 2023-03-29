package dev.chasem.cobblemonextras.screen

import com.cobblemon.mod.common.Cobblemon.storage
import dev.chasem.cobblemonextras.commands.PokeTrade.TradeSession
import dev.chasem.cobblemonextras.util.PokemonUtility.pokemonToItem
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.entity.player.PlayerInventory
import net.minecraft.inventory.Inventory
import net.minecraft.inventory.SimpleInventory
import net.minecraft.item.ItemStack
import net.minecraft.item.Items
import net.minecraft.network.packet.s2c.play.ScreenHandlerSlotUpdateS2CPacket
import net.minecraft.screen.GenericContainerScreenHandler
import net.minecraft.screen.NamedScreenHandlerFactory
import net.minecraft.screen.ScreenHandler
import net.minecraft.screen.ScreenHandlerType
import net.minecraft.screen.slot.Slot
import net.minecraft.screen.slot.SlotActionType
import net.minecraft.text.Text
import net.minecraft.util.Formatting

class PokeTradeHandlerFactory(private val tradeSession: TradeSession) : NamedScreenHandlerFactory {
    override fun getDisplayName(): Text {
        return Text.of("Trade: " + tradeSession.trader1!!.entityName + " - " + tradeSession.trader2.entityName)
    }

    fun rows(): Int {
        return 6
    }

    fun size(): Int {
        return rows() * 9
    }

    override fun createMenu(
        syncId: Int,
        inv: PlayerInventory,
        player: PlayerEntity
    ): ScreenHandler? {
        val unacceptedItem =
            ItemStack(Items.GRAY_DYE)
                .setCustomName(Text.literal("Click to Accept"))
        val acceptedItem =
            ItemStack(Items.LIME_DYE)
                .setCustomName(Text.literal("Accepted. Click to undo."))
        val inventory = SimpleInventory(size())
        for (i in 0 until size()) {
            val row = Math.floor(i.toDouble() / 9.0)
            val index = i % 9
            if (index == 4) {
                inventory.setStack(
                    i,
                    ItemStack(Items.YELLOW_STAINED_GLASS_PANE)
                        .setCustomName(Text.of(" "))
                )
            } else if ((row == 1.0 || row == 3.0) && index >= 1 && index <= 7) {
                inventory.setStack(
                    i,
                    ItemStack(Items.LIGHT_BLUE_STAINED_GLASS_PANE)
                        .setCustomName(Text.of(" "))
                )
            } else if (row == 2.0 && index > 0 && index % 2 == 1) {
                inventory.setStack(
                    i,
                    ItemStack(Items.LIGHT_BLUE_STAINED_GLASS_PANE)
                        .setCustomName(Text.of(" "))
                )
            } else {
                inventory.setStack(
                    i,
                    ItemStack(Items.GRAY_STAINED_GLASS_PANE)
                        .setCustomName(Text.of(" "))
                )
            }
        }
        inventory.setStack(53, unacceptedItem)
        inventory.setStack(45, unacceptedItem)
        val trader1Storage =
            storage.getParty(
                tradeSession.trader1!!
            )
        for (i in 0..5) {
            val pokemon = trader1Storage.get(i)
            if (pokemon != null) {
                val item = pokemonToItem(pokemon)
                val slotNbt = item.getOrCreateSubNbt("slot")
                slotNbt.putInt("slot", i)
                item.setSubNbt("slot", slotNbt)
                inventory.setStack(37 + i + if (i >= 3) 6 else 0, item)
            } else {
                inventory.setStack(
                    37 + i + if (i >= 3) 6 else 0,
                    ItemStack(Items.RED_STAINED_GLASS_PANE).setCustomName(
                        Text.literal("Empty").formatted(Formatting.GRAY)
                    )
                )
            }
        }
        val trader2Storage =
            storage.getParty(
                tradeSession.trader2
            )
        for (i in 0..5) {
            val pokemon = trader2Storage.get(i)
            if (pokemon != null) {
                val item = pokemonToItem(pokemon)
                val slotNbt = item.getOrCreateSubNbt("slot")
                slotNbt.putInt("slot", i)
                item.setSubNbt("slot", slotNbt)
                inventory.setStack(41 + i + if (i >= 3) 6 else 0, item)
            } else {
                inventory.setStack(
                    41 + i + if (i >= 3) 6 else 0,
                    ItemStack(Items.RED_STAINED_GLASS_PANE).setCustomName(
                        Text.literal("Empty").formatted(Formatting.GRAY)
                    )
                )
            }
        }
        return object :
            GenericContainerScreenHandler(ScreenHandlerType.GENERIC_9X6, syncId, inv, inventory, rows()) {
            override fun onSlotClick(slotIndex: Int, button: Int, actionType: SlotActionType, player: PlayerEntity) {
                if (tradeSession.cancelled) {
                    player.sendMessage(Text.literal("Trade has been cancelled.").formatted(Formatting.RED))
                    player.closeHandledScreen()
                }
                val row = Math.floor(slotIndex.toDouble() / 9.0)
                val index = slotIndex % 9
                if (index > 4 && player.uuid == tradeSession.trader1!!.uuid) {
                    return
                }
                if (index < 4 && player.uuid == tradeSession.trader2.uuid) {
                    return
                }
                setStackInSlot(45, nextRevision(), if (tradeSession.trader1Accept) acceptedItem else unacceptedItem)
                setStackInSlot(53, nextRevision(), if (tradeSession.trader2Accept) acceptedItem else unacceptedItem)
                if (tradeSession.trader1Pokemon != null) {
                    val pokemonItem = pokemonToItem(tradeSession.trader1Pokemon!!)
                    setStackInSlot(20, nextRevision(), pokemonItem)
                }
                if (tradeSession.trader2Pokemon != null) {
                    val pokemonItem = pokemonToItem(tradeSession.trader2Pokemon!!)
                    setStackInSlot(24, nextRevision(), pokemonItem)
                }
                if (slotIndex == 45 && player.uuid == tradeSession.trader1!!.uuid) {
                    tradeSession.trader1Accept = !tradeSession.trader1Accept
                    val item = if (tradeSession.trader1Accept) acceptedItem else unacceptedItem
                    setStackInSlot(45, nextRevision(), item)

                    // Syncs other players inventory immediately.
                    val packet =
                        ScreenHandlerSlotUpdateS2CPacket(syncId, nextRevision(), slotIndex, item.copy())
                    tradeSession.trader2.networkHandler.sendPacket(packet)
                }
                if (slotIndex == 53 && player.uuid == tradeSession.trader2.uuid) {
                    tradeSession.trader2Accept = !tradeSession.trader2Accept
                    val item = if (tradeSession.trader2Accept) acceptedItem else unacceptedItem
                    setStackInSlot(53, nextRevision(), item)

                    // Syncs other players inventory immediately.
                    val packet =
                        ScreenHandlerSlotUpdateS2CPacket(syncId, nextRevision(), slotIndex, item.copy())
                    tradeSession.trader1!!.networkHandler.sendPacket(packet)
                }
                if (slotIndex > 54) {
                    return
                }
                val stack = getInventory().getStack(slotIndex)
                if (stack != null && stack.hasNbt() && stack.getSubNbt("slot") != null) {
                    val slot = stack.getSubNbt("slot")!!.getInt("slot")
                    if (player.uuid == tradeSession.trader1!!.uuid) {
                        val pokemon = trader1Storage.get(slot)
                        if (pokemon != null) {
                            tradeSession.trader1Pokemon = pokemon
                            val pokemonItem = pokemonToItem(pokemon)
                            setStackInSlot(20, nextRevision(), pokemonItem)
                            val packet =
                                ScreenHandlerSlotUpdateS2CPacket(syncId, nextRevision(), 20, pokemonItem)
                            tradeSession.trader2.networkHandler.sendPacket(packet)
                            tradeSession.trader1Accept = false
                            tradeSession.trader2Accept = false
                            setStackInSlot(45, nextRevision(), unacceptedItem)
                            setStackInSlot(53, nextRevision(), unacceptedItem)
                            val packet2 =
                                ScreenHandlerSlotUpdateS2CPacket(syncId, nextRevision(), 45, unacceptedItem.copy())
                            tradeSession.trader2.networkHandler.sendPacket(packet2)
                            val packet3 =
                                ScreenHandlerSlotUpdateS2CPacket(syncId, nextRevision(), 53, unacceptedItem.copy())
                            tradeSession.trader2.networkHandler.sendPacket(packet3)
                        }
                    } else {
                        val pokemon = trader2Storage.get(slot)
                        if (pokemon != null) {
                            tradeSession.trader2Pokemon = pokemon
                            val pokemonItem = pokemonToItem(pokemon)
                            setStackInSlot(24, nextRevision(), pokemonItem)
                            val packet =
                                ScreenHandlerSlotUpdateS2CPacket(syncId, nextRevision(), 24, pokemonItem)
                            tradeSession.trader1!!.networkHandler.sendPacket(packet)
                            tradeSession.trader1Accept = false
                            tradeSession.trader2Accept = false
                            setStackInSlot(45, nextRevision(), unacceptedItem)
                            setStackInSlot(53, nextRevision(), unacceptedItem)
                            val packet2 =
                                ScreenHandlerSlotUpdateS2CPacket(syncId, nextRevision(), 45, unacceptedItem.copy())
                            tradeSession.trader1!!.networkHandler.sendPacket(packet2)
                            val packet3 =
                                ScreenHandlerSlotUpdateS2CPacket(syncId, nextRevision(), 53, unacceptedItem.copy())
                            tradeSession.trader1!!.networkHandler.sendPacket(packet3)
                        }
                    }
                }
                if (tradeSession.trader1Accept && tradeSession.trader2Accept) {
                    tradeSession.doTrade()
                    tradeSession.trader1!!.closeHandledScreen()
                    tradeSession.trader2.closeHandledScreen()
                }
            }

            override fun transferSlot(player: PlayerEntity, index: Int): ItemStack {
                return ItemStack.EMPTY
            }

            override fun canInsertIntoSlot(slot: Slot): Boolean {
                return false
            }

            override fun dropInventory(player: PlayerEntity, inventory: Inventory) {}
            override fun close(player: PlayerEntity) {
                if (!tradeSession.cancelled) {
                    tradeSession.cancel()
                    tradeSession.trader1!!.closeHandledScreen()
                    tradeSession.trader2.closeHandledScreen()
                }
            }
        }
    }
}