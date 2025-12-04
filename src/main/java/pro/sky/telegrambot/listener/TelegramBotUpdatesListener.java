package pro.sky.telegrambot.listener;

import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.UpdatesListener;
import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.model.Message;
import com.pengrad.telegrambot.model.Chat;
import com.pengrad.telegrambot.model.User;
import com.pengrad.telegrambot.request.SendMessage;
import com.pengrad.telegrambot.response.SendResponse;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import pro.sky.telegrambot.model.NotificationTask;
import pro.sky.telegrambot.service.NotificationTaskService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import javax.annotation.PostConstruct;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
public class TelegramBotUpdatesListener implements UpdatesListener {

    private static final Logger logger = LoggerFactory.getLogger(TelegramBotUpdatesListener.class);

    private final TelegramBot telegramBot;
    private final NotificationTaskService notificationTaskService;

    @Autowired
    public TelegramBotUpdatesListener(TelegramBot telegramBot, NotificationTaskService notificationTaskService) {
        this.telegramBot = telegramBot;
        this.notificationTaskService = notificationTaskService;
    }

    @PostConstruct
    public void init() {
        telegramBot.setUpdatesListener(this);
        logger.info("TelegramBotUpdatesListener initialized");
    }
    @Override
    public int process(List<Update> updates) {
        if (updates == null || updates.isEmpty()) {
            return UpdatesListener.CONFIRMED_UPDATES_ALL;
        }
        int processedCount = 0;

        for (Update update : updates) {
            try {
                processSingleUpdate(update);
                processedCount++;
            } catch (TelegramApiException e) {
                logger.error("Failed to process update {} due to Telegram error: {}",
                        update.updateId(), e.getMessage());
            } catch (Exception e) {
                logger.error("Failed to process update {}: {}",
                        update.updateId(), e.getMessage(), e);
            }
        }

        logger.info("Processed {} updates", processedCount);
        return UpdatesListener.CONFIRMED_UPDATES_ALL;
    }

    private void processSingleUpdate(Update update) throws TelegramApiException {
        logger.debug("Processing update ID: {}", update.updateId());

        Message message = update.message();
        if (message == null || message.text() == null) {
            return;
        }

        Chat chat = message.chat();
        if (chat == null) {
            return;
        }

        Long chatId = chat.id();
        String messageText = message.text().trim();
        User user = message.from();
        String userName = getUserName(user);

        logger.info("Message from {} ({}): {}", userName, chatId, messageText);

        if ("/start".equals(messageText)) {
            handleStartCommand(chatId, userName);
        }
        else if ("/myreminders".equals(messageText) || "/list".equals(messageText)) {
            handleListCommand(chatId);
        }
        else if (!messageText.startsWith("/")) {
            handleReminderMessage(chatId, messageText);
        }
    }

    private void handleReminderMessage(Long chatId, String messageText) {
        String response = notificationTaskService.parseAndSaveNotification(chatId, messageText);
        sendMessage(chatId, response);

        logger.info("Обработано напоминание от chatId={}: {}", chatId, messageText);
    }


    private void handleListCommand(Long chatId) {
        List<NotificationTask> notifications = notificationTaskService.getUserNotifications(chatId);

        if (notifications.isEmpty()) {
            sendMessage(chatId, "У вас нет активных напоминаний.");
            return;
        }

        StringBuilder response = new StringBuilder("Ваши напоминания:\n\n");

        for (NotificationTask notification : notifications) {
            response.append("• ")
                    .append(DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm")
                            .withZone(ZoneId.systemDefault())
                            .format(notification.getNotificationDateTime()))
                    .append(": ")
                    .append(notification.getMessageText())
                    .append("\n");
        }

        sendMessage(chatId, response.toString());
    }

    private void handleStartCommand(Long chatId, String userName) {
        String welcomeText = String.format(
                "Доброго дня, %s! 👋\n\n" +
                        "Я бот для напоминаний. Отправь мне сообщение в формате:\n\n" +
                        "дд.мм.гггг чч:мм Текст напоминания\n\n" +
                        "Например:\n" +
                        "25.12.2023 20:00 Поздравить с праздником\n\n" +
                        "Доступные команды:\n" +
                        "/start - начало работы\n" +
                        "/myreminders - показать мои напоминания\n" ,
                userName != null ? userName : "Друг"
        );

        sendMessage(chatId, welcomeText);
    }


    private String getUserName(User user) {
        if (user == null) {
            return "Пользователь";
        }
        String firstName = user.firstName();
        return (firstName != null && !firstName.isEmpty()) ? firstName : "Пользователь";
    }

    private void sendMessage(Long chatId, String text) {
        SendMessage request = new SendMessage(chatId, text);

        try {
            SendResponse response = telegramBot.execute(request);
            if (response.isOk()) {
                logger.debug("Message sent to chatId: {}", chatId);
            } else {
                logger.error("Failed to send message. Error: {}", response.description());
            }
        } catch (Exception e) {
            logger.error("Exception while sending message to chat {}: {}",
                    chatId, e.getMessage(), e);
        }
    }
}


