package com.example.util

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast

object FeeUtils {

    enum class MonthInfo(val monthNumber: Int, val englishName: String, val hindiName: String) {
        JANUARY(1, "January", "जनवरी"),
        FEBRUARY(2, "February", "फरवरी"),
        MARCH(3, "March", "मार्च"),
        APRIL(4, "April", "अप्रैल"),
        MAY(5, "May", "मई"),
        JUNE(6, "June", "जून"),
        JULY(7, "July", "जुलाई"),
        AUGUST(8, "August", "अगस्त"),
        SEPTEMBER(9, "September", "सितंबर"),
        OCTOBER(10, "October", "अक्टूबर"),
        NOVEMBER(11, "November", "नवंबर"),
        DECEMBER(12, "December", "दिसंबर");

        companion object {
            fun getByNumber(month: Int): MonthInfo {
                return values().firstOrNull { it.monthNumber == month } ?: APRIL
            }
        }
    }

    // Standard school session order: April to March
    val ACADEMIC_MONTHS = listOf(
        MonthInfo.APRIL, MonthInfo.MAY, MonthInfo.JUNE, MonthInfo.JULY,
        MonthInfo.AUGUST, MonthInfo.SEPTEMBER, MonthInfo.OCTOBER, MonthInfo.NOVEMBER,
        MonthInfo.DECEMBER, MonthInfo.JANUARY, MonthInfo.FEBRUARY, MonthInfo.MARCH
    )

    fun getMonthName(month: Int, inHindi: Boolean = false): String {
        val info = MonthInfo.getByNumber(month)
        return if (inHindi) info.hindiName else info.englishName
    }

    /**
     * Format phone number for WhatsApp URL (adds country code if needed)
     */
    fun formatWhatsAppNumber(mobile: String): String {
        val digitsOnly = mobile.replace(Regex("[^0-9]"), "")
        return if (digitsOnly.length == 10) {
            "91$digitsOnly" // Default to India country code 91
        } else {
            digitsOnly
        }
    }

    /**
     * Send WhatsApp message directly via Intent
     */
    fun sendWhatsAppReminder(
        context: Context,
        mobileNumber: String,
        message: String
    ) {
        try {
            val formattedPhone = formatWhatsAppNumber(mobileNumber)
            val encodedMsg = Uri.encode(message)
            val whatsappUrl = "https://api.whatsapp.com/send?phone=$formattedPhone&text=$encodedMsg"
            
            val intent = Intent(Intent.ACTION_VIEW).apply {
                data = Uri.parse(whatsappUrl)
                setPackage("com.whatsapp")
            }
            
            // Try launching WhatsApp app directly
            try {
                context.startActivity(intent)
            } catch (e: Exception) {
                // If official WhatsApp package not found, open browser / WhatsApp web
                val webIntent = Intent(Intent.ACTION_VIEW, Uri.parse(whatsappUrl))
                context.startActivity(webIntent)
            }
        } catch (e: Exception) {
            Toast.makeText(
                context,
                "Error sending WhatsApp message: ${e.localizedMessage}",
                Toast.LENGTH_LONG
            ).show()
        }
    }

    fun formatCurrency(amount: Double): String {
        return "₹${String.format("%,.0f", amount)}"
    }
}
