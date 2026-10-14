package com.example.data.repository

import com.example.data.local.dao.NotificationDao
import com.example.data.local.entity.NotificationEntity
import kotlinx.coroutines.flow.Flow

class NotificationRepository(private val notificationDao: NotificationDao) {

    val allNotifications: Flow<List<NotificationEntity>> = notificationDao.getAllNotifications()

    val unreadCount: Flow<Int> = notificationDao.getUnreadCount()

    suspend fun insertNotification(notification: NotificationEntity): Long =
        notificationDao.insertNotification(notification)

    suspend fun markAsRead(id: Long) = notificationDao.markAsRead(id)

    suspend fun markAllAsRead() = notificationDao.markAllAsRead()

    suspend fun deleteNotification(notification: NotificationEntity) =
        notificationDao.deleteNotification(notification)

    suspend fun deleteById(id: Long) =
        notificationDao.deleteNotificationById(id)

    suspend fun clearAll() = notificationDao.clearAll()
}
