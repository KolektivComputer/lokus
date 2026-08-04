package cat.mey.platform.core.extension.data.types

import cat.mey.platform.core.config.services.ConfigService
import cat.mey.platform.core.domain.data.types.Endpoint
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.koin.core.module.Module

/**
 * Base type for platform extensions.
 *
 * Subclasses can inject configs via:
 * ```
 * private val settings by configService.config<MyExtensionConfig>()
 * ```
 */
abstract class Extension : KoinComponent {
    abstract val id: ExtensionId
    abstract val name: String

    protected val configService: ConfigService by inject()

    /** Koin modules contributed by this extension (loaded with loadModules). */
    open fun modules(): List<Module> = emptyList()

    /** Domain-linkable endpoints. */
    open fun endpoints(): List<Endpoint> = emptyList()
}
