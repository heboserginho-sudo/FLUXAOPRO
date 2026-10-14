package com.example.data.i18n

data class InternationalAddress(
    val countryCode: String = "AO",
    val countryName: String = "Angola",
    val stateProvince: String = "",
    val city: String = "",
    val districtNeighborhood: String = "",
    val postalCode: String = "",
    val street: String = "",
    val streetNumber: String = "",
    val complement: String = "",
    val latitude: Double? = null,
    val longitude: Double? = null
) {
    fun formatSingleLine(): String {
        val parts = mutableListOf<String>()
        if (street.isNotBlank()) {
            val num = if (streetNumber.isNotBlank()) " $streetNumber" else ""
            val comp = if (complement.isNotBlank()) " ($complement)" else ""
            parts.add("$street$num$comp")
        }
        if (districtNeighborhood.isNotBlank()) parts.add(districtNeighborhood)
        if (city.isNotBlank()) parts.add(city)
        if (stateProvince.isNotBlank()) parts.add(stateProvince)
        if (postalCode.isNotBlank()) parts.add(postalCode)
        if (countryName.isNotBlank()) parts.add(countryName)
        return parts.joinToString(", ")
    }
}
