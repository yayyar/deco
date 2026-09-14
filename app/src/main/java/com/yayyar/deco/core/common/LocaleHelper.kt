package com.yayyar.deco.core.common

import android.content.Context
import android.content.ContextWrapper
import android.content.res.AssetManager
import android.content.res.Configuration
import android.content.res.Resources
import android.os.Build
import android.os.LocaleList
import java.util.Locale

class LocalizedContext(
    base: Context,
    private val configContext: Context
) : ContextWrapper(base) {
    override fun getResources(): Resources = configContext.resources
    override fun getAssets(): AssetManager = configContext.assets
}

object LocaleHelper {

    fun getLocale(languageCode: String): Locale {
        return when (languageCode.lowercase()) {
            "my" -> Locale("my", "MM")
            else -> Locale("en", "US")
        }
    }

    fun applyLocale(context: Context, languageCode: String): Context {
        val locale = getLocale(languageCode)
        Locale.setDefault(locale)

        val configuration = Configuration(context.resources.configuration)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            configuration.setLocales(LocaleList(locale))
        } else {
            @Suppress("DEPRECATION")
            configuration.locale = locale
        }

        val configContext = context.createConfigurationContext(configuration)
        return LocalizedContext(context, configContext)
    }

    fun getLocalizedConfiguration(baseConfiguration: Configuration, languageCode: String): Configuration {
        val locale = getLocale(languageCode)
        val configuration = Configuration(baseConfiguration)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            configuration.setLocales(LocaleList(locale))
        } else {
            @Suppress("DEPRECATION")
            configuration.locale = locale
        }
        return configuration
    }
}
