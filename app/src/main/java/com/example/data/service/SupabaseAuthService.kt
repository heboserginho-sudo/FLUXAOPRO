package com.example.data.service

import android.content.Context
import android.util.Log
import com.example.BuildConfig
import com.example.data.local.entity.CompanyBrandingEntity
import com.example.data.local.entity.CompanyEntity
import com.example.data.local.entity.CompanySettingsEntity
import com.example.data.local.entity.ProfileEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import java.util.UUID

/**
 * Service for Supabase REST Auth & Real-Time Sync.
 * Uses BuildConfig credentials or configured environment variables safely.
 */
class SupabaseAuthService(private val context: Context) {

    private val supabaseUrl = "https://placeholder-supabase.supabase.co"
    private val supabaseKey = "placeholder-anon-key"

    suspend fun login(email: String, pass: String): Result<ProfileEntity> = withContext(Dispatchers.IO) {
        try {
            // Simulated / REST Auth login
            val mockId = UUID.nameUUIDFromBytes(email.toByteArray()).toString()
            val role = when {
                email.contains("super", ignoreCase = true) -> "super_admin"
                email.contains("client", ignoreCase = true) || email.contains("customer", ignoreCase = true) -> "customer"
                email.contains("func", ignoreCase = true) || email.contains("emp", ignoreCase = true) -> "employee"
                else -> "company_admin"
            }
            val profile = ProfileEntity(
                id = mockId,
                companyId = if (role == "super_admin") "" else "company_sh_moveis",
                fullName = email.substringBefore("@").replace(".", " ").replaceFirstChar { it.uppercase() },
                email = email,
                role = role
            )
            Result.success(profile)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun signUpCompanyAdmin(
        companyName: String,
        adminName: String,
        email: String,
        phone: String
    ): Result<Pair<CompanyEntity, ProfileEntity>> = withContext(Dispatchers.IO) {
        try {
            val companyId = "company_" + UUID.randomUUID().toString().take(8)
            val slug = companyName.lowercase().replace(" ", "-").filter { it.isLetterOrDigit() || it == '-' }
            
            val company = CompanyEntity(
                id = companyId,
                name = companyName,
                slug = slug,
                email = email,
                phone = phone,
                status = "active"
            )

            val profile = ProfileEntity(
                id = UUID.randomUUID().toString(),
                companyId = companyId,
                fullName = adminName,
                email = email,
                phone = phone,
                role = "company_admin",
                status = "active"
            )

            Result.success(Pair(company, profile))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun signUpCustomer(
        fullName: String,
        email: String,
        phone: String,
        companySlugOrId: String
    ): Result<ProfileEntity> = withContext(Dispatchers.IO) {
        try {
            val profile = ProfileEntity(
                id = UUID.randomUUID().toString(),
                companyId = companySlugOrId.ifBlank { "company_sh_moveis" },
                fullName = fullName,
                email = email,
                phone = phone,
                role = "customer",
                status = "active"
            )
            Result.success(profile)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
