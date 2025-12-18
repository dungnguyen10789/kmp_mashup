import SwiftUI
import MashupShared
import Combine

@MainActor
public class LoginViewModelWrapper: ObservableObject {
	@Published public var username: String = "conandk1"
	@Published public var password: String = "Conandk1@"
	@Published public var isLoading: Bool = false
	@Published public var errorMessage: String?
		
	private let viewModel: LoginViewModel
	
	public init(viewModel: LoginViewModel) {
		self.viewModel = viewModel
	}
	
	public func login() async {
		guard !username.isEmpty, !password.isEmpty else {
			errorMessage = "Username and password required"
			return
		}
		
		isLoading = true
		errorMessage = nil
		
		await withCheckedContinuation { (cont: CheckedContinuation<Void, Never>) in
			viewModel.login(
				username: username,
				password: password,
				onSuccess: { _ in
					Task { @MainActor in
						self.isLoading = false
						cont.resume()
					}
				},
				onError: { message in
					Task { @MainActor in
						self.isLoading = false
						self.errorMessage = message
						cont.resume()
					}
				}
			)
		}
	}
	
	public func logout() async {
		isLoading = true
		errorMessage = nil
		
			// TODO: call actual login use case through viewModel
			// For now, simulate a delay
		try? await Task.sleep(nanoseconds: 2_000_000_000)
		
		isLoading = false
			// Simulate error/success based on username
		if username.isEmpty || password.isEmpty {
			errorMessage = "Username and password required"
		} else {
//			viewModel.onLoginSuccess(userId: username)
		}
	}
}
