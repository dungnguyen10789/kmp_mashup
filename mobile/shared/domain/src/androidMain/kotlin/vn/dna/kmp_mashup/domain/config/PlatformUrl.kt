package vn.dna.kmp_mashup.domain.config

actual fun normalizeBaseUrlForPlatform(baseUrl: String): String {
    // This regex now specifically targets just the hostname (localhost or 127.0.0.1)
    // and replaces it, leaving the rest of the URL (port and path) untouched.
    return baseUrl.replace(Regex("http://(localhost|127.0.0.1)"), "http://10.0.2.2")
}