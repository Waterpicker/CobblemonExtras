package dev.chasem.cobblemonextras.util

import net.minecraft.item.Item
import net.minecraft.item.ItemStack
import net.minecraft.nbt.NbtList
import net.minecraft.nbt.NbtString
import net.minecraft.text.Style
import net.minecraft.text.Text
import net.minecraft.text.Texts

class ItemBuilder(private var stack: ItemStack) {
    constructor(item: Item) : this(ItemStack(item))

    fun addLore(lore: Array<Text>): ItemBuilder {
        val nbt = stack.orCreateNbt
        val displayNbt = stack.getOrCreateSubNbt("display")
        val nbtLore = NbtList()
        for (text in lore) {
            val line = Texts.join(text.getWithStyle(Style.EMPTY.withItalic(false)), Text.of(""))
            nbtLore.add(NbtString.of(Text.Serializer.toJson(line)))
        }
        displayNbt.put("Lore", nbtLore)
        nbt.put("display", displayNbt)
        stack!!.nbt = nbt
        return this
    }

    fun hideAdditional(): ItemBuilder {
        stack.addHideFlag(ItemStack.TooltipSection.ADDITIONAL)
        return this
    }

    fun setCustomName(customName: Text): ItemBuilder {
        val pokemonName = Texts.join(customName.getWithStyle(Style.EMPTY.withItalic(false)), Text.of(""))
        stack.setCustomName(pokemonName)
        return this
    }

    fun build(): ItemStack {
        return stack
    }
}