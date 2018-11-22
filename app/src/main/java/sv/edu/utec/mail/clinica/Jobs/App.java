package sv.edu.utec.mail.clinica.Jobs;

import android.app.Application;

import com.evernote.android.job.JobManager;

public class App extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        JobManager.create(this).addJobCreator(new StepsJobCreator());
    }
}
