package com.callreport.app

import android.os.Build
import android.telecom.Call
import android.telecom.CallScreeningService
import android.util.Log

/**
 * Official CallScreeningService implementation.
 * Required to hold ROLE_CALL_SCREENING so that the system
 * can send ACTION_POST_CALL to our CallReportActivity.
 */
class CallScreeningService : CallScreeningService() {

    companion object {
        private const val TAG = "CallScreeningService"
    }

    override fun onScreenCall(callDetails: Call.Details) {
        Log.d(TAG, "onScreenCall: direction=${callDetails.callDirection}, handle=${callDetails.handle}")

        // We allow all calls. Our goal is only the post-call screen.
        // Respond within 5 seconds as required by the platform.
        val response = CallResponse.Builder()
            .setDisallowCall(false)
            .setRejectCall(false)
            .setSilenceCall(false)
            .setSkipCallLog(false)
            .setSkipNotification(false)
            .build()

        respondToCall(callDetails, response)
    }
}
