package vn.dna.kmp_mashup.presentation.navigation

sealed interface NavEffect {
    data object NavigateToLogin : NavEffect
    data class NavigateToHome(val userId: String) : NavEffect
    data object Back : NavEffect
    data class OpenUrl(val url: String) : NavEffect
}
