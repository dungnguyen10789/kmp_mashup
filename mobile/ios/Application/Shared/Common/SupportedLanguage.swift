import Foundation

public enum SupportedLanguage: String, CaseIterable, Identifiable {
	case english = "en"
	case vietnamese = "vi"
	
	public var id: String { rawValue }
	
	var displayName: String {
		switch self {
		case .english:
			return "English"
		case .vietnamese:
			return "Tiếng Việt"
		}
	}
}

public extension SupportedLanguage {
		/// Map từ Locale bất kỳ (\"en\", \"en-US\", \"vi-VN\" ...) về SupportedLanguage, nếu có.
	static func from(locale: Locale) -> SupportedLanguage? {
			// 1. ưu tiên languageCode nếu có (iOS 16+)
		if let code = locale.language.languageCode?.identifier.lowercased() {
			return SupportedLanguage(rawValue: code)
		}
		
			// 2. fallback cho iOS cũ: dùng identifier
		let id = locale.identifier.lowercased()
		if id.hasPrefix("vi") { return .vietnamese }
		if id.hasPrefix("en") { return .english }
		
		return nil
	}
}

public let DEFAULT_LANGUAGE: SupportedLanguage = .english
