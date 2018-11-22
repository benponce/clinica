package sv.edu.utec.mail.clinica.Jobs;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.evernote.android.job.Job;
import com.evernote.android.job.JobCreator;

public class StepsJobCreator implements JobCreator {
    @Nullable
    @Override
    public Job create(@NonNull String tag) {
        if (tag.equals(StepsSyncJob.TAG)) {
            return new StepsSyncJob();
        } else {
            return null;
        }
    }
}
