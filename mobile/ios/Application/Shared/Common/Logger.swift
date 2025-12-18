import Foundation

public enum LogCategory: String {
	case network
	case deeplink
	case keychain
	case notification
	case security
	case ui
	case app
}

public enum LogSeverity: String {
	case debug
	case info
	case warning
	case error
}

public protocol LogControllable {
	static func debug(
		_ message: @autoclosure () -> String,
		category: LogCategory?
	)
	
	static func info(
		_ message: @autoclosure () -> String,
		category: LogCategory?
	)
	
	static func warn(
		_ message: @autoclosure () -> String,
		category: LogCategory?
	)
	
	static func error(
		_ message: @autoclosure () -> String,
		error: Error?,
		category: LogCategory?
	)
}

public struct Logger: LogControllable {
	private static func color(for severity: LogSeverity) -> String {
		switch severity {
		case .debug: return "🟤"
		case .info: return "🟢"
		case .warning: return "🟡"
		case .error: return "🔴"
		}
	}
	
	public static func debug(
		_ message: @autoclosure () -> String,
		category: LogCategory? = .app
	) {
		self.log(message(), category: category, severity: .debug)
	}
	
	public static func info(
		_ message: @autoclosure () -> String,
		category: LogCategory? = .app
	) {
		self.log(message(), category: category, severity: .info)
	}
	
	public static func warn(
		_ message: @autoclosure () -> String,
		category: LogCategory? = .app
	) {
		self.log(
			message(),
			category: category,
			severity: .warning
		)
	}
	
	public static func error(
		_ message: @autoclosure () -> String,
		error: Error? = nil,
		category: LogCategory? = .app
		
	) {
		self.log(message(), category: category, severity: .error, error: error)
	}
	
	private static func log(
		_ message: @autoclosure () -> String,
		category: LogCategory? = .app,
		severity: LogSeverity = .debug,
		error: Error? = nil
	) {
#if DEBUG
		let ts = Date()
		let dateStr = DateFormatter.cached.string(from: ts)
		
		let categoryText = category?.rawValue.uppercased()
		let ball = color(for: severity)
		
		var prefix =
		"[\(dateStr)] \(ball)"
		
		if let categoryText = categoryText {
			prefix += " [\(categoryText)]"
		}
		
		if let error {
			print("\(prefix) \(message()) | error=\(error) 🔶")
		} else {
			print("\(prefix) \(message()) 🔶")
		}
		
#endif
	}
}

extension DateFormatter {
	fileprivate static let cached: DateFormatter = {
		let df = DateFormatter()
		df.dateFormat = "HH:mm:ss.SSS"
		return df
	}()
}
