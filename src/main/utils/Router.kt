package utils

import io.ktor.client.request.HttpRequestBuilder
import models.ApiRequest

object Router {
    data class RouteInfo(
        val method: String = "",
        val url: String = "",
        val path: String = "",
        val queryParams: String = "",
        val scheme: String = "https",
        val authority: String = "",
        val extraArgs: Map<String, Any> = emptyMap()
    ) {
        companion object {
            /**
             * Intelligently parses vararg arguments into a strongly-typed RouteInfo instance.
             */
            fun from(vararg args: Any?): RouteInfo {
                var method = "GET"
                var url = ""
                var path = ""
                var queryParams = ""
                var scheme = "https"
                var authority = ""
                val extras = mutableMapOf<String, Any>()

                for (arg in args) {
                    when (arg) {
                        is HttpRequestBuilder -> {
                            val apiReq = ApiRequest.from(arg)
                            method = apiReq.method
                            url = apiReq.url
                            path = apiReq.path
                            queryParams = apiReq.queryParams
                            scheme = apiReq.scheme
                            authority = apiReq.authority
                        }
                        is ApiRequest -> {
                            method = arg.method
                            url = arg.url
                            path = arg.path
                            queryParams = arg.queryParams
                            scheme = arg.scheme
                            authority = arg.authority
                        }
                        is Pair<*, *> -> {
                            if (arg.first != null && arg.second != null) {
                                extras[arg.first.toString()] = arg.second!!
                            }
                        }
                        is Map<*, *> -> {
                            arg.forEach { (k, v) ->
                                if (k != null && v != null) extras[k.toString()] = v
                            }
                        }
                        is String -> {
                            if (url.isEmpty()) url = arg else path = arg
                        }
                    }
                }

                return RouteInfo(
                    method = method,
                    url = url,
                    path = path,
                    queryParams = queryParams,
                    scheme = scheme,
                    authority = authority,
                    extraArgs = extras
                )
            }
        }
    }

    fun extract(vararg args: Any?): RouteInfo = RouteInfo.from(*args)
}
