package com.yayyar.teahouse.core.data.repository

import android.content.Context
import android.content.SharedPreferences
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

interface PreferencesRepository {
    val isDarkMode: StateFlow<Boolean?>
    val selectedLanguage: StateFlow<String>
    val selectedPrinterAddress: StateFlow<String?>
    val selectedPrinterName: StateFlow<String?>
    val isGridView: StateFlow<Boolean>
    val selectedThemeSeed: StateFlow<String>
    val selectedThemeStyle: StateFlow<String>

    fun setDarkMode(enabled: Boolean?)
    fun setSelectedLanguage(lang: String)
    fun setSelectedPrinter(address: String?, name: String?)
    fun setGridView(isGrid: Boolean)
    fun setSelectedTheme(seed: String, style: String)
}

@Singleton
class PreferencesRepositoryImpl @Inject constructor(
    @param:ApplicationContext private val context: Context
) : PreferencesRepository {

    private val prefs: SharedPreferences =
        context.getSharedPreferences("deco_preferences", Context.MODE_PRIVATE)

    private val _isDarkMode = MutableStateFlow<Boolean?>(readDarkMode())
    override val isDarkMode: StateFlow<Boolean?> = _isDarkMode.asStateFlow()

    private val _selectedLanguage = MutableStateFlow(readLanguage())
    override val selectedLanguage: StateFlow<String> = _selectedLanguage.asStateFlow()

    private val _selectedPrinterAddress = MutableStateFlow(readPrinterAddress())
    override val selectedPrinterAddress: StateFlow<String?> = _selectedPrinterAddress.asStateFlow()

    private val _selectedPrinterName = MutableStateFlow(readPrinterName())
    override val selectedPrinterName: StateFlow<String?> = _selectedPrinterName.asStateFlow()

    private val _isGridView = MutableStateFlow(readGridView())
    override val isGridView: StateFlow<Boolean> = _isGridView.asStateFlow()

    private val _selectedThemeSeed = MutableStateFlow(readThemeSeed())
    override val selectedThemeSeed: StateFlow<String> = _selectedThemeSeed.asStateFlow()

    private val _selectedThemeStyle = MutableStateFlow(readThemeStyle())
    override val selectedThemeStyle: StateFlow<String> = _selectedThemeStyle.asStateFlow()

    private fun readDarkMode(): Boolean? {
        if (!prefs.contains(KEY_DARK_MODE)) return null // Follow system
        return prefs.getBoolean(KEY_DARK_MODE, false)
    }

    private fun readLanguage(): String {
        return prefs.getString(KEY_LANGUAGE, "en") ?: "en"
    }

    private fun readPrinterAddress(): String? {
        return prefs.getString(KEY_PRINTER_ADDRESS, null)
    }

    private fun readPrinterName(): String? {
        return prefs.getString(KEY_PRINTER_NAME, null)
    }

    private fun readGridView(): Boolean {
        return prefs.getBoolean(KEY_ITEM_LAYOUT_GRID, true)
    }

    private fun readThemeSeed(): String {
        return prefs.getString(KEY_THEME_SEED, "TEAL") ?: "TEAL"
    }

    private fun readThemeStyle(): String {
        return prefs.getString(KEY_THEME_STYLE, "TONAL_SPOT") ?: "TONAL_SPOT"
    }

    override fun setDarkMode(enabled: Boolean?) {
        prefs.edit().apply {
            if (enabled == null) {
                remove(KEY_DARK_MODE)
            } else {
                putBoolean(KEY_DARK_MODE, enabled)
            }
            apply()
        }
        _isDarkMode.value = enabled
    }

    override fun setSelectedLanguage(lang: String) {
        prefs.edit().putString(KEY_LANGUAGE, lang).apply()
        _selectedLanguage.value = lang
    }

    override fun setSelectedPrinter(address: String?, name: String?) {
        prefs.edit().apply {
            if (address == null) remove(KEY_PRINTER_ADDRESS) else putString(KEY_PRINTER_ADDRESS, address)
            if (name == null) remove(KEY_PRINTER_NAME) else putString(KEY_PRINTER_NAME, name)
            apply()
        }
        _selectedPrinterAddress.value = address
        _selectedPrinterName.value = name
    }

    override fun setGridView(isGrid: Boolean) {
        prefs.edit().putBoolean(KEY_ITEM_LAYOUT_GRID, isGrid).apply()
        _isGridView.value = isGrid
    }

    override fun setSelectedTheme(seed: String, style: String) {
        prefs.edit().apply {
            putString(KEY_THEME_SEED, seed)
            putString(KEY_THEME_STYLE, style)
            apply()
        }
        _selectedThemeSeed.value = seed
        _selectedThemeStyle.value = style
    }

    companion object {
        private const val KEY_DARK_MODE = "pref_dark_mode"
        private const val KEY_LANGUAGE = "pref_language"
        private const val KEY_PRINTER_ADDRESS = "pref_printer_address"
        private const val KEY_PRINTER_NAME = "pref_printer_name"
        private const val KEY_ITEM_LAYOUT_GRID = "pref_item_layout_grid"
        private const val KEY_THEME_SEED = "pref_theme_seed"
        private const val KEY_THEME_STYLE = "pref_theme_style"
    }
}
