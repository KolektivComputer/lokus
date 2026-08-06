package capital.yuri.locus.platform.core.extension.data.types

import capital.yuri.locus.platform.core.config.services.ConfigService
import capital.yuri.locus.platform.core.db.services.TableRegistryService
import capital.yuri.locus.platform.core.domain.data.types.Endpoint
import capital.yuri.locus.platform.core.domain.services.EndpointRegistryService
import org.jetbrains.exposed.v1.core.Table
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

/**
 * Live extension instance after SPI discovery and Koin module load.
 *
 * Subclasses can inject configs via:
 * ```
 * private val settings by configService.config<MyExtensionConfig>()
 * ```
 */
abstract class Extension : KoinComponent {
    abstract val id: ExtensionId
    abstract val name: String

    protected val configService by inject<ConfigService>()

    /** Domain-linkable endpoints contributed by this extension. */
    open fun endpoints(): List<Endpoint> = emptyList()

    /** Exposed tables owned by this extension (registered for migration). */
    open fun tables(): List<Table> = emptyList()

    /**
     * Called once after construction when the extension is installed.
     * Default wires [tables] and [endpoints] into the core registries.
     */
    open fun install(
        tableRegistry: TableRegistryService,
        endpointRegistry: EndpointRegistryService,
    ) {
        val ownedTables = tables()
        if (ownedTables.isNotEmpty()) {
            tableRegistry.register(ownedTables)
        }
        endpoints().forEach { endpointRegistry.defineEndpoint(it) }
    }
}
