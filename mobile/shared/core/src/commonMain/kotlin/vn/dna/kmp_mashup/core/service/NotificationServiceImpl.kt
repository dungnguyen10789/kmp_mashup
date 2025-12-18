package vn.dna.kmp_mashup.core.service

import vn.dna.kmp_mashup.domain.service.NotificationService

expect class NotificationServiceImpl : NotificationService {
    override suspend fun requestPermission(): Boolean
    override fun scheduleNotification(id: String, title: String, body: String, delayMillis: Long)
}
