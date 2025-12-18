import SwiftUI
import MashupShared

public struct LoginView: View {
	@StateObject private var vm: LoginViewModelWrapper
	
	public init(viewModel: LoginViewModelWrapper) {
		_vm = StateObject(wrappedValue: viewModel)
	}
	
	public var body: some View {
		VStack(spacing: 20) {
			TextField("Username", text: $vm.username)
				.textFieldStyle(.roundedBorder)
			
			SecureField("Password", text: $vm.password)
				.textFieldStyle(.roundedBorder)
			
			Button {
				Task { await vm.login() }
			} label: {
				if vm.isLoading {
					HStack(spacing: 10) {
						Text("Login")
						ProgressView()
							.progressViewStyle(.circular)
							.tint(.white)
					}
				} else {
					Text("Login")
				}
			}
			.buttonStyle(.borderedProminent)
			
			if let err = vm.errorMessage {
				Text(err).foregroundColor(.red)
			}
		}
		.frame(maxWidth: .infinity, maxHeight: .infinity)
		.padding()
		.background(.yellow)
	}
}
