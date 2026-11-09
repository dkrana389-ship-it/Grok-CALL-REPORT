package com.callreport.app

import android.Manifest
import android.app.role.RoleManager
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat

class MainActivity : AppCompatActivity() {

    private lateinit var statusText: TextView
    private lateinit var btnRequestRole: Button
    private lateinit var btnRequestPermissions: Button

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val allGranted = permissions.all { it.value }
        updateStatus()
        if (allGranted) {
            Toast.makeText(this, "Permissions granted", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(this, "Some permissions denied. Call stats may be limited.", Toast.LENGTH_LONG).show()
        }
    }

    private val requestRoleLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) {
        updateStatus()
        if (isCallScreeningRoleHeld()) {
            Toast.makeText(this, "Call Screening role granted! After-call screen is now active.", Toast.LENGTH_LONG).show()
        } else {
            Toast.makeText(this, "Role not granted. After-call screen will not appear automatically.", Toast.LENGTH_LONG).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        statusText = findViewById(R.id.statusText)
        btnRequestRole = findViewById(R.id.btnRequestRole)
        btnRequestPermissions = findViewById(R.id.btnRequestPermissions)

        btnRequestPermissions.setOnClickListener { requestNeededPermissions() }
        btnRequestRole.setOnClickListener { requestCallScreeningRole() }

        updateStatus()
    }

    override fun onResume() {
        super.onResume()
        updateStatus()
    }

    private fun updateStatus() {
        val permOk = hasRequiredPermissions()
        val roleOk = isCallScreeningRoleHeld()

        val sb = StringBuilder()
        sb.append("Permissions: ").append(if (permOk) "✅ Granted" else "❌ Missing").append("\n")
        sb.append("Call Screening Role: ").append(if (roleOk) "✅ Active" else "❌ Not set").append("\n\n")

        if (roleOk && permOk) {
            sb.append("Ready! After any supported call ends, the Call Report screen will appear automatically.")
        } else {
            sb.append("Please grant permissions and set this app as Caller ID & spam app.")
        }
        statusText.text = sb.toString()
    }

    private fun hasRequiredPermissions(): Boolean {
        val needed = mutableListOf(
            Manifest.permission.READ_PHONE_STATE,
            Manifest.permission.READ_CALL_LOG,
            Manifest.permission.READ_CONTACTS
        )
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            needed.add(Manifest.permission.POST_NOTIFICATIONS)
        }
        return needed.all {
            ContextCompat.checkSelfPermission(this, it) == PackageManager.PERMISSION_GRANTED
        }
    }

    private fun requestNeededPermissions() {
        val needed = mutableListOf(
            Manifest.permission.READ_PHONE_STATE,
            Manifest.permission.READ_CALL_LOG,
            Manifest.permission.READ_CONTACTS
        )
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            needed.add(Manifest.permission.POST_NOTIFICATIONS)
        }
        requestPermissionLauncher.launch(needed.toTypedArray())
    }

    private fun isCallScreeningRoleHeld(): Boolean {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) return false
        val roleManager = getSystemService(RoleManager::class.java) ?: return false
        return roleManager.isRoleHeld(RoleManager.ROLE_CALL_SCREENING)
    }

    private fun requestCallScreeningRole() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
            Toast.makeText(this, "Call Screening role requires Android 10+", Toast.LENGTH_LONG).show()
            return
        }
        val roleManager = getSystemService(RoleManager::class.java) ?: return
        if (!roleManager.isRoleAvailable(RoleManager.ROLE_CALL_SCREENING)) {
            Toast.makeText(this, "Call Screening role is not available on this device", Toast.LENGTH_LONG).show()
            return
        }
        if (roleManager.isRoleHeld(RoleManager.ROLE_CALL_SCREENING)) {
            Toast.makeText(this, "Already holding Call Screening role", Toast.LENGTH_SHORT).show()
            return
        }
        val intent = roleManager.createRequestRoleIntent(RoleManager.ROLE_CALL_SCREENING)
        requestRoleLauncher.launch(intent)
    }
}
