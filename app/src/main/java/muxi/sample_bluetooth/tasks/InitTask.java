package muxi.sample_bluetooth.tasks;

import android.app.ProgressDialog;
import android.os.AsyncTask;

import muxi.payservices.sdk.service.IMPSManager;
public class InitTask extends AsyncTask<Void,Void,Void> {

    private ProgressDialog progressDialog;
    private String cnpj;
    private boolean defaultPinpadmsg;
    private IMPSManager mpsManager;


    public InitTask(ProgressDialog progressDialog,
                    String cnpj,
                    boolean defaultPinpadMsg,
                    IMPSManager mpsManager){
        this.progressDialog = progressDialog;
        this.cnpj = cnpj;
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
