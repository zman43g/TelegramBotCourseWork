package pro.sky.telegrambot.service;

import pro.sky.telegrambot.model.NotificationTask;
import pro.sky.telegrambot.repository.NotificationTaskRepository;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class NotificationTaskService {
    private static final Logger logger = LoggerFactory.getLogger(NotificationTaskService.class);
    private final NotificationTaskRepository repository;


    private static final Pattern MESSAGE_PATTERN =
            Pattern.compile("([0-9\\.\\:\\s]{16})\\s(.+)", Pattern.DOTALL);


    private static final DateTimeFormatter DATE_TIME_FORMATTER =
            DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");

    public NotificationTaskService(NotificationTaskRepository repository) {
        this.repository = repository;
    }

    @Transactional
    public String parseAndSaveNotification(Long chatId, String messageText) {
        try {

            ParsedMessage parsedMessage = parseMessage(messageText);

            if (parsedMessage == null) {
                return "Некорректный формат сообщения. Используйте: дд.мм.гггг чч:мм Текст напоминания";
            }

            if (parsedMessage.dateTime.isBefore(LocalDateTime.now())) {
                return "Дата напоминания не может быть в прошлом!";
            }


            NotificationTask task = new NotificationTask(
                    chatId,
                    parsedMessage.text,
                    parsedMessage.dateTime
            );

            repository.save(task);

            logger.info("Сохранено напоминание: chatId={}, date={}, text={}",
                    chatId, parsedMessage.dateTime, parsedMessage.text);

            return String.format("Напоминание создано на %s: %s",
                    parsedMessage.dateTime.format(DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm")),
                    parsedMessage.text);

        } catch (DateTimeParseException e) {
            logger.error("Ошибка парсинга даты: {}", e.getMessage());
            return "Некорректный формат даты. Используйте: дд.мм.гггг чч:мм";
        } catch (Exception e) {
            logger.error("Ошибка при сохранении напоминания: {}", e.getMessage(), e);
            return "Произошла ошибка при сохранении напоминания";
        }
    }


    private ParsedMessage parseMessage(String message) {
        Matcher matcher = MESSAGE_PATTERN.matcher(message.trim());

        if (!matcher.matches() || matcher.groupCount() < 2) {
            return null;
        }

        String dateTimeStr = matcher.group(1).trim();
        String text = matcher.group(2).trim();

        LocalDateTime dateTime = LocalDateTime.parse(dateTimeStr, DATE_TIME_FORMATTER);

        return new ParsedMessage(dateTime, text);
    }

    public List<NotificationTask> getUserNotifications(Long chatId) {
        return repository.findByChatIdAndIsSentFalse(chatId);
    }


    public List<NotificationTask> getTasksForNotification() {
        return repository.findTasksForNotification(LocalDateTime.now());
    }

    @Transactional
    public void markAsSent(Long taskId) {
        repository.findById(taskId).ifPresent(task -> {
            task.markAsSent();
            repository.save(task);
            logger.info("Напоминание {} помечено как отправленное", taskId);
        });
    }


    private static class ParsedMessage {
        final LocalDateTime dateTime;
        final String text;

        ParsedMessage(LocalDateTime dateTime, String text) {
            this.dateTime = dateTime;
            this.text = text;
        }
    }
}
