package muxi.sample_bluetooth.tasks;

import android.app.ProgressDialog;
import android.os.AsyncTask;

import muxi.payservices.sdk.service.IMPSManager;
import muxi.sample_bluetooth.BuildConfig;

public class InitTask extends AsyncTask<Void,Void,Void> {

    private ProgressDialog progressDialog;
    private String merchantId;
    private boolean defaultPinpadmsg;
    private IMPSManager mpsManager;


    public InitTask(ProgressDialog progressDialog,
                    String merchantId,
                    boolean defaultPinpadMsg,
                    IMPSManager mpsManager){
        this.progressDialog = progressDialog;
        this.merchantId = merchantId;
        this.defaultPinpadmsg = defaultPinpadMsg;
        this.mpsManager = mpsManager;


    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        //todo tirar este hardcoded
        progressDialog.setMessage("Loading");
        progressDialog.setIndeterminate(false);
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progressDialog.setCancelable(false);
        progressDialog.show();
    }

    @Override
    protected Void doInBackground(Void... voids) {
        mpsManager.initialize(defaultPinpadmsg,cnpj);
        return null;
    }

    @Override
    protected void onPostExecute(Void aVoid) {
        super.onPostExecute(aVoid);
        progressDialog.dismiss();
    }

}
