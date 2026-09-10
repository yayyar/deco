package com.yayyar.deco.core.data.repository

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

    fun setDarkMode(enabled: Boolean?)
    fun setSelectedLanguage(lang: String)
    fun setSelectedPrinter(address: String?, name: String?)
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

    companion object {
        private const val KEY_DARK_MODE = "pref_dark_mode"
        private const val KEY_LANGUAGE = "pref_language"
        private const val KEY_PRINTER_ADDRESS = "pref_printer_address"
        private const val KEY_PRINTER_NAME = "pref_printer_name"
    }
}
