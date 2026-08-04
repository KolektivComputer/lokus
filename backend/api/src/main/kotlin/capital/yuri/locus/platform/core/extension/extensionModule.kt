package capital.yuri.locus.platform.core.extension

import org.koin.core.qualifier.named
import org.koin.dsl.module

val extensionModule = module {
    scope(named("extension")) {
    }
}
