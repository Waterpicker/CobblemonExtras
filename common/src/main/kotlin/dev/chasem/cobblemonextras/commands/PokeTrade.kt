package dev.chasem.cobblemonextras.commands

import com.cobblemon.mod.common.Cobblemon.storage
import com.cobblemon.mod.common.api.pokemon.evolution.Evolution
import com.cobblemon.mod.common.api.text.plus
import com.cobblemon.mod.common.api.text.red
import com.cobblemon.mod.common.api.text.text
import com.cobblemon.mod.common.api.text.yellow
import com.cobblemon.mod.common.pokemon.Pokemon
import com.cobblemon.mod.common.pokemon.evolution.variants.TradeEvolution
import com.cobblemon.mod.common.util.permission
import com.mojang.brigadier.CommandDispatcher
import com.mojang.brigadier.context.CommandContext
import com.mojang.brigadier.exceptions.CommandSyntaxException
import dev.chasem.cobblemonextras.CobblemonExtras
import dev.chasem.cobblemonextras.permissions.CobblemonExtrasPermissions
import dev.chasem.cobblemonextras.screen.PokeTradeHandlerFactory
import net.minecraft.command.argument.EntityArgumentType
import net.minecraft.server.command.CommandManager
import net.minecraft.server.command.ServerCommandSource
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.text.*
import net.minecraft.util.Formatting
import java.util.*
import java.util.function.Consumer
import java.util.function.Predicate

object PokeTrade {
    fun register(dispatcher: CommandDispatcher<ServerCommandSource?>) {
        dispatcher.register(
            CommandManager.literal("poketrade")
                .permission(CobblemonExtrasPermissions.POKETRADE_PERMISSION)
                .then(
                    CommandManager.literal("accept")
                        .executes { ctx: CommandContext<ServerCommandSource> -> respond(ctx) })
        )
        dispatcher.register(
            CommandManager.literal("poketrade")
                .permission(CobblemonExtrasPermissions.POKETRADE_PERMISSION)
                .then(
                    CommandManager.literal("deny")
                        .executes { ctx: CommandContext<ServerCommandSource> -> respond(ctx) })
        )
        dispatcher.register(
            CommandManager.literal("poketrade")
                .permission(CobblemonExtrasPermissions.POKETRADE_PERMISSION)
                .then(
                    CommandManager.literal("cancel")
                        .executes { ctx: CommandContext<ServerCommandSource> -> respond(ctx) })
        )
        dispatcher.register(
            CommandManager.literal("poketrade")
                .permission(CobblemonExtrasPermissions.POKETRADE_PERMISSION)
                .then(
                    CommandManager.argument("player", EntityArgumentType.player())
                        .executes { ctx: CommandContext<ServerCommandSource> -> createTrade(ctx) })
        )
    }

    var tradeSessions = HashMap<UUID, TradeSession?>()

    class TradeSession(var trader1: ServerPlayerEntity?, var trader2: ServerPlayerEntity) {
        var trader1UUID // In case of offline.
                : UUID = trader1!!.uuid
        var trader1Accept = false
        var trader2UUID // In case of offline.
                : UUID = trader2.uuid
        var trader2Accept = false
        var trader1Pokemon: Pokemon? = null
        var trader2Pokemon: Pokemon? = null
        var timestamp: Long = System.currentTimeMillis()
        var cancelled = false

        fun cancel() {
            trader1!!.sendMessage(Text.literal("Trade cancelled.").formatted(Formatting.RED))
            trader2.sendMessage(Text.literal("Trade cancelled.").formatted(Formatting.RED))
            tradeSessions.remove(trader1UUID)
            tradeSessions.remove(trader2UUID)
            cancelled = true
        }

        fun deny() {
            trader1!!.sendMessage(Text.literal("Trade declined.").formatted(Formatting.RED))
            trader2.sendMessage(Text.literal("Trade declined.").formatted(Formatting.RED))
            tradeSessions.remove(trader1UUID)
            tradeSessions.remove(trader2UUID)
            cancelled = true
        }

        fun expire() {
            trader1!!.sendMessage(Text.literal("Trade request expired.").formatted(Formatting.RED))
            trader2.sendMessage(Text.literal("Trade request expired.").formatted(Formatting.RED))
            tradeSessions.remove(trader1UUID)
            tradeSessions.remove(trader2UUID)
            cancelled = true
        }

        fun accept() {
            val tradeHandler = PokeTradeHandlerFactory(this)
            trader1!!.openHandledScreen(tradeHandler)
            trader2.openHandledScreen(tradeHandler)
        }

        fun doTrade() {
            if (cancelled) {
                println("Something funky is goin' on")
                cancel()
                return
            }
            cancelled = true
            val party1 = storage.getParty(
                trader1!!
            )
            val party2 = storage.getParty(
                trader2
            )
            trader1Pokemon?.apply { party1.remove(this) }
            trader2Pokemon?.apply { party2.remove(this) }

            trader1Pokemon?.apply {
                this.also { party2.add(this) }.evolutions.forEach(Consumer { evolution: Evolution -> (evolution as? TradeEvolution)?.evolve(this) })
            }

            if (trader2Pokemon != null) {
                party1.add(trader2Pokemon!!)
                trader2Pokemon!!.evolutions.forEach(Consumer { evolution: Evolution ->
                    (evolution as? TradeEvolution)?.evolve(
                        trader2Pokemon!!
                    )
                })
            }
            val toSend: Text = Text.literal("Trade complete!").formatted(Formatting.GREEN)
            trader1!!.sendMessage(toSend)
            trader2.sendMessage(toSend)
            tradeSessions.remove(trader1UUID)
            tradeSessions.remove(trader2UUID)
        }
    }

    private fun createTrade(ctx: CommandContext<ServerCommandSource>): Int = with(ctx.source.player) {
        this?.apply {
            if (tradeSessions.containsKey(this.uuid)) {
                val tradeSession = tradeSessions[this.uuid]
                val timeSince = System.currentTimeMillis() - tradeSession!!.timestamp
                when {
                    timeSince > 1000 * 60 -> {
                        // Expire sender's trade session.
                        tradeSession.expire()
                    }
                    else -> {
                        val cancel = "[CANCEL]".text()
                            .styled {
                                it.withBold(true)
                                it.withColor(Formatting.DARK_RED)
                                it.withClickEvent(ClickEvent(ClickEvent.Action.RUN_COMMAND, "/poketrade cancel"))
                                it.withHoverEvent(HoverEvent(HoverEvent.Action.SHOW_TEXT, "Cancel Trade".text()))
                            }

                        this.sendMessage("You have a trade pending. Cancel your last before creating a new trade. ".red() + cancel)
                        return 1
                    }
                }
            }
            val tradePartnerPlayer: ServerPlayerEntity = try {
                EntityArgumentType.getPlayer(ctx, "player")
            } catch (e: CommandSyntaxException) {
                ctx.source.sendError("Error finding player.".text())
                return 1
            }

            if (tradePartnerPlayer.uuid == this.uuid) {
                ctx.source.sendError("Trading yourself? Your worth more than that <3".text())
                return 1
            }

            if (tradeSessions.containsKey(tradePartnerPlayer.uuid)) {
                val tradeSession = tradeSessions[tradePartnerPlayer.uuid]
                val timeSince = System.currentTimeMillis() - tradeSession!!.timestamp

                when {
                    timeSince > 1000 * 60 -> {
                        // Expire trade partner's trade session.
                        tradeSession.expire()
                    }
                    else -> {
                        this.sendMessage("Trade partner already has a trade pending, they must cancel or complete their trade before starting a new one.".red())
                        return 1
                    }
                }
            }

            val tradeSession = TradeSession(this, tradePartnerPlayer)
            tradeSessions[tradePartnerPlayer.uuid] = tradeSession
            tradeSessions[this.uuid] = tradeSession
            this.sendMessage(Text.literal("Trade request sent.").formatted(Formatting.YELLOW))
            tradePartnerPlayer.sendMessage(
                Text.literal("Pokemon trade request received from ").formatted(Formatting.YELLOW)
                    .append(Text.literal(this.entityName + ". ").formatted(Formatting.GREEN))
            )
            val accept: Text = "[ACCEPT]".text()
                    .styled {
                        it.withBold(true).withColor(Formatting.GREEN)
                        it.withClickEvent(ClickEvent(ClickEvent.Action.RUN_COMMAND, "/poketrade accept"))
                        it.withHoverEvent(HoverEvent(HoverEvent.Action.SHOW_TEXT, "Accept Trade".text()))
                    }

            val deny: Text = "[DENY]".red()
                    .styled {
                        it.withClickEvent(ClickEvent(ClickEvent.Action.RUN_COMMAND, "/poketrade deny"))
                        it.withHoverEvent(HoverEvent(HoverEvent.Action.SHOW_TEXT, "Deny Trade".text()))
                    }

            tradePartnerPlayer.sendMessage(accept.copy().append(" ").append(deny))
        }
        return 1
    }

    private fun respond(ctx: CommandContext<ServerCommandSource>): Int = with(ctx.source.player) {
        return@with this?.run {
            val response = ctx.input.split(" ".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()[1]
            val tradeSession = this.uuid.let { tradeSessions.getOrDefault(it, null) }
            if (tradeSession == null) {
                this.sendMessage("No pending trade session.".yellow())
                return 1
            }
            when {
                response.equals("cancel", ignoreCase = true) -> {
                    tradeSession.cancel()
                }

                response.equals("deny", ignoreCase = true) -> {
                    if (tradeSession.trader2UUID == this.uuid) {
                        tradeSession.deny()
                    }
                }

                response.equals("accept", ignoreCase = true) -> {
                    if (tradeSession.trader2UUID == this.uuid) { // The INVITED user (trader2) accepted.
                        tradeSession.accept()
                    }
                }
            }
        }
        }.run { 1 }
}