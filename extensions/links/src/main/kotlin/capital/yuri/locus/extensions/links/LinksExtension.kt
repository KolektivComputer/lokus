package capital.yuri.locus.extensions.links

import capital.yuri.locus.extensions.links.data.tables.LinkPageEntriesTable
import capital.yuri.locus.extensions.links.data.tables.LinkPagesTable
import capital.yuri.locus.extensions.links.data.tables.LinksTable
import capital.yuri.locus.platform.core.extension.data.types.Extension
import capital.yuri.locus.platform.core.extension.data.types.ExtensionCompanion
import capital.yuri.locus.platform.core.extension.data.types.ExtensionId
import org.jetbrains.exposed.v1.core.Table

class LinksExtension : Extension() {
    override val id: ExtensionId = ID
    override val name: String = "Links"

    override fun tables(): List<Table> = listOf(
        LinksTable,
        LinkPagesTable,
        LinkPageEntriesTable,
    )

    companion object : ExtensionCompanion {
        override val ID = ExtensionId("links")
        const val VERSION = "0.0.1"
    }
}
