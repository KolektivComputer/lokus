package cat.mey.platform.core.domain.data.types

import io.ktor.server.routing.Route

interface Endpoint {
    val id: EndpointId

    fun Route.buildRoutes()
}