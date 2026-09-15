package di

import com.google.gson.Gson
import com.google.gson.GsonBuilder

object FrameworkModule {
    /**
     * Centralized pretty-printing Gson instance for reporters, file writers, and logging.
     */
    val prettyGson: Gson by lazy {
        GsonBuilder()
            .setPrettyPrinting()
            .disableHtmlEscaping()
            .create()
    }

    /**
     * Centralized compact Gson instance for fast serialization/parsing.
     */
    val gson: Gson by lazy {
        GsonBuilder().create()
    }
}
