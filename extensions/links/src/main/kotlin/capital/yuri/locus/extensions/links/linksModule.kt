package capital.yuri.locus.extensions.links

import capital.yuri.locus.extensions.links.services.LinkService
import org.koin.dsl.module
import org.koin.plugin.module.dsl.single

val linksModule = module {
    single<LinkService>()
}
