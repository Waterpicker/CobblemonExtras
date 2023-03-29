package dev.chasem.cobblemonextras

import dev.architectury.event.events.common.CommandRegistrationEvent
import dev.chasem.cobblemonextras.commands.*

object CobblemonExtras {
    const val MODID = "cobblemonextras"
    fun initialize() {
        System.out.println("CobblemonExtras - Initialized")
        CommandRegistrationEvent.EVENT.register { dispatcher, _, _ ->
            run {
                CompSee.register(dispatcher);
                PC.register(dispatcher);
                PokeSee.register(dispatcher);
                PokeShout.register(dispatcher);
                PokeTrade.register(dispatcher);
            }
        }
    }
}
