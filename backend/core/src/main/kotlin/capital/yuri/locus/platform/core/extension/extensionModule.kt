package capital.yuri.locus.platform.core.extension

import capital.yuri.locus.platform.core.extension.services.ExtensionLoaderService
import org.koin.core.qualifier.named
import org.koin.dsl.module
import org.koin.plugin.module.dsl.single

val extensionModule = module {
    single<ExtensionLoaderService>()

    scope(named("extension")) {
    }
}
