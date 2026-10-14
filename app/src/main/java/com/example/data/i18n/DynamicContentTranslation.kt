package com.example.data.i18n

data class MultilingualProduct(
    val id: String,
    val defaultName: String,
    val defaultDescription: String,
    val price: Double,
    val translations: Map<String, ProductTranslationItem> = emptyMap()
) {
    fun getLocalizedName(language: AppLanguage): String {
        return translations[language.code]?.name
            ?: translations[AppLanguage.ENGLISH.code]?.name
            ?: translations[AppLanguage.PORTUGUESE.code]?.name
            ?: defaultName
    }

    fun getLocalizedDescription(language: AppLanguage): String {
        return translations[language.code]?.description
            ?: translations[AppLanguage.ENGLISH.code]?.description
            ?: translations[AppLanguage.PORTUGUESE.code]?.description
            ?: defaultDescription
    }
}

data class ProductTranslationItem(
    val name: String,
    val description: String
)

data class MultilingualCategory(
    val id: String,
    val defaultName: String,
    val translations: Map<String, String> = emptyMap()
) {
    fun getLocalizedName(language: AppLanguage): String {
        return translations[language.code]
            ?: translations[AppLanguage.ENGLISH.code]
            ?: translations[AppLanguage.PORTUGUESE.code]
            ?: defaultName
    }
}
