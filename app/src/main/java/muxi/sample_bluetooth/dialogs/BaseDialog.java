package muxi.sample_bluetooth.dialogs;

import android.support.v7.app.AlertDialog;
import android.content.Context;


public abstract class BaseDialog {

    public void createDialog(AlertDialog.Builder builder){
        builder.create();
    }
    public void showDialog(AlertDialog.Builder builder){
        builder.show();
    }
}
