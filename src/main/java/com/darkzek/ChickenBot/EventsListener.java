package com.darkzek.ChickenBot;

import net.dv8tion.jda.core.entities.Game;
import net.dv8tion.jda.core.events.Event;
import net.dv8tion.jda.core.events.guild.GuildJoinEvent;
import net.dv8tion.jda.core.events.guild.GuildLeaveEvent;
import net.dv8tion.jda.core.hooks.EventListener;

public class EventsListener implements EventListener {
    @Override
    public void onEvent(Event event) {
        if (event instanceof GuildJoinEvent || event instanceof GuildLeaveEvent) {
            PresenceMessage.getInstance(event.getJDA()).UpdatePresence();
        }
    }
}
