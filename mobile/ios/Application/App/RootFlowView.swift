import MashupShared
import SwiftUI
import Presentation

struct RootFlowView: View {
    // Local state to drive UI routing
    @State private var currentAppState: AppState = AppStateBootstrapping.shared
    @State private var status: String = "Booting..."
    @State private var hasBootstrapped: Bool = false
    
    @State private var stateCancellable: AppStoreCancellable?
    @State private var effectCancellable: AppStoreCancellable?
    @State private var showAlert: Bool = false
    @State private var alertMessage: String = ""
    
    // DI Helpers for iOS - Init ONCE
    private let coreHelper = CoreDIHelper()
    private let authHelper = AuthDIHelper()
    private let userHelper = UserDIHelper()
    
    private let appStore: AppStore
    
    init() {
        // Retrieve scoped singleton from DI
        self.appStore = CoreDIHelper().getAppStore()
    }
    
    var body: some View {
        Group {
            if currentAppState is AppStateBootstrapping {
                StoryboardSplashView()
            } else if currentAppState is AppStateAuthenticated {
                MainFlowView(
                    status: status,
                    coreHelper: coreHelper,
                    authHelper: authHelper,
                    userHelper: userHelper
                )
				
            } else if currentAppState is AppStateUnauthenticated {
                AuthFlowView(status: status, authHelper: authHelper)
            } else {
                Text("Unknown State")
            }
        }
        .animation(.easeInOut(duration: 0.2), value: String(describing: currentAppState))
        .task {
            // Start observing auth state from the DI instance
            if stateCancellable == nil {
                stateCancellable = appStore.watchState(onEach: { state in
                    self.currentAppState = state
                    
                    // Update debug status text
                    if state is AppStateAuthenticated {
                        status = "Authenticated"
                    } else if state is AppStateUnauthenticated {
                        status = "Unauthenticated"
                    } else {
                        status = "Bootstrapping..."
                    }
                })
            }
            // Start observing one-shot effects
            if effectCancellable == nil {
                effectCancellable = appStore.watchEffects(onEach: { effect in
                    if let e = effect as? AppEffectShowMessage {
                        alertMessage = e.message
                        showAlert = true
                        status = e.message
                    }
                })
            }
            
            // Bootstrap once
            if !hasBootstrapped {
                hasBootstrapped = true
                do {
                    // Simulate Splash delay
                    try await Task.sleep(nanoseconds: 1_500_000_000)
                    
                    let bootstrapper = coreHelper.getBootstrapAppUseCase()
                    _ = try await bootstrapper.invoke()
                    
                } catch {
                    status = "Bootstrap failed"
                    alertMessage = "Bootstrap failed: \(error)"
                    showAlert = true
                }
            }
        }
        .alert("Notice", isPresented: $showAlert) {
            Button("OK", role: .cancel) {}
        } message: {
            Text(alertMessage)
        }
        .onDisappear {
            stateCancellable?.cancel()
            effectCancellable?.cancel()
            stateCancellable = nil
            effectCancellable = nil
        }
    }
}

// MARK: - Bootstrap View (Splash)
private struct BootstrapView: View {
    var body: some View {
        ZStack {
            Color.blue.opacity(0.1).ignoresSafeArea()
            VStack(spacing: 20) {
                Text("KMP Mashup")
                    .font(.largeTitle)
                    .fontWeight(.bold)
                ProgressView()
                    .scaleEffect(1.5)
                Text("Initializing...")
                    .font(.caption)
                    .foregroundColor(.secondary)
            }
        }
    }
}

// MARK: - Auth Flow
private struct AuthFlowView: View {
    let status: String
    @StateObject private var vm: LoginViewModelWrapper
    
    init(status: String, authHelper: AuthDIHelper) {
        self.status = status
        let kmpVm = authHelper.getLoginViewModel()
        _vm = StateObject(wrappedValue: LoginViewModelWrapper(viewModel: kmpVm))
    }
    
    var body: some View {
        VStack(spacing: 12) {
            Text("AuthFlow")
            Text("Status: \(status)")
            LoginView(viewModel: vm)
        }
        .padding()
    }
}

// MARK: - Main Flow
private struct MainFlowView: View {
    @State var status: String
    
    // Dependencies
    let logoutUseCase: LogoutUseCase
    let getProfileUseCase: GetUserProfileUseCase
    let notificationService: NotificationService
    
    init(status: String, coreHelper: CoreDIHelper, authHelper: AuthDIHelper, userHelper: UserDIHelper) {
        self._status = State(initialValue: status)
        // Init dependencies ONCE
        self.logoutUseCase = authHelper.getLogoutUseCase()
        self.getProfileUseCase = userHelper.getGetUserProfileUseCase()
        self.notificationService = coreHelper.getNotificationService()
    }
    
    var body: some View {
        VStack(spacing: 12) {
            Text("MainFlow")
            Text("Status: \(status)")
            
            Button(action: {
                Task {
                    status = "Loading Profile..."
                    do {
                        let user = try await getProfileUseCase.invoke()
                        status = "User: \(user.fullName) (\(user.email))"
                    } catch {
                        status = "Error: \(error.localizedDescription)"
                    }
                }
            }) {
                Text("Get My Profile")
                    .frame(maxWidth: .infinity)
                    .padding()
                    .background(Color.blue)
                    .foregroundColor(.white)
                    .cornerRadius(8)
            }
            
            Button(action: {
                Task {
                    _ = try? await logoutUseCase.invoke()
                }
            }) {
                Text("Logout")
                    .frame(maxWidth: .infinity)
                    .padding()
                    .background(Color.red.opacity(0.8))
                    .foregroundColor(.white)
                    .cornerRadius(8)
            }
            
            Button(action: {
                Task {
                    _ = try await notificationService.requestPermission()
                }
            }) {
                Text("Request noti permissions")
                    .frame(maxWidth: .infinity)
                    .padding()
                    .background(Color.orange.opacity(0.8))
                    .foregroundColor(.white)
                    .cornerRadius(8)
            }
            
            Button(action: {
                notificationService.scheduleNotification(id: "id", title: "Hello iOS", body: "Notification form KMP", delayMillis: 2000)
            }) {
                Text("Test Local Noti")
                    .frame(maxWidth: .infinity)
                    .padding()
                    .background(Color.green.opacity(0.8))
                    .foregroundColor(.white)
                    .cornerRadius(8)
            }

        }
        .padding()
        .task {
            // Auto fetch profile on appear
            status = "Loading Profile..."
            do {
                let user = try await getProfileUseCase.invoke()
                status = "User: \(user.fullName) (\(user.email))"
            } catch {
                status = "Error: \(error.localizedDescription)"
            }
        }
    }
}
