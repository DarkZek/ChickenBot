package com.darkzek.ChickenBot;

import net.dv8tion.jda.api.events.GenericEvent;
import net.dv8tion.jda.api.events.guild.GuildJoinEvent;
import net.dv8tion.jda.api.events.guild.GuildLeaveEvent;
import net.dv8tion.jda.api.hooks.EventListener;
import org.jetbrains.annotations.NotNull;

public class EventsListener implements EventListener {
    @Override
    public void onEvent(@NotNull GenericEvent event) {
        if (event instanceof GuildJoinEvent || event instanceof GuildLeaveEvent) {
            PresenceMessage.getInstance(event.getJDA()).UpdatePresence();
        }
    }
}
