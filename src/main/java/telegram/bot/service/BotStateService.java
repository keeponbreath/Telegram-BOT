package telegram.bot.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import telegram.bot.entity.BotState;
import telegram.bot.repository.BotStateRepository;

import java.util.Optional;

@Service
public class BotStateService {
    private final BotStateRepository repo;

    @Autowired
    public BotStateService(BotStateRepository repo) {
        this.repo = repo;
    }

    public Optional<BotState> getBotStateById(String chatId) {
        return repo.findById(chatId);
    }

    public void save(BotState state) {
        if(getBotStateById(state.getId()).isEmpty()) {
            repo.save(state);
        }
    }

    public void delete(BotState state) {
        repo.delete(state);
    }
}