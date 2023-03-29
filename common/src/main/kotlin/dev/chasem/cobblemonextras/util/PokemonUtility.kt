package dev.chasem.cobblemonextras.util

import com.cobblemon.mod.common.api.moves.Move
import com.cobblemon.mod.common.api.moves.MoveSet
import com.cobblemon.mod.common.api.pokemon.stats.Stats
import com.cobblemon.mod.common.api.text.*
import com.cobblemon.mod.common.item.PokemonItem
import com.cobblemon.mod.common.pokemon.EVs
import com.cobblemon.mod.common.pokemon.IVs
import com.cobblemon.mod.common.pokemon.Pokemon
import com.cobblemon.mod.common.pokemon.PokemonStats
import com.cobblemon.mod.common.util.lang
import net.minecraft.item.ItemStack
import net.minecraft.text.MutableText
import net.minecraft.text.Style
import net.minecraft.text.Text
import net.minecraft.util.Formatting
object PokemonUtility {
    @JvmStatic
    fun pokemonToItem(pokemon: Pokemon): ItemStack = with(pokemon) {
        return ItemBuilder(PokemonItem.from(pokemon, 1))
            .hideAdditional()
            .addLore(
                arrayOf<Text>(
                    caughtBall.item().name.copy().italicise().darkGray(),
                    "Level: ".aqua() + level.toString().white(),
                    "Nature: ".yellow() + lang(nature.displayName.replace("cobblemon.", "")).white(),
                    "Ability: ".gold() + lang(ability.displayName.replace("cobblemon.", "")).white(),
                    "IVs: ".lightPurple(),
                    "  HP: ".red() + ivs.text(Stats.HP) + "  Atk: ".blue() + ivs.text(Stats.ATTACK) + "  Def: ".green() + ivs.text(Stats.DEFENCE),
                    "  SpAtk: ".aqua() + pokemon.ivs.text(Stats.SPECIAL_ATTACK) + "  SpDef: ".yellow() + ivs.text(Stats.SPECIAL_DEFENCE) + "  Spd: ".green() + ivs.text(Stats.SPEED),
                    "EVs: ".darkAqua(),
                    "  HP: ".red() + evs.text(Stats.HP) + "  Atk: ".blue() + evs.text(Stats.ATTACK) + "  Def: ".gray() + evs.text(Stats.DEFENCE),
                    "  SpAtk: ".aqua() + evs.text(Stats.SPECIAL_ATTACK) + "  SpDef: ".yellow() + evs.text(Stats.SPECIAL_DEFENCE) + "  Spd: ".green() + evs.text(Stats.SPEED),
                    "Moves: ".darkGreen(),
                    moveSet.text(0),
                    moveSet.text(1),
                    moveSet.text(2),
                    moveSet.text(3)
                )
            )
            .setCustomName(if (pokemon.shiny) displayName.gray() + " ★".gold() else pokemon.displayName.gray())
            .build()
    }
}

private fun PokemonStats.text(stat: Stats): MutableText = this.getOrDefault(stat).text()

private fun Int.text() = this.toString().white()
private fun MoveSet.text(slot: Int): MutableText = (" " + (this[slot]?.takeIf { getMoves().size >= slot+1 }?.displayName?.string ?: "Empty")).white()
