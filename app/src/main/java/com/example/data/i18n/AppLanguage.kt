package com.example.data.i18n

enum class AppLanguage(
    val code: String,
    val displayName: String,
    val nativeName: String,
    val flag: String,
    val isRtl: Boolean = false,
    val isSupported: Boolean = true
) {
    PORTUGUESE(
        code = "pt",
        displayName = "Português",
        nativeName = "Português",
        flag = "🇵🇹",
        isRtl = false,
        isSupported = true
    ),
    ENGLISH(
        code = "en",
        displayName = "Inglês",
        nativeName = "English",
        flag = "🇬🇧",
        isRtl = false,
        isSupported = true
    ),
    FRENCH(
        code = "fr",
        displayName = "Francês",
        nativeName = "Français",
        flag = "🇫🇷",
        isRtl = false,
        isSupported = true
    ),
    SPANISH(
        code = "es",
        displayName = "Espanhol",
        nativeName = "Español",
        flag = "🇪🇸",
        isRtl = false,
        isSupported = true
    ),
    // Ready for future activation
    GERMAN(
        code = "de",
        displayName = "Alemão",
        nativeName = "Deutsch",
        flag = "🇩🇪",
        isRtl = false,
        isSupported = false
    ),
    ITALIAN(
        code = "it",
        displayName = "Italiano",
        nativeName = "Italiano",
        flag = "🇮🇹",
        isRtl = false,
        isSupported = false
    ),
    DUTCH(
        code = "nl",
        displayName = "Holandês",
        nativeName = "Nederlands",
        flag = "🇳🇱",
        isRtl = false,
        isSupported = false
    ),
    ARABIC(
        code = "ar",
        displayName = "Árabe",
        nativeName = "العربية",
        flag = "🇸🇦",
        isRtl = true,
        isSupported = false
    ),
    PORTUGUESE_BR(
        code = "pt-BR",
        displayName = "Português (Brasil)",
        nativeName = "Português (Brasil)",
        flag = "🇧🇷",
        isRtl = false,
        isSupported = false
    ),
    ENGLISH_US(
        code = "en-US",
        displayName = "Inglês (Estados Unidos)",
        nativeName = "English (United States)",
        flag = "🇺🇸",
        isRtl = false,
        isSupported = false
    ),
    CHINESE(
        code = "zh",
        displayName = "Chinês",
        nativeName = "中文",
        flag = "🇨🇳",
        isRtl = false,
        isSupported = false
    ),
    JAPANESE(
        code = "ja",
        displayName = "Japonês",
        nativeName = "日本語",
        flag = "🇯🇵",
        isRtl = false,
        isSupported = false
    );

    companion object {
        val DEFAULT = ENGLISH // International standard default if device language unsupported

        fun fromCode(code: String?): AppLanguage {
            if (code == null) return DEFAULT
            val clean = code.lowercase().trim()
            return values().firstOrNull {
                it.code.equals(clean, ignoreCase = true) ||
                clean.startsWith(it.code.lowercase())
            } ?: DEFAULT
        }

        fun detectDeviceLanguage(): AppLanguage {
            val deviceLang = java.util.Locale.getDefault().language.lowercase()
            return when {
                deviceLang.startsWith("pt") -> PORTUGUESE
                deviceLang.startsWith("fr") -> FRENCH
                deviceLang.startsWith("es") -> SPANISH
                deviceLang.startsWith("en") -> ENGLISH
                else -> ENGLISH // Fallback to English as requested
            }
        }

        fun getActiveLanguages(): List<AppLanguage> {
            return values().filter { it.isSupported }
        }

        fun getUpcomingLanguages(): List<AppLanguage> {
            return values().filter { !it.isSupported }
        }
    }
}
