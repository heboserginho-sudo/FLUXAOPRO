package com.example.data.model

import com.example.data.local.entity.CompanyBrandingEntity
import com.example.data.local.entity.CompanyEntity
import com.example.data.local.entity.CompanySettingsEntity
import com.example.data.local.entity.ProfileEntity

data class CompanyContext(
    val currentCompany: CompanyEntity? = null,
    val companyId: String = "",
    val branding: CompanyBrandingEntity? = null,
    val settings: CompanySettingsEntity? = null,
    val userProfile: ProfileEntity? = null,
    val userRole: String = "company_admin" // super_admin, company_admin, employee, customer
) {
    val isSuperAdmin: Boolean
        get() = userRole.equals("super_admin", ignoreCase = true)

    val isCompanyAdmin: Boolean
        get() = userRole.equals("company_admin", ignoreCase = true) || isSuperAdmin

    val isEmployee: Boolean
        get() = userRole.equals("employee", ignoreCase = true)

    val isCustomer: Boolean
        get() = userRole.equals("customer", ignoreCase = true)
}
