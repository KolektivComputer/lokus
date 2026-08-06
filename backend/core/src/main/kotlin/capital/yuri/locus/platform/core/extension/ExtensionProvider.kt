package capital.yuri.locus.platform.core.extension

import capital.yuri.locus.platform.core.extension.data.types.Extension
import capital.yuri.locus.platform.core.extension.data.types.ExtensionId
import org.koin.core.module.Module

/**
 * SPI entry for a loadable extension JAR (or classpath module).
 *
 * Implementations must be concrete, public, and have a no-arg constructor so
 * [java.util.ServiceLoader] can instantiate them. Do **not** implement Koin
 * injection here — that belongs on [Extension] after modules are loaded.
 *
 * Register via:
 * `META-INF/services/capital.yuri.locus.platform.core.extension.ExtensionProvider`
 */
interface ExtensionProvider {
    val id: ExtensionId
    val name: String
    val version: String

    /** Koin modules contributed before [create] runs. */
    fun modules(): List<Module> = emptyList()

    /** Build the live extension once its modules (and core) are on the graph. */
    fun create(): Extension
}
