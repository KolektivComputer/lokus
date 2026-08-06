package capital.yuri.locus.extensions.links

import capital.yuri.locus.platform.core.extension.ExtensionProvider
import capital.yuri.locus.platform.core.extension.data.types.Extension
import capital.yuri.locus.platform.core.extension.data.types.ExtensionId
import org.koin.core.module.Module

/** SPI entry for the links extension (classpath or dropped JAR). */
class LinksExtensionProvider : ExtensionProvider {
    override val id: ExtensionId = LinksExtension.ID
    override val name: String = "Links"
    override val version: String = LinksExtension.VERSION

    override fun modules(): List<Module> = listOf(linksModule)

    override fun create(): Extension = LinksExtension()
}
