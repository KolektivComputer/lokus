package cat.mey.platform.core.domain.data.types

import kotlinx.serialization.Serializable

@Serializable
@JvmInline
value class EndpointId(val endpointId: String) {
    companion object {
        fun plugin(pluginName: String, endpointName: String) =
            EndpointId("plugin.$pluginName.$endpointName")
    }
}