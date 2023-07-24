package telegram.bot.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;
import org.springframework.data.redis.core.index.Indexed;
import telegram.bot.enums.TaskStatus;

import java.io.Serializable;
import java.time.LocalDateTime;

@RedisHash("task")
@Data
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder
public class Task implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Indexed
    private String userId;
    private LocalDateTime timeToSend;
    @Indexed
    @Enumerated(EnumType.STRING)
    private TaskStatus taskStatus;
    private String taskText;
}
