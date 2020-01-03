package muxi.sample_bluetooth.tasks;

import android.app.ProgressDialog;
import android.os.AsyncTask;

import java.util.List;

import muxi.payservices.sdk.service.IMPSManager;

public class GenericTask extends AsyncTask<Void,Void,Void> {

    private ProgressDialog progressDialog;
    private IMPSManager mpsManager;
    private String message;
    private String command;
    private List<String> params;


    public GenericTask(ProgressDialog progressDialog,
                       String command,
                       List<String> params,
                       IMPSManager mpsManager,
                       String message) {
        this.progressDialog = progressDialog;
        this.mpsManager = mpsManager;
        this.message = message;
        this.command = command;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        progressDialog.setMessage(message);
        progressDialog.setIndeterminate(false);
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progressDialog.setCancelable(false);
        progressDialog.show();
    }

    @Override
    protected Void doInBackground(Void... voids) {
        mpsManager.genericCommand(command,params);
        return null;
    }
}
