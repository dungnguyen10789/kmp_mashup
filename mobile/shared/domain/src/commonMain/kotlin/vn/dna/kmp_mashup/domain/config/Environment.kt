package vn.dna.kmp_mashup.domain.config

enum class Environment {
    DEV,
    STAGING,
    PROD;

    companion object {
        fun fromName(name: String?): Environment {
            return when (name?.trim()?.uppercase()) {
                "DEV" -> DEV
                "STAGING" -> STAGING
                "PROD" -> PROD
                else -> PROD
            }
        }
    }
}
