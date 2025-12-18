package vn.dna.kmp_mashup.android

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.safeContentPadding
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import vn.dna.kmp_mashup.domain.usecase.auth.BootstrapAppUseCase
import vn.dna.kmp_mashup.domain.usecase.auth.LogoutUseCase
import vn.dna.kmp_mashup.domain.usecase.user.GetMyProfileUseCase
import vn.dna.kmp_mashup.domain.usecase.user.GetUserProfileUseCase
import vn.dna.kmp_mashup.presentation.auth.AppEffect
import vn.dna.kmp_mashup.presentation.auth.AppStore
import vn.dna.kmp_mashup.presentation.auth.AppState

// Koin component to inject dependencies correctly
private class RootDeps : KoinComponent {
    val bootstrapAppUseCase: BootstrapAppUseCase by inject()
    val logoutUseCase: LogoutUseCase by inject()
    val getMyProfileUseCase: GetMyProfileUseCase by inject()
    val getUserProfileUseCase: GetUserProfileUseCase by inject()
    val appStore: AppStore by inject()
}

@Composable
fun RootFlows() {
    val deps = remember { RootDeps() }
    val scope = rememberCoroutineScope()

    // Subscribe to global AppState from DI
    val appState by deps.appStore.appState.collectAsState()
    
    var statusMessage by remember { mutableStateOf("Ready") }

    // One-shot effects (toast/alert)
    LaunchedEffect(Unit) {
        deps.appStore.effects.collect { e ->
            when (e) {
                is AppEffect.ShowMessage -> statusMessage = e.message
            }
        }
    }

    // Reset status message when app state changes (e.g. logout)
    LaunchedEffect(appState) {
        if (appState is AppState.Unauthenticated) {
            statusMessage = "Ready"
        }
    }

    // Trigger bootstrap ONLY ONCE when app starts.
    // The AppStore initializes with Bootstrapping state.
    // We can simulate a minimum splash time here if desired, or just let bootstrapper finish.
    LaunchedEffect(Unit) {
        scope.launchCatching(
             block = { 
                 deps.bootstrapAppUseCase.invoke()
             },
             onSuccess = { result ->
                 statusMessage = "Boot result: $result"
             },
             onFailure = {
                 statusMessage = "Boot failed: ${it.message}"
                 // Fallback to Unauthenticated if boot fails severely
                 deps.appStore.setUnauthenticated(showMessage = "Boot failed: ${it.message}")
             }
        )
    }

    MaterialTheme {
        when (appState) {
            is AppState.Bootstrapping -> {
                BootstrapFlow(status = statusMessage)
            }
            is AppState.Unauthenticated -> {
                AuthFlow(status = statusMessage)
            }
            is AppState.Authenticated -> {
                MainFlow(
                    status = statusMessage,
                    onLogout = {
                        scope.launchCatching(
                            block = { deps.logoutUseCase.invoke(params = Unit) },
                            onFailure = { /* Ignore logout errors */ }
                        )
                    },
                    onGetMyProfile = {
                        scope.launchCatching(
                            onLoading = { statusMessage = "Loading Profile..." },
                            block = { deps.getMyProfileUseCase .invoke(params = Unit) },
                            onSuccess = { user ->
                                statusMessage = "User: ${user.fullName} (${user.email})"
                            },
                            onFailure = { e ->
                                statusMessage = "Error: ${e.message}"
                            }
                        )
                    },
                    onGetUserProfile = {
                        scope.launchCatching(
                            onLoading = { statusMessage = "Loading Profile..." },
                            block = { deps.getUserProfileUseCase.invoke(params = "853ddfef-6479-421b-8946-905ec4ed22df") },
                            onSuccess = { user ->
                            },
                            onFailure = { e ->
                            }
                        )
                    }
                )
            }
        }
    }
}

@Composable
fun BootstrapFlow(status: String) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.primaryContainer),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "KMP Mashup", 
                style = MaterialTheme.typography.headlineLarge,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
            Spacer(Modifier.height(16.dp))
            CircularProgressIndicator(
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = "Initializing...",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
            Spacer(Modifier.height(8.dp))
            // Show detailed status for debugging
            Text(
                text = status,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.error
            )
        }
    }
}

@Composable
fun AuthFlow(status: String) {
    Column(Modifier.safeContentPadding()) {
        Text("AuthFlow")
        Spacer(Modifier.height(8.dp))
        Text("Status: $status")
        Spacer(Modifier.height(12.dp))
        // Reuse existing sample UI (login/logout buttons etc.)
        App()
    }
}

@Composable
fun MainFlow(
    status: String, 
    onLogout: () -> Unit,
    onGetMyProfile: () -> Unit,
    onGetUserProfile: () -> Unit
) {
    // Automatically fetch profile when entering MainFlow
    LaunchedEffect(Unit) {
        onGetMyProfile()
        onGetUserProfile()
    }

    Column(Modifier.safeContentPadding()) {
        Text("MainFlow")
        Spacer(Modifier.height(8.dp))
        Text("Status: $status")
        Spacer(Modifier.height(12.dp))
        
        Button(modifier = Modifier.fillMaxWidth(), onClick = onGetMyProfile) {
            Text("Get My Profile (Retry)")
        }
        
        Spacer(Modifier.height(8.dp))
        
        Button(modifier = Modifier.fillMaxWidth(), onClick = onLogout) {
            Text("Logout")
        }
    }
}
