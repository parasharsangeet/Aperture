package dev.aperture.commands.info;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import java.time.Instant;
import java.util.stream.Collectors;
import dev.aperture.Constants;

public class UserInfo extends ListenerAdapter
{
    @Override
    public void onSlashCommandInteraction(SlashCommandInteractionEvent event)
    {
        if (event.getName().equals("userinfo"))
        {
            Guild server = event.getGuild();

            OptionMapping user = event.getOption("user");
            User author = user == null ? event.getUser() : user.getAsUser();
            Member mem = server.getMember(author);

            long creationTime = author.getTimeCreated().toEpochSecond();
            long joinedAt = mem.getTimeJoined().toEpochSecond();

            int rolesNo = mem.getRoles().size();
            String roles = mem.getRoles().stream().filter(role -> !role.getName().equals("everyone"))
                    .map(role -> "<@&" + role.getId() + ">").collect(Collectors.joining(" "));

            EmbedBuilder embed = new EmbedBuilder().setDescription("<@" + author.getId() + ">")
                    .setAuthor(author.getName(), null, author.getAvatarUrl()).setColor(Constants.GENERAL)
                    .addField("• ID", "`" + author.getId() + "`", true)
                    .addField("• Nickname", author.getEffectiveName(), true)
                    .addField("• Creation", "<t:" + creationTime + "> (<t:" + creationTime + ":R>)", false)
                    .addField("• Joined Server", "<t:" + joinedAt + "> (<t:" + joinedAt + ":R>)", false)
                    .addField("• Roles [" + rolesNo + "]", roles, false)
                    .setThumbnail(author.getAvatar().getUrl().toString())
                    .setFooter("Aperture", event.getJDA().getSelfUser().getAvatarUrl()).setTimestamp(Instant.now());

            event.replyEmbeds(embed.build()).queue();
        }
    }
}
