package dev.chasem.cobblemonextras.commands

import com.cobblemon.mod.common.Cobblemon.storage
import com.cobblemon.mod.common.api.pokemon.stats.Stats
import com.cobblemon.mod.common.pokemon.Pokemon
import com.cobblemon.mod.common.util.lang
import com.cobblemon.mod.common.util.permission
import com.mojang.brigadier.CommandDispatcher
import com.mojang.brigadier.arguments.IntegerArgumentType
import com.mojang.brigadier.context.CommandContext
import dev.chasem.cobblemonextras.CobblemonExtras
import dev.chasem.cobblemonextras.permissions.CobblemonExtrasPermissions
import net.minecraft.server.command.CommandManager
import net.minecraft.server.command.ServerCommandSource
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.text.*
import net.minecraft.util.Formatting
import java.lang.Boolean
import java.util.function.Consumer
import java.util.function.Predicate
import kotlin.Int

object PokeShout {
    fun register(dispatcher: CommandDispatcher<ServerCommandSource?>) {
        dispatcher.register(
            CommandManager.literal("pokeshout")
                .permission(CobblemonExtrasPermissions.POKESHOUT_PERMISSION)
                .then(
                    CommandManager.argument("slot", IntegerArgumentType.integer(1, 6))
                        .executes { ctx: CommandContext<ServerCommandSource> -> execute(ctx) })
        )
    }

    private fun execute(ctx: CommandContext<ServerCommandSource>): Int {
        if (ctx.source.player != null) {
            val player = ctx.source.player
            val slot = ctx.getArgument("slot", Int::class.java)
            val party = storage.getParty(
                player!!
            )
            val pokemon = party.get(slot - 1)
            if (pokemon != null) {
                val toSend = Text.literal("[").formatted(Formatting.GREEN)
                    .append(Text.literal("PokeShout").formatted(Formatting.YELLOW))
                    .append(Text.literal("] ").formatted(Formatting.GREEN))
                    .append(player.displayName.copy().append(Text.of(": ")).formatted(Formatting.WHITE))
                val pokemonName = pokemon.species.translatedName.formatted(Formatting.GREEN).append(" ")
                toSend.append(pokemonName)
                if (pokemon.shiny) {
                    toSend.append(Text.literal("★ ").formatted(Formatting.GOLD))
                }
                getHoverText(toSend, pokemon)
                ctx.source.server.playerManager.playerList.forEach(Consumer { serverPlayer: ServerPlayerEntity ->
                    serverPlayer.sendMessage(
                        toSend
                    )
                })
            } else {
                ctx.source.sendError(Text.literal("No Pokemon in slot."))
            }
        } else {
            ctx.source.sendError(Text.of("Sorry, this is only for players."))
        }
        return 1
    }

    fun getHoverText(toSend: MutableText, pokemon: Pokemon): Text {
        val statsHoverText = Text.literal("").fillStyle(Style.EMPTY)
        statsHoverText.append(
            pokemon.displayName.setStyle(
                Style.EMPTY.withColor(Formatting.DARK_GREEN).withUnderline(true)
            )
        )
        if (pokemon.shiny) {
            statsHoverText.append(Text.literal(" ★").formatted(Formatting.GOLD))
        }
        statsHoverText.append(Text.literal("\n"))
        statsHoverText.append(
            Text.literal("Level: ").formatted(Formatting.AQUA).append(
                Text.literal(pokemon.level.toString()).formatted(
                    Formatting.WHITE
                )
            )
        )
        statsHoverText.append(Text.literal("\n"))
        statsHoverText.append(
            Text.literal("Nature: ").formatted(Formatting.YELLOW).append(
                lang(pokemon.nature.displayName.replace("cobblemon.", "")).formatted(
                    Formatting.WHITE
                )
            )
        )
        statsHoverText.append(Text.literal("\n"))
        statsHoverText.append(
            Text.literal("Ability: ").formatted(Formatting.GOLD).append(
                lang(pokemon.ability.displayName.replace("cobblemon.", "")).formatted(
                    Formatting.WHITE
                )
            )
        )
        val statsText = Texts.join(
            Text.literal("[Stats]").formatted(Formatting.RED)
                .getWithStyle(
                    Style.EMPTY.withColor(Formatting.RED)
                        .withHoverEvent(HoverEvent(HoverEvent.Action.SHOW_TEXT, statsHoverText))
                ), Text.of("")
        )
        toSend.append(statsText)
        val evsText = Text.literal(" [EVs]").formatted(Formatting.GOLD)
        val evsHoverText = Text.literal("")
        val evsHoverTextList = Text.literal("EVs").formatted(Formatting.GOLD).getWithStyle(
            Style.EMPTY.withUnderline(
                Boolean.TRUE
            )
        )
        evsHoverTextList.add(Text.literal("\n"))
        evsHoverTextList.add(
            Text.literal("HP: ").formatted(Formatting.RED).append(
                Text.literal(
                    pokemon.evs.getOrDefault(
                        Stats.HP
                    ).toString()
                ).formatted(Formatting.WHITE)
            )
        )
        evsHoverTextList.add(Text.literal("\n"))
        evsHoverTextList.add(
            Text.literal("Attack: ").formatted(Formatting.BLUE).append(
                Text.literal(
                    pokemon.evs.getOrDefault(
                        Stats.ATTACK
                    ).toString()
                ).formatted(Formatting.WHITE)
            )
        )
        evsHoverTextList.add(Text.literal("\n"))
        evsHoverTextList.add(
            Text.literal("Defense: ").formatted(Formatting.GRAY).append(
                Text.literal(
                    pokemon.evs.getOrDefault(
                        Stats.DEFENCE
                    ).toString()
                ).formatted(Formatting.WHITE)
            )
        )
        evsHoverTextList.add(Text.literal("\n"))
        evsHoverTextList.add(
            Text.literal("Sp. Attack: ").formatted(Formatting.AQUA).append(
                Text.literal(
                    pokemon.evs.getOrDefault(
                        Stats.SPECIAL_ATTACK
                    ).toString()
                ).formatted(Formatting.WHITE)
            )
        )
        evsHoverTextList.add(Text.literal("\n"))
        evsHoverTextList.add(
            Text.literal("Sp. Defense: ").formatted(Formatting.YELLOW).append(
                Text.literal(
                    pokemon.evs.getOrDefault(
                        Stats.SPECIAL_DEFENCE
                    ).toString()
                ).formatted(Formatting.WHITE)
            )
        )
        evsHoverTextList.add(Text.literal("\n"))
        evsHoverTextList.add(
            Text.literal("Speed: ").formatted(Formatting.GREEN).append(
                Text.literal(
                    pokemon.evs.getOrDefault(
                        Stats.SPEED
                    ).toString()
                ).formatted(Formatting.WHITE)
            )
        )
        evsHoverTextList.forEach(Consumer { text: Text? -> evsHoverText.append(text) })
        val evsList = evsText.getWithStyle(
            evsText.style.withHoverEvent(
                HoverEvent(
                    HoverEvent.Action.SHOW_TEXT,
                    evsHoverText
                )
            )
        )
        evsList.forEach(Consumer { text: Text? -> toSend.append(text) })
        val ivsText = Text.literal(" [IVs]").formatted(Formatting.LIGHT_PURPLE)
        val ivsHoverText = Text.literal("")
        val ivsHoverTextList = Text.literal("IVs").formatted(Formatting.GOLD).getWithStyle(
            Style.EMPTY.withUnderline(
                Boolean.TRUE
            )
        )
        ivsHoverTextList.add(Text.literal("\n"))
        ivsHoverTextList.add(
            Text.literal("HP: ").formatted(Formatting.RED).append(
                Text.literal(
                    pokemon.ivs.getOrDefault(
                        Stats.HP
                    ).toString()
                ).formatted(Formatting.WHITE)
            )
        )
        ivsHoverTextList.add(Text.literal("\n"))
        ivsHoverTextList.add(
            Text.literal("Attack: ").formatted(Formatting.BLUE).append(
                Text.literal(
                    pokemon.ivs.getOrDefault(
                        Stats.ATTACK
                    ).toString()
                ).formatted(Formatting.WHITE)
            )
        )
        ivsHoverTextList.add(Text.literal("\n"))
        ivsHoverTextList.add(
            Text.literal("Defense: ").formatted(Formatting.GRAY).append(
                Text.literal(
                    pokemon.ivs.getOrDefault(
                        Stats.DEFENCE
                    ).toString()
                ).formatted(Formatting.WHITE)
            )
        )
        ivsHoverTextList.add(Text.literal("\n"))
        ivsHoverTextList.add(
            Text.literal("Sp. Attack: ").formatted(Formatting.AQUA).append(
                Text.literal(
                    pokemon.ivs.getOrDefault(
                        Stats.SPECIAL_ATTACK
                    ).toString()
                ).formatted(Formatting.WHITE)
            )
        )
        ivsHoverTextList.add(Text.literal("\n"))
        ivsHoverTextList.add(
            Text.literal("Sp. Defense: ").formatted(Formatting.YELLOW).append(
                Text.literal(
                    pokemon.ivs.getOrDefault(
                        Stats.SPECIAL_DEFENCE
                    ).toString()
                ).formatted(Formatting.WHITE)
            )
        )
        ivsHoverTextList.add(Text.literal("\n"))
        ivsHoverTextList.add(
            Text.literal("Speed: ").formatted(Formatting.GREEN).append(
                Text.literal(
                    pokemon.ivs.getOrDefault(
                        Stats.SPEED
                    ).toString()
                ).formatted(Formatting.WHITE)
            )
        )
        ivsHoverTextList.forEach(Consumer { text: Text? -> ivsHoverText.append(text) })
        val ivsList = ivsText.getWithStyle(
            ivsText.style.withHoverEvent(
                HoverEvent(
                    HoverEvent.Action.SHOW_TEXT,
                    ivsHoverText
                )
            )
        )
        ivsList.forEach(Consumer { text: Text? -> toSend.append(text) })
        val movesText = Text.literal(" [Moves]").formatted(Formatting.BLUE)
        val movesHoverText = Text.literal("")
        val movesHoverTextList = Text.literal("Moves").formatted(Formatting.BLUE).getWithStyle(
            Style.EMPTY.withUnderline(
                Boolean.TRUE
            )
        )
        movesHoverTextList.add(Text.literal("\n"))
        val moveOne = if (pokemon.moveSet.getMoves().size >= 1) pokemon.moveSet[0]!!.displayName.string else "Empty"
        val moveTwo = if (pokemon.moveSet.getMoves().size >= 2) pokemon.moveSet[1]!!.displayName.string else "Empty"
        val moveThree = if (pokemon.moveSet.getMoves().size >= 3) pokemon.moveSet[2]!!.displayName.string else "Empty"
        val moveFour = if (pokemon.moveSet.getMoves().size >= 4) pokemon.moveSet[3]!!.displayName.string else "Empty"
        movesHoverTextList.add(
            Text.literal("Move 1: ").formatted(Formatting.RED).append(
                Text.literal(moveOne).formatted(
                    Formatting.WHITE
                )
            )
        )
        movesHoverTextList.add(Text.literal("\n"))
        movesHoverTextList.add(
            Text.literal("Move 2: ").formatted(Formatting.YELLOW).append(
                Text.literal(moveTwo).formatted(
                    Formatting.WHITE
                )
            )
        )
        movesHoverTextList.add(Text.literal("\n"))
        movesHoverTextList.add(
            Text.literal("Move 3: ").formatted(Formatting.AQUA).append(
                Text.literal(moveThree).formatted(
                    Formatting.WHITE
                )
            )
        )
        movesHoverTextList.add(Text.literal("\n"))
        movesHoverTextList.add(
            Text.literal("Move 4: ").formatted(Formatting.GREEN).append(
                Text.literal(moveFour).formatted(
                    Formatting.WHITE
                )
            )
        )
        movesHoverTextList.forEach(Consumer { text: Text? -> movesHoverText.append(text) })
        val movesList = movesText.getWithStyle(
            movesText.style.withHoverEvent(
                HoverEvent(
                    HoverEvent.Action.SHOW_TEXT,
                    movesHoverText
                )
            )
        )
        movesList.forEach(Consumer { text: Text? -> toSend.append(text) })
        return toSend
    }
}