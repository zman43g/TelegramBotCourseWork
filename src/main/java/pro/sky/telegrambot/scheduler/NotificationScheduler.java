package pro.sky.telegrambot.scheduler;

import org.springframework.dao.DataAccessException;
import pro.sky.telegrambot.model.NotificationTask;
import pro.sky.telegrambot.service.NotificationTaskService;
import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.request.SendMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Component
public class NotificationScheduler {

    private static final Logger logger = LoggerFactory.getLogger(NotificationScheduler.class);
    private static final DateTimeFormatter FORMATTER =
            DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");

    private final NotificationTaskService notificationTaskService;
    private final TelegramBot telegramBot;

    public NotificationScheduler(NotificationTaskService notificationTaskService,
                                 TelegramBot telegramBot) {
        this.notificationTaskService = notificationTaskService;
        this.telegramBot = telegramBot;
    }

    @Scheduled(cron = "0 0/1 * * * *")
    public void checkAndSendNotifications() {
        logger.debug("Checking for notifications to send at {}", LocalDateTime.now());

        List<NotificationTask> tasks = notificationTaskService.getTasksForNotification();

        if (tasks.isEmpty()) {
            logger.debug("No notifications to send");
            return;
        }

        logger.info("Found {} notifications to send", tasks.size());

        for (NotificationTask task : tasks) {
            try {
                String message = String.format(" *Напоминание!*\n\n%s",
                        task.getMessageText());

                SendMessage sendMessage = new SendMessage(task.getChatId(), message);
                telegramBot.execute(sendMessage);

                notificationTaskService.markAsSent(task.getId());

                logger.info("Notification sent: chatId={}, text={}",
                        task.getChatId(), task.getMessageText());

            }  catch (DataAccessException e) {
                logger.error("Database error while marking notification as sent for task {}: {}",
                        task.getId(), e.getMessage());
            } catch (Exception e) {
                logger.error("Unexpected error while processing notification task {}: {}",
                        task.getId(), e.getMessage());
            }
        }
    }
}
