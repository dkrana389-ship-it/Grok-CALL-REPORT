package com.callreport.app

import android.content.Context
import android.database.Cursor
import android.provider.CallLog
import android.provider.ContactsContract
import android.telephony.PhoneNumberUtils
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit

data class CallInfo(
    val number: String,
    val name: String?,
    val type: Int,          // CallLog.Calls.INCOMING_TYPE etc.
    val durationSec: Long,
    val dateMillis: Long
)

object CallStatsHelper {

    fun getContactName(context: Context, number: String?): String? {
        if (number.isNullOrBlank()) return null
        val uri = ContactsContract.PhoneLookup.CONTENT_FILTER_URI.buildUpon()
            .appendPath(number).build()
        context.contentResolver.query(
            uri,
            arrayOf(ContactsContract.PhoneLookup.DISPLAY_NAME),
            null, null, null
        )?.use { cursor ->
            if (cursor.moveToFirst()) {
                return cursor.getString(0)
            }
        }
        return null
    }

    fun getLastCall(context: Context): CallInfo? {
        val projection = arrayOf(
            CallLog.Calls.NUMBER,
            CallLog.Calls.CACHED_NAME,
            CallLog.Calls.TYPE,
            CallLog.Calls.DURATION,
            CallLog.Calls.DATE
        )
        context.contentResolver.query(
            CallLog.Calls.CONTENT_URI,
            projection,
            null,
            null,
            "${CallLog.Calls.DATE} DESC"
        )?.use { cursor ->
            if (cursor.moveToFirst()) {
                return cursorToCallInfo(cursor)
            }
        }
        return null
    }

    fun getTodayStats(context: Context): Pair<Long, Int> {
        // returns (totalDurationSeconds, callCount)
        val cal = Calendar.getInstance()
        cal.set(Calendar.HOUR_OF_DAY, 0)
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)
        val startOfDay = cal.timeInMillis

        var totalDuration = 0L
        var count = 0

        val projection = arrayOf(CallLog.Calls.DURATION, CallLog.Calls.DATE)
        context.contentResolver.query(
            CallLog.Calls.CONTENT_URI,
            projection,
            "${CallLog.Calls.DATE} >= ?",
            arrayOf(startOfDay.toString()),
            null
        )?.use { cursor ->
            while (cursor.moveToNext()) {
                totalDuration += cursor.getLong(0)
                count++
            }
        }
        return totalDuration to count
    }

    private fun cursorToCallInfo(cursor: Cursor): CallInfo {
        val number = cursor.getString(0) ?: ""
        val name = cursor.getString(1)
        val type = cursor.getInt(2)
        val duration = cursor.getLong(3)
        val date = cursor.getLong(4)
        return CallInfo(number, name, type, duration, date)
    }

    fun formatDuration(seconds: Long): String {
        if (seconds <= 0) return "0 sec"
        val min = TimeUnit.SECONDS.toMinutes(seconds)
        val sec = seconds % 60
        return when {
            min > 0 -> "${min} min ${sec} sec"
            else -> "$sec sec"
        }
    }

    fun formatCallType(type: Int): String {
        return when (type) {
            CallLog.Calls.INCOMING_TYPE -> "Incoming"
            CallLog.Calls.OUTGOING_TYPE -> "Outgoing"
            CallLog.Calls.MISSED_TYPE -> "Missed"
            CallLog.Calls.REJECTED_TYPE -> "Rejected"
            CallLog.Calls.BLOCKED_TYPE -> "Blocked"
            else -> "Unknown"
        }
    }

    fun formatRelativeDate(millis: Long): String {
        val now = System.currentTimeMillis()
        val diff = now - millis
        val days = TimeUnit.MILLISECONDS.toDays(diff)
        return when {
            days == 0L -> "Today"
            days == 1L -> "1 day ago"
            days < 7 -> "$days days ago"
            else -> {
                val sdf = SimpleDateFormat("MMM d, yyyy", Locale.getDefault())
                sdf.format(Date(millis))
            }
        }
    }

    fun formatExactDate(millis: Long): String {
        val sdf = SimpleDateFormat("MMM d, yyyy", Locale.getDefault())
        return sdf.format(Date(millis))
    }
}
