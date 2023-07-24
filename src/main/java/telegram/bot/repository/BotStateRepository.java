package telegram.bot.repository;

import org.springframework.data.repository.CrudRepository;
import telegram.bot.entity.BotState;

public interface BotStateRepository extends CrudRepository<BotState, String> {
}
