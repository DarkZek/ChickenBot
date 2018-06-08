package com.darkzek.ChickenBot;

import net.dv8tion.jda.core.entities.Game;
import net.dv8tion.jda.core.events.Event;
import net.dv8tion.jda.core.events.guild.GuildJoinEvent;
import net.dv8tion.jda.core.events.guild.GuildLeaveEvent;
import net.dv8tion.jda.core.hooks.EventListener;

public class EventsListener implements EventListener {
    @Override
    public void onEvent(Event event) {
        if (event instanceof GuildJoinEvent) {
            //Joined a new guild. Update the status
            event.getJDA().getPresence().setGame(Game.playing(">help | " + event.getJDA().getGuilds().size() + " servers"));
        } else if (event instanceof GuildLeaveEvent) {
            //Update the status
            event.getJDA().getPresence().setGame(Game.playing(">help | " + event.getJDA().getGuilds().size() + " servers"));
        }
    }
}
