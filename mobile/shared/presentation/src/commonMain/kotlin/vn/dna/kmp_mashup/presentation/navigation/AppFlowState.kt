package vn.dna.kmp_mashup.presentation.navigation

sealed interface AppFlowState {
    data object Splash : AppFlowState
    data object Unauthenticated : AppFlowState
    data class Authenticated(val userId: String) : AppFlowState
}
