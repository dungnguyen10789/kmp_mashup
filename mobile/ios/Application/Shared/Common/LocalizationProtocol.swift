public protocol LocalizationReadable {
	var currentLanguage: SupportedLanguage? { get }
}

public protocol LocalizationControllable {
	func setLanguage(_ code: SupportedLanguage)
}
