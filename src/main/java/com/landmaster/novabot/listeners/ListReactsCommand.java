package com.landmaster.novabot.listeners;

import com.landmaster.novabot.util.ReactionAndUser;
import net.dv8tion.jda.api.entities.channel.concrete.ThreadChannel;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.requests.RestAction;
import net.dv8tion.jda.api.utils.MarkdownSanitizer;
import net.dv8tion.jda.api.utils.messages.MessageCreateBuilder;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.*;

public class ListReactsCommand extends ListenerAdapter {
    private final Connection connection;

    public ListReactsCommand(Connection connection) {
        this.connection = connection;
    }

    @Override
    public void onSlashCommandInteraction(SlashCommandInteractionEvent event) {
        if (!event.getName().equals("list-reacts")) {
            return;
        }

        boolean isValidChannel = false;
        if (event.getChannel() instanceof ThreadChannel threadChannel) {
            if (threadChannel.getParentChannel().getId().equals(System.getenv("NOVABOT_MEETUP_FORUM"))) {
                isValidChannel = true;
                event.deferReply().queue();
                threadChannel.retrieveStartMessage().queue(message -> {
                    var reactions = message.getReactions();
                    Map<ReactionAndUser, String> userReactions = new HashMap<>();
                    List<RestAction<Void>> actions = new ArrayList<>();
                    for (var reaction: reactions) {
                        actions.add(reaction.retrieveUsers().map(users -> {
                            for (var user: users) {
                                userReactions.put(new ReactionAndUser(reaction.getEmoji().getFormatted(), user.getIdLong()), user.getEffectiveName());
                            }
                            return null;
                        }));
                    }
                    if (actions.isEmpty()) {
                        event.getHook().sendMessage("No reacts found!").queue();
                        return;
                    }
                    RestAction.allOf(actions).queue(dummy -> {
                        var reply = new StringBuilder();
                        try (PreparedStatement statement = connection.prepareStatement("SELECT * FROM reacts WHERE thread_id = ? ORDER BY timestamp, uncertain DESC")) {
                            statement.setLong(1, threadChannel.getIdLong());
                            var resultSet = statement.executeQuery();
                            while (resultSet.next()) {
                                var key = new ReactionAndUser(resultSet.getString("react"), resultSet.getLong("user_id"));
                                if (userReactions.containsKey(key)) {
                                    reply.append("User **").append(MarkdownSanitizer.escape(userReactions.get(key))).append("** reacted with ")
                                            .append(key.reaction()).append(" on <t:").append(resultSet.getLong("timestamp") / 1000).append(":F>");
                                    if (resultSet.getBoolean("uncertain")) {
                                        reply.append(" (uncertain)");
                                    }
                                    reply.append("\n");
                                }
                            }
                        } catch (SQLException e) {
                            throw new RuntimeException(e);
                        }
                        event.getHook().sendMessage(new MessageCreateBuilder()
                                .setContent(reply.toString())
                                .setAllowedMentions(Collections.emptyList())
                                .build()
                        ).queue();
                    });
                });
            }
        }
        if (!isValidChannel) {
            event.reply("This command can only be used in a meetup thread.").queue();
        }
    }
}
