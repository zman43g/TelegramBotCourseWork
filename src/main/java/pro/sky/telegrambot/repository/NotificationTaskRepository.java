package pro.sky.telegrambot.repository;

import pro.sky.telegrambot.model.NotificationTask;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;

@Repository
public interface NotificationTaskRepository extends JpaRepository<NotificationTask, Long> {


    // Найти задачи для отправки по указанному времени

    List<NotificationTask> findByNotificationDateTimeLessThanEqualAndIsSentFalse(Instant dateTime);
    List<NotificationTask> findByIsSentFalseAndNotificationDateTimeBefore(Instant dateTime);

//    Найти все задачи по chatId

    List<NotificationTask> findByChatId(Long chatId);

    List<NotificationTask> findByChatIdAndIsSentFalse(Long chatId);

   //  Найти задачи для отправки с помощью JPQL запроса

    @Query("SELECT nt FROM NotificationTask nt WHERE nt.notificationDateTime <= :currentTime AND nt.isSent = false")
    List<NotificationTask> findTasksForNotification(@Param("currentTime") Instant currentTime);
}