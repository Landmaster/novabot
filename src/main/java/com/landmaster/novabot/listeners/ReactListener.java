package com.landmaster.novabot.listeners;

import net.dv8tion.jda.api.entities.channel.concrete.ThreadChannel;
import net.dv8tion.jda.api.events.message.react.MessageReactionAddEvent;
import net.dv8tion.jda.api.events.message.react.MessageReactionRemoveAllEvent;
import net.dv8tion.jda.api.events.message.react.MessageReactionRemoveEmojiEvent;
import net.dv8tion.jda.api.events.message.react.MessageReactionRemoveEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class ReactListener extends ListenerAdapter {
    private final Connection connection;

    public ReactListener(Connection connection) {
        this.connection = connection;
    }

    @Override
    public void onMessageReactionAdd(MessageReactionAddEvent event) {
        if (event.getChannel() instanceof ThreadChannel threadChannel) {
            if (threadChannel.getParentChannel().getId().equals(System.getenv("NOVABOT_MEETUP_FORUM"))) {
                threadChannel.retrieveStartMessage().queue(message -> {
                    if (message.getIdLong() == event.getMessageIdLong()) {
                        try (PreparedStatement statement = connection.prepareStatement("INSERT OR REPLACE INTO reacts VALUES (?, ?, ?, ?, 0)")) {
                            statement.setLong(1, threadChannel.getIdLong());
                            statement.setString(2, event.getReaction().getEmoji().getFormatted());
                            statement.setLong(3, event.getUserIdLong());
                            statement.setLong(4, System.currentTimeMillis());
                            statement.execute();
                        } catch (SQLException e) {
                            throw new RuntimeException(e);
                        }
                    }
                });
            }
        }
    }

    @Override
    public void onMessageReactionRemove(MessageReactionRemoveEvent event) {
        if (event.getChannel() instanceof ThreadChannel threadChannel) {
            if (threadChannel.getParentChannel().getId().equals(System.getenv("NOVABOT_MEETUP_FORUM"))) {
                threadChannel.retrieveStartMessage().queue(message -> {
                    if (message.getIdLong() == event.getMessageIdLong()) {
                        try (PreparedStatement statement = connection.prepareStatement("DELETE FROM reacts WHERE (thread_id, react, user_id) = (?, ?, ?)")) {
                            statement.setLong(1, threadChannel.getIdLong());
                            statement.setString(2, event.getReaction().getEmoji().getFormatted());
                            statement.setLong(3, event.getUserIdLong());
                            statement.execute();
                        } catch (SQLException e) {
                            throw new RuntimeException(e);
                        }
                    }
                });
            }
        }
    }

    @Override
    public void onMessageReactionRemoveAll(MessageReactionRemoveAllEvent event) {
        if (event.getChannel() instanceof ThreadChannel threadChannel) {
            if (threadChannel.getParentChannel().getId().equals(System.getenv("NOVABOT_MEETUP_FORUM"))) {
                threadChannel.retrieveStartMessage().queue(message -> {
                    if (message.getIdLong() == event.getMessageIdLong()) {
                        try (PreparedStatement statement = connection.prepareStatement("DELETE FROM reacts WHERE thread_id = ?")) {
                            statement.setLong(1, threadChannel.getIdLong());
                            statement.execute();
                        } catch (SQLException e) {
                            throw new RuntimeException(e);
                        }
                    }
                });
            }
        }
    }

    @Override
    public void onMessageReactionRemoveEmoji(MessageReactionRemoveEmojiEvent event) {
        if (event.getChannel() instanceof ThreadChannel threadChannel) {
            if (threadChannel.getParentChannel().getId().equals(System.getenv("NOVABOT_MEETUP_FORUM"))) {
                threadChannel.retrieveStartMessage().queue(message -> {
                    if (message.getIdLong() == event.getMessageIdLong()) {
                        try (PreparedStatement statement = connection.prepareStatement("DELETE FROM reacts WHERE (thread_id, react) = (?, ?)")) {
                            statement.setLong(1, threadChannel.getIdLong());
                            statement.setString(2, event.getReaction().getEmoji().getFormatted());
                            statement.execute();
                        } catch (SQLException e) {
                            throw new RuntimeException(e);
                        }
                    }
                });
            }
        }
    }

}
