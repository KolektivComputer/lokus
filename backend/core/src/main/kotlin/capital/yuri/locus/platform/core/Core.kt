package capital.yuri.locus.platform.core

/**
 * Marker for the platform core library module.
 *
 * Existing core types still live under `:backend:api` during the migration;
 * they move here incrementally (config, db, auth, scheduling, stats, domain).
 */
object Core {
    const val MODULE = "backend:core"
}
