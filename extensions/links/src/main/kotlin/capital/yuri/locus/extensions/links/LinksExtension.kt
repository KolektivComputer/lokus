package capital.yuri.locus.extensions.links

/**
 * Links extension (in-dev, same repo).
 *
 * Tables currently still live under `platform.links` in `:backend:api` and are
 * registered via [capital.yuri.locus.platform.core.db.registerCoreTables].
 * When this module is JAR-loaded, call:
 *
 * ```
 * tableRegistry.register(LinksTable, LinkPagesTable, LinkPageEntriesTable)
 * ```
 */
object LinksExtension {
    const val ID = "links"
    const val VERSION = "0.0.1"
}
