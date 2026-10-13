package org.minimalos.launcher;

import android.app.job.JobInfo;
import android.app.job.JobParameters;
import android.app.job.JobScheduler;
import android.app.job.JobService;
import android.content.ComponentName;
import android.content.Context;

/** Android may defer these best-effort intervals, especially under battery restrictions. */
public final class SyncJobService extends JobService {
    private static final int JOB_ID = 38742;
    private volatile boolean stopped;

    static void schedule(Context context) {
        int minutes = PeerSettings.minutes(context);
        JobInfo.Builder builder = new JobInfo.Builder(JOB_ID,
                new ComponentName(context, SyncJobService.class))
                .setRequiredNetworkType(JobInfo.NETWORK_TYPE_ANY)
                .setMinimumLatency(minutes * 60000L)
                .setOverrideDeadline(minutes * 120000L)
                .setPersisted(true);
        JobScheduler scheduler = (JobScheduler) context.getSystemService(JOB_SCHEDULER_SERVICE);
        scheduler.schedule(builder.build());
    }

    static void cancel(Context context) {
        JobScheduler scheduler = (JobScheduler) context.getSystemService(JOB_SCHEDULER_SERVICE);
        scheduler.cancel(JOB_ID);
    }

    @Override public boolean onStartJob(JobParameters params) {
        stopped = false;
        new Thread(() -> {
            try { SyncWorker.sync(this); }
            catch (Exception failure) { android.util.Log.w("MinimalOS", "Synchronisation différée échouée", failure); }
            finally {
                if (!stopped) {
                    jobFinished(params, false);
                    schedule(this);
                }
            }
        }, "minimal-os-sync-job").start();
        return true;
    }
    @Override public boolean onStopJob(JobParameters params) {
        stopped = true;
        return true;
    }
}
