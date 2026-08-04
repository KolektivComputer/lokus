@file:OptIn(ExperimentalUuidApi::class)

package capital.yuri.locus.platform.core.domain.ktor

import capital.yuri.locus.platform.core.auth.jobs.DeactivateSessionsJob.Companion.getKoin
import capital.yuri.locus.platform.core.domain.data.entities.Domain
import capital.yuri.locus.platform.core.domain.services.repositories.DomainRepository
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.ApplicationCall
import io.ktor.server.application.createRouteScopedPlugin
import io.ktor.server.request.host
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.util.AttributeKey
import org.koin.core.qualifier.named
import org.koin.core.scope.Scope
import org.koin.ktor.ext.getKoin
import kotlin.uuid.ExperimentalUuidApi

val DomainKey = AttributeKey<Domain>("Domain")
val DomainScopeKey = AttributeKey<Scope>("DomainScope")

val ApplicationCall.domain: Domain
    get() = attributes[DomainKey]

val ApplicationCall.domainScope: Scope
    get() = attributes[DomainScopeKey]

// Nice call-site injection
inline fun <reified T : Any> ApplicationCall.getDomainScoped(): T = domainScope.get()

class DomainRouteConfig {
    /**
     * How to resolve the current Domain for this route tree.
     * Defaults to Host-header lookup.
     */
    var resolve: suspend ApplicationCall.() -> Domain? = { defaultDomainResolve() }
}

val DomainRoutePlugin = createRouteScopedPlugin(
    name = "DomainRoute",
    createConfiguration = ::DomainRouteConfig,
) {
    val resolve = pluginConfig.resolve

    onCall { call ->
        val domain = call.resolve()
            ?: return@onCall call.respond(HttpStatusCode.NotFound, "Unknown domain")

        call.attributes.put(DomainKey, domain)

        val scope = getKoin().getOrCreateScope(
            scopeId = "domain-${domain.id}",
            qualifier = named("domain"),
        )

        // make domain available for injection inside the scope
        scope.declare(domain, allowOverride = true)

        call.attributes.put(DomainScopeKey, scope)
    }
}

suspend fun ApplicationCall.defaultDomainResolve(): Domain? {
    val hostHeader = request.host().lowercase()
    val fqdn = hostHeader.substringBefore(":") // trim port onwards from the end

    return getKoin().get<DomainRepository>().findByFqdnSuspend(fqdn)
}

fun Route.domainRoute(
    resolve: suspend ApplicationCall.() -> Domain? = { defaultDomainResolve() },
    block: Route.() -> Unit,
) {
    install(DomainRoutePlugin) {
        this.resolve = resolve
    }
    block()
}
