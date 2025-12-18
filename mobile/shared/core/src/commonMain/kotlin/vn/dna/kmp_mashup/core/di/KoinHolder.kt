package vn.dna.kmp_mashup.core.di

import org.koin.core.Koin

/**
 * A singleton holder for the active [Koin] instance.
 *
 * This object is particularly useful for iOS/Swift interop.
 * Since Swift cannot easily access Koin's global context via inline functions (like `inject()`),
 * we expose the raw Koin instance here so that Swift helpers or manual injection logic
 * can retrieve dependencies.
 */
object KoinHolder {
    /**
     * The initialized Koin instance.
     * Guaranteed to be set after [initKoin] is called.
     */
    lateinit var koin: Koin
}
