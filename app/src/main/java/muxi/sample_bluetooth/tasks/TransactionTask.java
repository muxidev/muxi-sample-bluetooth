package muxi.sample_bluetooth.tasks;

import android.app.ProgressDialog;
import android.os.AsyncTask;

import muxi.payservices.sdk.service.IMPSManager;
import muxi.sample_bluetooth.AppConstants;
import muxi.payservices.sdk.data.MPSTransaction;
import muxi.payservices.sdk.service.MPSManager;


public class TransactionTask extends AsyncTask<Void,Void,Void> {


    private ProgressDialog progressDialog;
    private IMPSManager mpsManager;
    private MPSTransaction mpsTransaction;
    private AppConstants.TransactionState mTransactionState;


    public TransactionTask(ProgressDialog progressDialog,
                           IMPSManager mpsManager,
                           MPSTransaction mpsTransaction,
                           AppConstants.TransactionState mTransactionState
                           ){
        this.progressDialog = progressDialog;
        this.mpsManager = mpsManager;
        this.mpsTransaction = mpsTransaction;
        this.mTransactionState = mTransactionState;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        //Todo remover este marretado
        progressDialog.setMessage("Waiting pinpad...");
        progressDialog.setIndeterminate(false);
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progressDialog.setCancelable(false);
        progressDialog.show();

    }

    @Override
    protected Void doInBackground(Void... voids) {
        switch (mTransactionState){
            case payment:
                mpsManager.transaction(mpsTransaction);
                break;
            case cancel:
                mpsManager.cancelTransaction(mpsTransaction);
                break;
        }
        return null;
    }
}
