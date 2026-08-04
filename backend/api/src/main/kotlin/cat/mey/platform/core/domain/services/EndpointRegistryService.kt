package cat.mey.platform.core.domain.services

import cat.mey.platform.core.domain.data.types.Endpoint
import cat.mey.platform.core.domain.data.types.EndpointId
import org.koin.core.component.KoinComponent

class EndpointRegistryService : KoinComponent {
    private val endpoints = mutableMapOf<EndpointId, Endpoint>()

    fun defineEndpoint(endpoint: Endpoint) {
        endpoints[endpoint.id] = endpoint
    }

    operator fun get(endpointId: EndpointId): Endpoint? = endpoints[endpointId]
}