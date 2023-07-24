package telegram.bot.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import telegram.bot.enums.TaskStatus;
import telegram.bot.repository.TasksRepository;
import telegram.bot.entity.Task;

import java.util.List;

@Service
public class TasksService {
    private final TasksRepository repository;

    @Autowired
    public TasksService(TasksRepository repository) {
        this.repository = repository;
    }

    public void save(Task task) {
        repository.save(task);
    }

    public void delete(Task task) {
        repository.delete(task);
    }

    public List<Task> getAllTasksByUserId(String userId) {
        return repository.getTasksByUserId(userId);
    }

    public List<Task> getAllUncompletedTasks() {
        return repository.getTasksByTaskStatus(TaskStatus.CREATED);
    }

    public List<Task> getUncompletedTasksByUserId(String userId) {
        return repository.getTasksByUserIdAndTaskStatus(userId, TaskStatus.CREATED);
    }
}
