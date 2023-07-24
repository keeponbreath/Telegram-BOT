package telegram.bot.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;

import java.io.Serializable;

@RedisHash("state")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class BotState implements Serializable {
    @Id
    private String id;
    private String lastMessage;
}
