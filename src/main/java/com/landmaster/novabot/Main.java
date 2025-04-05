package com.landmaster.novabot;

import com.landmaster.novabot.listeners.ListReactsCommand;
import com.landmaster.novabot.listeners.ReactListener;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.requests.GatewayIntent;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.EnumSet;
import java.util.concurrent.Executors;

import static com.landmaster.novabot.util.EnvVars.NOVABOT_TOKEN;

public class Main {
    public static void main(String[] args) {
        try (
            // create a database connection
            Connection connection = DriverManager.getConnection("jdbc:sqlite:react_tracker.db")
        ) {
            connection.setAutoCommit(true);
            try (Statement statement = connection.createStatement()) {
                statement.executeUpdate("CREATE TABLE IF NOT EXISTS reacts(thread_id INTEGER, react TEXT, user_id INTEGER, timestamp INTEGER, uncertain INTEGER, PRIMARY KEY (thread_id, react, user_id))");
            }
            var jda = JDABuilder.createLight(NOVABOT_TOKEN, EnumSet.of(GatewayIntent.GUILD_MESSAGE_REACTIONS))
                    .setCallbackPool(Executors.newSingleThreadExecutor(), true)
                    .addEventListeners(new ReactListener(connection), new ListReactsCommand(connection))
                    .build();
            jda.upsertCommand("list-reacts", "List reacts to the current meetup thread in chronological order.").queue();
            boolean continueWaiting = false;
            do {
                try {
                    jda.awaitShutdown();
                } catch (InterruptedException e) {
                    continueWaiting = true;
                }
            } while (continueWaiting);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}