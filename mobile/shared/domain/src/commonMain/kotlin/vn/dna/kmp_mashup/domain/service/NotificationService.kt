package vn.dna.kmp_mashup.domain.service

interface NotificationService {
    suspend fun requestPermission(): Boolean
    fun scheduleNotification(id: String, title: String, body: String, delayMillis: Long)
}
