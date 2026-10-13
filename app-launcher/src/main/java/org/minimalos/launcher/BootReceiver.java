package org.minimalos.launcher;
import android.content.*;
import android.app.job.JobScheduler;
/** Retire jobs left by earlier APK versions; no replacement background transfer. */
public final class BootReceiver extends BroadcastReceiver {
    static void retireTransfers(Context context) {
        JobScheduler jobs = (JobScheduler) context.getSystemService(Context.JOB_SCHEDULER_SERVICE);
        if (jobs != null) { jobs.cancel(38742); jobs.cancel(38743); }
    }
    @Override public void onReceive(Context context, Intent intent) {
        if (Intent.ACTION_BOOT_COMPLETED.equals(intent.getAction()) || Intent.ACTION_MY_PACKAGE_REPLACED.equals(intent.getAction())) retireTransfers(context);
    }
}
