package com.ascendy.app.service

import android.app.admin.DeviceAdminReceiver
import android.app.admin.DevicePolicyManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import com.ascendy.app.R

/**
 * Backs Lockdown mode. While this admin is *active*, Android refuses a direct uninstall —
 * the user must first deactivate it (Settings → Security → Device admin apps), which the
 * accessibility-service Settings-bounce blocks during a locked session. We request no
 * device-admin policies; being active is all we need.
 *
 * This is NOT device-owner — there is no factory-reset provisioning. The user can always
 * deactivate the admin when no session is running, and the mandatory safety timer guarantees
 * every session eventually ends, so Lockdown can never permanently trap anyone.
 */
class AscendyDeviceAdminReceiver : DeviceAdminReceiver() {

    override fun onDisableRequested(context: Context, intent: Intent): CharSequence =
        context.getString(R.string.device_admin_disable_warning)

    companion object {
        fun component(context: Context): ComponentName =
            ComponentName(context, AscendyDeviceAdminReceiver::class.java)

        private fun dpm(context: Context): DevicePolicyManager =
            context.getSystemService(Context.DEVICE_POLICY_SERVICE) as DevicePolicyManager

        fun isActive(context: Context): Boolean =
            dpm(context).isAdminActive(component(context))

        /** Intent that asks the user to activate the admin, with our themed explanation. */
        fun addIntent(context: Context, explanation: String): Intent =
            Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN)
                .putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN, component(context))
                .putExtra(DevicePolicyManager.EXTRA_ADD_EXPLANATION, explanation)

        fun remove(context: Context) {
            val d = dpm(context)
            val c = component(context)
            if (d.isAdminActive(c)) d.removeActiveAdmin(c)
        }
    }
}
