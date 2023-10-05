package dev.aperture.commands.info;

import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

public class ServerInfo extends ListenerAdapter
{
    @Override
    public void onSlashCommandInteraction(SlashCommandInteractionEvent event)
    {
        if (event.getName().equals("serverinfo"))
        {
            event.reply("Get lost idiot").queue();
        }
    }
}
