import MashupShared
import SwiftUI
import UserNotifications
import Presentation

@main
struct Mashup: App {
    init() {
        // Lấy biến môi trường từ Info.plist (được inject bởi xcconfig)
        let environment = (Bundle.main.object(forInfoDictionaryKey: "ENV") as? String) ?? "PROD"
        
        // Khởi tạo Koin một lần duy nhất khi App start
        // Sử dụng KoinInitializer (class Swift-friendly mà chúng ta vừa tạo)
        KoinInitializer().start(envName: environment)
        
        // Nếu muốn cấu hình thêm cho Notification center delegate (tùy chọn)
        UNUserNotificationCenter.current().delegate = NotificationDelegate.shared
    }
    
    var body: some Scene {
        WindowGroup {
            RootFlowView()
        }
    }
}

// Delegate đơn giản để xử lý notification khi app đang chạy foreground
class NotificationDelegate: NSObject, UNUserNotificationCenterDelegate {
    static let shared = NotificationDelegate()
    
    func userNotificationCenter(
        _ center: UNUserNotificationCenter,
        willPresent notification: UNNotification,
        withCompletionHandler completionHandler: @escaping (UNNotificationPresentationOptions) -> Void
    ) {
        // Hiển thị notification banner ngay cả khi app đang mở
        completionHandler([.banner, .sound, .badge])
    }
}
