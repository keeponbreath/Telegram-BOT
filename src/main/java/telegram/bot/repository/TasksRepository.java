package telegram.bot.repository;

import org.springframework.data.repository.CrudRepository;
import telegram.bot.entity.Task;
import telegram.bot.enums.TaskStatus;

import java.util.List;

public interface TasksRepository extends CrudRepository<Task, Long> {
    List<Task> getTasksByUserId(String userId);
    List<Task> getTasksByTaskStatus(TaskStatus status);
    List<Task> getTasksByUserIdAndTaskStatus(String userId, TaskStatus status);
}