package com.callreport.app

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.CountDownTimer
import android.telecom.TelecomManager
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import java.util.concurrent.TimeUnit

/**
 * This Activity is launched by the system via ACTION_POST_CALL
 * when the app holds ROLE_CALL_SCREENING.
 */
class CallReportActivity : AppCompatActivity() {

    private var timer: CountDownTimer? = null
    private lateinit var tvTimer: TextView
    private lateinit var tvName: TextView
    private lateinit var tvNumber: TextView
    private lateinit var tvToday: TextView
    private lateinit var tvLastCall: TextView
    private lateinit var tvLastCallDate: TextView
    private lateinit var btnClose: Button
    private lateinit var btnCallback: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_call_report)

        tvTimer = findViewById(R.id.tvTimer)
        tvName = findViewById(R.id.tvName)
        tvNumber = findViewById(R.id.tvNumber)
        tvToday = findViewById(R.id.tvToday)
        tvLastCall = findViewById(R.id.tvLastCall)
        tvLastCallDate = findViewById(R.id.tvLastCallDate)
        btnClose = findViewById(R.id.btnClose)
        btnCallback = findViewById(R.id.btnCallback)

        // Extract data from ACTION_POST_CALL extras (if available)
        val handle: Uri? = intent.getParcelableExtra(TelecomManager.EXTRA_HANDLE)
        val numberFromIntent = handle?.schemeSpecificPart

        // Load real data from CallLog for accuracy
        val lastCall = CallStatsHelper.getLastCall(this)
        val (todayDuration, todayCount) = CallStatsHelper.getTodayStats(this)

        val displayNumber = numberFromIntent ?: lastCall?.number ?: "Unknown"
        val displayName = CallStatsHelper.getContactName(this, displayNumber)
            ?: lastCall?.name
            ?: "Unknown"

        tvName.text = displayName
        tvNumber.text = displayNumber

        // Today stats
        val todayText = "${CallStatsHelper.formatDuration(todayDuration)} ($todayCount calls)"
        tvToday.text = todayText

        // Last call
        if (lastCall != null) {
            val typeStr = CallStatsHelper.formatCallType(lastCall.type)
            val durStr = CallStatsHelper.formatDuration(lastCall.durationSec)
            tvLastCall.text = "$typeStr · $durStr"
            tvLastCallDate.text = CallStatsHelper.formatExactDate(lastCall.dateMillis)
        } else {
            tvLastCall.text = "—"
            tvLastCallDate.text = "—"
        }

        // 5 second auto-close timer (visual only)
        startTimer()

        btnClose.setOnClickListener {
            finish()
        }

        btnCallback.setOnClickListener {
            val num = displayNumber.replace(Regex("[^0-9+]"), "")
            if (num.isNotBlank()) {
                val callIntent = Intent(Intent.ACTION_CALL).apply {
                    data = Uri.parse("tel:$num")
                }
                try {
                    startActivity(callIntent)
                } catch (e: SecurityException) {
                    // Fallback to dial
                    val dialIntent = Intent(Intent.ACTION_DIAL).apply {
                        data = Uri.parse("tel:$num")
                    }
                    startActivity(dialIntent)
                }
            }
            finish()
        }
    }

    private fun startTimer() {
        timer = object : CountDownTimer(5000, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                val sec = TimeUnit.MILLISECONDS.toSeconds(millisUntilFinished) + 1
                tvTimer.text = "${sec}s"
            }

            override fun onFinish() {
                tvTimer.text = "0s"
                // Optional: auto finish after timer
                // finish()
            }
        }.start()
    }

    override fun onDestroy() {
        timer?.cancel()
        super.onDestroy()
    }
}
