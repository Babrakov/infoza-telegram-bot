package ru.infoza.bot.config.state;

import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
public class BotStateContext {
    private final Map<Long, BotState> userStates = new HashMap<>();

    public BotState getUserState(long userId) {
        return userStates.getOrDefault(userId, BotState.START); // START - начальное состояние
    }

    public void setUserState(long userId, BotState state) {
        userStates.put(userId, state);
    }
}
