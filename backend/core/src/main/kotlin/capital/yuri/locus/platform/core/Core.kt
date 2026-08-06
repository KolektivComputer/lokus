package capital.yuri.locus.platform.core

/**
 * Marker for the platform core library module (`:backend:core`).
 *
 * Owns config, db, auth, scheduling, stats, domain, version, and extension
 * contracts. The API daemon (`:backend:api`) and CLI (`:backend:cli`) depend
 * on this library; feature code such as links still lives under api until
 * moved into `:extensions:*`.
 */
object Core {
    const val MODULE = "backend:core"
}
