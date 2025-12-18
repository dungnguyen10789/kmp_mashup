import SwiftUI

public struct StoryboardSplashView: UIViewControllerRepresentable {
	
	public init() {}

	public func makeUIViewController(context: Context) -> UIViewController {
        return SplashController()
    }

	public func updateUIViewController(_ uiViewController: UIViewController, context: Context) {}
}
