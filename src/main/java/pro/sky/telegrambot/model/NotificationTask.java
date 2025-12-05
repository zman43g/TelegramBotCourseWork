package pro.sky.telegrambot.model;

import javax.persistence.*;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Objects;

@Entity
@Table(name = "notification_task")
public class NotificationTask {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "chat_id", nullable = false)
    private Long chatId;

    @Column(name = "message_text", nullable = false, columnDefinition = "TEXT")
    private String messageText;

    @Column(name = "notification_date_time", nullable = false)
    private Instant notificationDateTime;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "is_sent", nullable = false)
    private boolean isSent = false;

    public NotificationTask() {
    }

    public NotificationTask(Long chatId, String messageText, Instant notificationDateTime) {
        this.chatId = chatId;
        this.messageText = messageText;
        this.notificationDateTime = notificationDateTime;
    }

    public void markAsSent() {
        this.isSent = true;
    }

    public boolean shouldBeSentNow() {
        return !isSent && notificationDateTime.isBefore(Instant.now().plusSeconds(60));
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getChatId() {
        return chatId;
    }

    public void setChatId(Long chatId) {
        this.chatId = chatId;
    }

    public String getMessageText() {
        return messageText;
    }

    public void setMessageText(String messageText) {
        this.messageText = messageText;
    }

    public Instant getNotificationDateTime() {
        return notificationDateTime;
    }

    public void setNotificationDateTime(Instant notificationDateTime) {
        this.notificationDateTime = notificationDateTime;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public Boolean getSent() {
        return isSent;
    }

    public void setSent(Boolean sent) {
        isSent = sent;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        NotificationTask that = (NotificationTask) o;
        return Objects.equals(id, that.id) &&
                Objects.equals(chatId, that.chatId) &&
                Objects.equals(notificationDateTime, that.notificationDateTime);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, chatId, notificationDateTime);
    }

    @Override
    public String toString() {
        return "NotificationTask{" +
                "id=" + id +
                ", chatId=" + chatId +
                ", messageText='" + messageText + '\'' +
                ", notificationDateTime=" + notificationDateTime +
                ", createdAt=" + createdAt +
                ", isSent=" + isSent +
                '}';
    }
}