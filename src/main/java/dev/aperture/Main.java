package dev.aperture;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.utils.MemberCachePolicy;
import dev.aperture.commands.games.TicTacToe;
import dev.aperture.commands.info.ServerInfo;
import dev.aperture.commands.info.UserInfo;
import dev.aperture.commands.misc.Ping;
import javax.security.auth.login.LoginException;
import io.github.cdimascio.dotenv.Dotenv;

public class Main
{
    public static void main(String[] args) throws LoginException
    {
        Dotenv dotenv = Dotenv.load();
        String token = dotenv.get("BOT_TOKEN");

        JDA jda = JDABuilder.createDefault(token).enableIntents(GatewayIntent.GUILD_MEMBERS)
                .enableIntents(GatewayIntent.GUILD_MESSAGES, GatewayIntent.GUILD_MEMBERS, GatewayIntent.GUILD_PRESENCES)
                .setMemberCachePolicy(MemberCachePolicy.ALL).setActivity(Activity.playing("Aperture")).build();

        jda.updateCommands()
                .addCommands(Commands.slash("ping", "Calculates the ping of the bot."),
                        Commands.slash("serverinfo", "Display all information regarding the server"),
                        Commands.slash("userinfo", "Display all information of an user").addOption(OptionType.USER,
                                "user", "The user to get.", false),
                        Commands.slash("tictactoe", "Play tictactoe.").addOption(OptionType.USER, "user",
                                "The user with whom you want to play with.", true))
                .queue();

        jda.addEventListener(new Ping());
        jda.addEventListener(new UserInfo());
        jda.addEventListener(new ServerInfo());
        jda.addEventListener(new TicTacToe());
    }
}