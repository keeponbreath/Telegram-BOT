package telegram.bot.base;

import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;
import telegram.bot.entity.BotState;
import telegram.bot.entity.Task;
import telegram.bot.enums.TaskStatus;
import telegram.bot.service.BotStateService;
import telegram.bot.service.TasksService;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
@PropertySource("classpath:bot.properties")
public class MyBot extends TelegramLongPollingBot {
    @Value("${bot.name}")
    private String botName;
    @Value("${bot.token}")
    private String botToken;
    @Autowired
    private TasksService tasksService;
    @Autowired
    private BotStateService botStateService;

    public static void run() {
        new MyBot();
    }

    @Override
    public void onUpdateReceived(Update update) {
        var msg = update.getMessage();
        if(msg.isCommand()) {
            switch (msg.getText()) {
                case "/start" -> {
                    SendMessage greeting = SendMessage.builder()
                            .chatId(String.valueOf(msg.getFrom().getId()))
                            .text("Hello, " + msg.getFrom().getFirstName() + "! Nice to meet you!")
                            .build();
                    checkForPresence(msg, greeting);
                }
                case "/create" -> {
                    SendMessage creation = SendMessage.builder()
                            .chatId(String.valueOf(msg.getFrom().getId()))
                            .text("To create a task just send me a message in next format:\n"
                                    + "dd.MM.yyyy HH:MM Text of your task")
                            .build();
                    BotState botState = new BotState();
                    botState.setId(String.valueOf(msg.getChatId()));
                    botState.setLastMessage("/create");
                    botStateService.save(botState);
                    try {
                        execute(creation);
                    } catch (TelegramApiException e) {
                        throw new RuntimeException(e.getMessage());
                    }
                }
                case "/tasks" -> {
                    List<Task> uncompletedTasks = tasksService
                            .getUncompletedTasksByUserId(String.valueOf(msg.getFrom().getId()));
                    for (Task task : uncompletedTasks) {
                        SendMessage tasks = SendMessage.builder()
                                .chatId(String.valueOf(msg.getFrom().getId()))
                                .text("Date and time: " + task.getTimeToSend()
                                        .truncatedTo(ChronoUnit.MINUTES) + "\n" + "Task: "
                                        + task.getTaskText())
                                .build();
                        checkForPresence(msg, tasks);
                    }
                }
                case "/alltasks" -> {
                    List<Task> allTasks = tasksService
                            .getAllTasksByUserId(String.valueOf(msg.getFrom().getId()));
                    for (Task task : allTasks) {
                        SendMessage tasks = SendMessage.builder()
                                .chatId(String.valueOf(msg.getFrom().getId()))
                                .text("Date and time: " + task.getTimeToSend()
                                        .truncatedTo(ChronoUnit.MINUTES) + "\n"
                                        + "Task: " + task.getTaskText() + "\n" + "Status: "
                                        + task.getTaskStatus())
                                .build();
                        checkForPresence(msg, tasks);
                    }
                }
            }
        } else {
            Optional<BotState> botState = botStateService
                    .getBotStateById(String.valueOf(msg.getChatId()));
            if(botState.isPresent() && botState.get().getId()!=null) {
                if (botState.get().getLastMessage().equals("/create")) {
                    Pattern pattern = Pattern
                            .compile("\\d{2}\\.\\d{2}\\.\\d{4}\\s\\d{2}:\\d{2}.+");
                    Matcher matcher = pattern.matcher(msg.getText());
                    if (matcher.matches()) {
                        DateTimeFormatter formatter = DateTimeFormatter
                                .ofPattern("dd.MM.yyyy HH:mm");
                        LocalDateTime timeToSend = LocalDateTime
                                .parse(msg.getText().substring(0, 16), formatter);
                        if (timeToSend.isAfter(LocalDateTime.now())) {
                            Task task = Task.builder()
                                    .userId(String.valueOf(msg.getFrom().getId()))
                                    .timeToSend(timeToSend)
                                    .taskStatus(TaskStatus.CREATED)
                                    .taskText(msg.getText().substring(17))
                                    .build();
                            tasksService.save(task);
                            botStateService.delete(botState.get());
                            SendMessage creationSuccess = SendMessage.builder()
                                    .chatId(String.valueOf(msg.getFrom().getId()))
                                    .text("Task created! I'll remind you of it 15 minutes before!")
                                    .build();
                            try {
                                execute(creationSuccess);
                            } catch (TelegramApiException e) {
                                throw new RuntimeException(e.getMessage());
                            }
                        } else {
                            SendMessage creationException = SendMessage.builder()
                                    .chatId(String.valueOf(msg.getFrom().getId()))
                                    .text("You have to specify time in future. Try it again")
                                    .build();
                            try {
                                execute(creationException);
                            } catch (TelegramApiException e) {
                                throw new RuntimeException(e.getMessage());
                            }
                        }
                    } else {
                        SendMessage creationFailure = SendMessage.builder()
                                .chatId(String.valueOf(msg.getFrom().getId()))
                                .text("Something went wrong!\n Try it again with the format:"
                                        + "DD.MM HH:MM Text of your task")
                                .build();
                        try {
                            execute(creationFailure);
                        } catch (TelegramApiException e) {
                            throw new RuntimeException(e.getMessage());
                        }
                    }
                }
            }
        }
    }

    private void checkForPresence(Message msg, SendMessage tasks) {
        if(botStateService.getBotStateById(msg.getChatId().toString())
                .isPresent()) {
            BotState state = botStateService
                    .getBotStateById(msg.getChatId().toString()).get();
            botStateService.delete(state);
        }
        try {
            execute(tasks);
        } catch (TelegramApiException e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    @Scheduled(fixedDelay = 1000 * 60)
    public void sendMessages() {
        tasksService.getAllUncompletedTasks().parallelStream()
                .filter(task -> task.getTimeToSend().truncatedTo(ChronoUnit.MINUTES)
                        .minusMinutes(15L).isBefore(LocalDateTime.now()))
                .forEach(task -> {
                    SendMessage taskReminder = SendMessage.builder()
                            .chatId(String.valueOf(task.getUserId()))
                            .text(task.getTaskText())
                            .build();
                    task.setTaskStatus(TaskStatus.COMPLETED);
                    tasksService.save(task);
                    try {
                        execute(taskReminder);
                    } catch (TelegramApiException e) {
                        throw new RuntimeException(e.getMessage());
                    }
                });
    }

    @Override
    public String getBotUsername() {
        return botName;
    }

    @Override
    public String getBotToken() {
        return botToken;
    }

    @PostConstruct
    public void registerBot() throws TelegramApiException {
        TelegramBotsApi botsApi = new TelegramBotsApi(DefaultBotSession.class);
        try {
            botsApi.registerBot(this);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }
}