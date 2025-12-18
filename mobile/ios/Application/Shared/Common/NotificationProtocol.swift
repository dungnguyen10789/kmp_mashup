import Foundation
import UserNotifications

public protocol NotificationReadable {
	var permissionStatus: UNAuthorizationStatus { get }
	var deviceToken: String? { get set }
}


public protocol NotificationControllable {
	func requestPermission(_ completion: @escaping (UNAuthorizationStatus) -> Void)
	func checkPermission(_ completion: @escaping (UNAuthorizationStatus) -> Void)
	func openSettings()
	func setDeviceToken(_ token: String)
	func scheduleLocalNotification(
		title: String,
		body: String,
		after seconds: TimeInterval
	)
}
