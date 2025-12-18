package vn.dna.kmp_mashup.android

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.safeContentPadding
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import org.jetbrains.compose.ui.tooling.preview.Preview
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import vn.dna.kmp_mashup.domain.config.AppConfig
import vn.dna.kmp_mashup.domain.model.auth.LoginCredentials
import vn.dna.kmp_mashup.domain.model.error.Failure
import vn.dna.kmp_mashup.domain.usecase.auth.LoginUseCase
import vn.dna.kmp_mashup.domain.usecase.auth.LogoutUseCase
import vn.dna.kmp_mashup.presentation.platform.toFailure

private class AuthDeps : KoinComponent {
    val appConfig: AppConfig by inject()
    val loginUseCase: LoginUseCase by inject()
    val logoutUseCase: LogoutUseCase by inject()
}

@Composable
@Preview
fun App() {
    MaterialTheme {
        val scope = rememberCoroutineScope()
        val deps = remember { AuthDeps() }
        val shared = deps.appConfig

        var username by remember { mutableStateOf("conandk1") }
        var password by remember { mutableStateOf("Conandk1@") }
        var status by remember { mutableStateOf("Idle") }

        Column(
            modifier = Modifier
                .background(MaterialTheme.colorScheme.primaryContainer)
                .safeContentPadding()
                .fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            SelectionContainer {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text("Android ENV: ${NativeConfig.envName}")
                    Text("Shared baseUrl: ${shared.baseUrl}")
                    Text("Status: $status")
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                modifier = Modifier.fillMaxWidth(),
                value = username,
                onValueChange = { username = it },
                label = { Text("Username") },
            )

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                modifier = Modifier.fillMaxWidth(),
                value = password,
                onValueChange = { password = it },
                label = { Text("Password") },
            )

            Spacer(modifier = Modifier.height(12.dp))

            Button(
                onClick = {
                    scope.launch {
                        status = "Logging in..."
                        try {
                            // invoke now returns R directly, or throws exception on failure
                            deps.loginUseCase.invoke(
                                LoginCredentials(username = username, password = password)
                            )
                            // Success path
                            println("LOGIN_SUCCESS")
                            status = "Login success"
                        } catch (t: Throwable) {
                            // Failure path
                            val failure = t.toFailure()
                            when (failure) {
                                is Failure.DataMappingError -> {
                                    println("LOGIN_FAILED: DataMappingError: ${failure.exception.message}")
                                }

                                is Failure.ApiError -> {
                                    println("LOGIN_FAILED: ApiError(${failure.code}): ${failure.errorBody}")
                                }
                                
                                is Failure.UnknownError -> {
                                    // Print the internal exception message for debugging
                                    val cause = failure.exception?.message ?: "No cause"
                                    println("LOGIN_FAILED: UnknownError: ${failure.message} Cause: $cause")
                                }

                                else -> {
                                    println("LOGIN_FAILED: ${failure.message}")
                                }
                            }
                            status = "Login failed: ${failure.message}"
                        }
                    }
                }
            ) {
                Text("Login")
            }

            Spacer(modifier = Modifier.height(12.dp))

            Button(
                onClick = {
                    scope.launch {
                        // Use the LogoutUseCase from Koin to ensure all logic (API call, clearing tokens) is handled correctly.
                        status = "Logging out..."
                        try {
                            deps.logoutUseCase.invoke(params = Unit)
                            println("LOGOUT_SUCCESS")
                            status = "Logout success"
                        } catch (t: Throwable) {
                            println("LOGOUT_FAILED: ${t.message}")
                            status = "Logout failed"
                        }
                    }
                }
            ) {
                Text("Logout")
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                "Note: Android emulator -> host machine: use http://10.0.2.2:<port> (HTTP requires cleartext enabled).",
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}
