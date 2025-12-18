package vn.dna.kmp_mashup.core.service

import platform.UserNotifications.UNAuthorizationOptionAlert
import platform.UserNotifications.UNAuthorizationOptionBadge
import platform.UserNotifications.UNAuthorizationOptionSound
import platform.UserNotifications.UNMutableNotificationContent
import platform.UserNotifications.UNNotificationRequest
import platform.UserNotifications.UNTimeIntervalNotificationTrigger
import platform.UserNotifications.UNUserNotificationCenter
import vn.dna.kmp_mashup.domain.service.NotificationService
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

actual class NotificationServiceImpl : NotificationService {

    actual override suspend fun requestPermission(): Boolean = suspendCoroutine { continuation ->
        val center = UNUserNotificationCenter.currentNotificationCenter()
        val options = UNAuthorizationOptionAlert or UNAuthorizationOptionSound or UNAuthorizationOptionBadge
        center.requestAuthorizationWithOptions(options) { granted, error ->
            if (error != null) {
                println("Notification permission error: ${error.localizedDescription}")
                continuation.resume(false)
            } else {
                continuation.resume(granted)
            }
        }
    }

    actual override fun scheduleNotification(id: String, title: String, body: String, delayMillis: Long) {
        val content = UNMutableNotificationContent().apply {
            setTitle(title)
            setBody(body)
            setSound(platform.UserNotifications.UNNotificationSound.defaultSound)
        }

        // Convert delayMillis to seconds
        val timeInterval = delayMillis / 1000.0
        // Trigger must be at least 0.1s
        val triggerDelay = if (timeInterval <= 0) 1.0 else timeInterval

        val trigger = UNTimeIntervalNotificationTrigger.triggerWithTimeInterval(
            timeInterval = triggerDelay,
            repeats = false
        )

        val request = UNNotificationRequest.requestWithIdentifier(
            identifier = id,
            content = content,
            trigger = trigger
        )

        UNUserNotificationCenter.currentNotificationCenter().addNotificationRequest(request) { error ->
            if (error != null) {
                println("Error scheduling notification: ${error.localizedDescription}")
            }
        }
    }
}
