package muxi.sample_bluetooth;

import android.app.ProgressDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Objects;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

import static muxi.sample_bluetooth.AppConstants.DEFAULT_USE_PP;
import static muxi.sample_bluetooth.AppConstants.RADIO_GROUP_CREDIT;
import static muxi.sample_bluetooth.AppConstants.RADIO_GROUP_DEBIT;
import static muxi.sample_bluetooth.AppConstants.RADIO_GROUP_VOUCHER;
import muxi.payservices.sdk.data.MPSTransaction;
import muxi.payservices.sdk.data.MPSResult;
import muxi.payservices.sdk.data.ReceiptType;
import muxi.payservices.sdk.service.IMPSManager;
import muxi.sample_bluetooth.dialogs.DialogCallback;
import muxi.sample_bluetooth.dialogs.DialogHelper;
import muxi.payservices.sdk.service.CallbackAnswer;
import muxi.payservices.sdk.service.MPSManager;
import muxi.sample_bluetooth.tasks.GenericTask;
import muxi.sample_bluetooth.tasks.InitTask;
import muxi.sample_bluetooth.tasks.TransactionTask;
import muxi.sample_bluetooth.utils.FormatUtils;


public class MainActivity extends AppCompatActivity implements BluetoothList.BtComunication, DialogCallback {

    private static final String TAG = MainActivity.class.getSimpleName();

    private static final boolean DEFAULT_PP_MESSAGE = true;
    private static final String DESENV_MERCHANT_ID= "9876";

    private int DEFAULT_POSITION = 4;

    private ActionBarDrawerToggle mDrawerToggle;


    @BindView(R.id.et_value)
    EditText mTextValue;
    @BindView(R.id.radioGroup)
    RadioGroup mTypeRadioGroup;
    @BindView(R.id.drawer_layout)
    DrawerLayout mDrawerLayout;
    @BindView(R.id.my_toolbar)
    Toolbar mToolbar;
    @BindView(R.id.nav_view)
    NavigationView mNavigationView;
    @BindView(R.id.tv_last_transaction)
    TextView mLastTransaction;
    @BindView(R.id.tv_value_last_transaction)
    TextView mValueLastTransaction;
    @BindView(R.id.tv_date_time_last_transaction)
    TextView mDateTimeLastTransaction;

    private DialogHelper dialogHelper;
    private ProgressDialog mProgressDialog;

    private Spinner mBluetoothDevice;
    private ImageView mImageBtStatus;
    private ImageView mImageInitStatus;

    private IMPSManager mpsManager;
    private MPSResult resultOfCall = new MPSResult();

    private static String mCurrentSelectedDevice;
    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;
    private BluetoothList bluetoothList;

    private boolean isBounded = false;

    enum COMMAND {
        INIT,
        TRANSACT,
        CANCEL,
        GENERIC,
        DECONFIGURE,
        IDLE
    }

    COMMAND currentCommand = COMMAND.IDLE;

    String merchantId = DESENV_MERCHANT_ID;

    String clientReceipt = "";
    String establishmentReceipt = "";

    boolean rate = false;
    @Override
    public void updateStatusBt() {
        updateStatus(this);
    }

    private int defaultInstalments = 0;


    private ArrayAdapter<String> adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setup();
    }

    private void setup() {
        setContentView(R.layout.activity_main);
        sharedPreferences = getSharedPreferences(getString(R.string.pref_key), Context.MODE_PRIVATE);
        merchantId = sharedPreferences.getString(getString(R.string.pref_merchant_id_key),DESENV_MERCHANT_ID);
        mBluetoothDevice = findViewById(R.id.spinner_pinpad_name);
        ButterKnife.bind(this);
        if (bluetoothList == null){
            bluetoothList = new BluetoothList(this,mBluetoothDevice);
        }
        bluetoothList.setBtLinester(this);
        adapter = bluetoothList.createAdapter();

        mProgressDialog = new ProgressDialog(this);
        mImageBtStatus = findViewById(R.id.iv_paired_status);
        mImageInitStatus = findViewById(R.id.iv_initialization_status);
        mTextValue.addTextChangedListener(MaskWatcher.mask(mTextValue));
        mTypeRadioGroup.check(R.id.radioButton_credit);
        mTextValue.requestFocus();
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm != null) {
            imm.showSoftInput(mTextValue, InputMethodManager.SHOW_IMPLICIT);
        }
        //Instantiate Dialogs
        dialogHelper = new DialogHelper(this,this);

        setSupportActionBar(mToolbar);
        mDrawerToggle = new ActionBarDrawerToggle(this,mDrawerLayout,mToolbar,R.string.open_drawer,R.string.close_drawer);
        mDrawerLayout.addDrawerListener(mDrawerToggle);
        mDrawerToggle.syncState();


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        }
        setupNavMenu();
        updateMerchantId(merchantId);
    }

    @Override
    public void onBackPressed(){}

    void updateStatus(Context context) {
        if(mpsManager!= null){

            if(!mpsManager.isInitialized()){
                mImageInitStatus.setBackground(context.getResources().getDrawable(R.drawable.circle_off));
            }
            else{
                mImageInitStatus.setBackground(context.getResources().getDrawable(R.drawable.circle_on));
            }

            if(mBluetoothDevice.getSelectedItem().toString().equals
                    (context.getResources().getString(R.string.no_pinpad_selected))){
                mImageBtStatus.setBackground(context.getResources().getDrawable(R.drawable.circle_off));
            }
            else{
                mImageBtStatus.setBackground(context.getResources().getDrawable(R.drawable.circle_on));
            }
        }


    }

    private void setupNavMenu(){
        mNavigationView.setNavigationItemSelectedListener(
                new NavigationView.OnNavigationItemSelectedListener() {
                    @Override
                    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                        mDrawerLayout.closeDrawer(GravityCompat.START);
                        switch (item.getItemId()) {
                            case R.id.action_start:
                                if(!isBounded){
                                    currentCommand = COMMAND.INIT;
                                    isBounded = mpsManager.bindService(getApplicationContext());

                                }else{
                                    callInit();
                                }
                                return true;
                            case R.id.action_deconfigure:
                                if(!isBounded){
                                    currentCommand = COMMAND.DECONFIGURE;
                                    isBounded = mpsManager.bindService(getApplicationContext());
                                }else{
                                    mpsManager.deconfigure(true);
                                }
                                return true;
                            case R.id.action_establishment:
                                dialogHelper.showEstablishmentDialog(sharedPreferences);
                                return true;
                            case R.id.action_cancelTransaction:
                                dialogHelper.showVoidAnyDialog();
                                return true;
                            case R.id.action_getVersion:
                                if(!isBounded){
                                    currentCommand = COMMAND.GENERIC;
                                    isBounded = mpsManager.bindService(getApplicationContext());
                                }else{
                                    callGeneric();
                                }
                            case R.id.action_stopService:
                                mpsManager.stopService(getApplicationContext());
                                return true;
                            default:
                                return false;
                        }
                    }
                });
    }

    private void callGeneric() {
        final GenericTask taskVersions =
                new GenericTask(mProgressDialog,
                        AppConstants.GET_VERSIONS_CMD,
                        null,
                        mpsManager,
                        getResources().getString(R.string.loading));
        taskVersions.execute();
    }

    private void callInit() {
        final InitTask taskInit =
                new InitTask(mProgressDialog,
                        merchantId,
                        DEFAULT_PP_MESSAGE,
                        mpsManager,
                        BuildConfig.API_KEY,
                        getResources().getString(R.string.loading));
        taskInit.execute();
    }

    private void updateMerchantId(String merchantId) {
        this.merchantId = merchantId;
        View header = mNavigationView.getHeaderView(0);
        TextView mMerchantId= header.findViewById(R.id.tv_merchantIdHeader);
        String text = "MerchantId " + merchantId;
        mMerchantId.setText(text);

    }

    private void setLastTransactionData(String titleLastTransaction, String typePayment,
                                        String valueLastTransaction,
                                        String dateTimeLastTransaction, String valueDefault,
                                        String clientReceipt, String establishmentReceipt) {
        if(typePayment.equals(MPSTransaction.TransactionMode.CREDIT.name())){
            typePayment = getResources().getString(R.string.credit)+" | ";
        }
        else{
            if(typePayment.equals(MPSTransaction.TransactionMode.DEBIT.name())){

                typePayment = getResources().getString(R.string.debit)+" | ";
            }
            else{
                if(typePayment.equals(MPSTransaction.TransactionMode.VOUCHER.name())) {
                    typePayment = getResources().getString(R.string.voucher) + " | ";
                }
            }
        }
        mLastTransaction.setText(titleLastTransaction);
        mValueLastTransaction.setText(valueLastTransaction);
        String dateTimeandType = typePayment+dateTimeLastTransaction;
        mDateTimeLastTransaction.setText(dateTimeandType);
        mTextValue.setText(valueDefault);
        this.clientReceipt = clientReceipt;
        this.establishmentReceipt = establishmentReceipt;
    }



    MPSTransaction currentMpsTransaction = null;
    @OnClick(R.id.btn_cancel)
    public void onBtnCancel()
    {
        //TODO add a protection for sending null in transactionMode for cancel last transaction.
        //Actually this field is ignored, but still necessary to avoid crash
        currentMpsTransaction = createTransaction("", MPSTransaction.TransactionMode.CREDIT,"","", 0,false);

        if(isBounded){
            callTransact(currentMpsTransaction, AppConstants.TransactionState.cancel);

        }else{
            isBounded = mpsManager.bindService(this);
            currentCommand = COMMAND.CANCEL;
        }
    }

    @OnClick(R.id.btn_reprint)
    public void onBtnReprint(){
        if(clientReceipt.equals("")){
            Toast.makeText(this, getResources().getString(R.string.empty_receipt), Toast.LENGTH_SHORT).show();
        }else {
            dialogHelper.showReprintDialog();
        }
    }
    MPSTransaction.TransactionMode transactionMode;

    @OnClick(R.id.btn_pay)
    public void onBtnPay(){
        if (DEFAULT_USE_PP) {
            transactionMode = getSelectedType(mTypeRadioGroup);
        }
        if(transactionMode.equals(MPSTransaction.TransactionMode.CREDIT)){
            dialogHelper.showTransactionTypeDialog(transactionMode);
        }
        else{
            if(isBounded){
                makePayment(transactionMode,defaultInstalments);
            }else{
                isBounded = mpsManager.bindService(this);
                currentCommand = COMMAND.TRANSACT;
            }
        }
    }


    private MPSTransaction.TransactionMode getSelectedType(RadioGroup mTypeRadioGroup) {
        int radioButtonID = mTypeRadioGroup.getCheckedRadioButtonId();
        View radioButton = mTypeRadioGroup.findViewById(radioButtonID);
        int indexChecked = mTypeRadioGroup.indexOfChild(radioButton);

        MPSTransaction.TransactionMode transactionMode = null;
        switch (indexChecked) {
            case RADIO_GROUP_CREDIT:
                transactionMode = MPSTransaction.TransactionMode.CREDIT;
                break;
            case RADIO_GROUP_DEBIT:
                transactionMode = MPSTransaction.TransactionMode.DEBIT;
                break;
            case RADIO_GROUP_VOUCHER:
                transactionMode = MPSTransaction.TransactionMode.VOUCHER;
                break;
        }
        return transactionMode;
    }


    private void createToast(final String msg) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(MainActivity.this, msg , Toast.LENGTH_SHORT).show();
            }
        });
    }

    private static String currentValue = "";
    private static MPSTransaction.TransactionMode typePayment;
    private String currentNumericValue = "";

    private void makePayment(MPSTransaction.TransactionMode type, int installmentsNumber) {
        currentNumericValue = mTextValue.getText().toString();
        String value = FormatUtils.getValueReplaced(currentNumericValue);
        currentValue = value;
        typePayment = type;

        if (value.equals("") || value.equals("000")) {
            createToast(getString(R.string.empty_value));
        }else{
            Log.d(TAG,"Make payment value = "+value);

            MPSTransaction transaction = createTransaction(currentValue,typePayment,"", "", installmentsNumber,rate);
            TransactionTask transactionTask = new TransactionTask(mProgressDialog,mpsManager,transaction, AppConstants.TransactionState.payment);
            transactionTask.execute();
        }
    }

    private MPSTransaction createTransaction(String value, MPSTransaction.TransactionMode type,
                                             String cv, String aut,int installmentsNumber, boolean rate) {
        MPSTransaction transaction = new MPSTransaction();
        transaction.setAmount(value);
        transaction.setCurrency(MPSTransaction.CurrencyType.BRL);
        transaction.setInstallments(installmentsNumber);
        transaction.setTransactionMode(type);
        transaction.setCv(cv);
        transaction.setAuth(aut);
        transaction.setRate(rate);
        transaction.setMerchantId(merchantId);

        return transaction;
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (mpsManager == null){
            Log.d(TAG, "Instantiating  mpsManager ");
            mpsManager = new MPSManager(this.getApplicationContext());
            bluetoothList.setMpsManager(mpsManager);
        }

        //TODO: Actually this can be done here because we never use stopService. If we did, we won't
        //be able to do anything without check service is bound or minimize and resume app.
        isBounded = mpsManager.bindService(getApplicationContext());
        Log.d(TAG, "bindService result =" + isBounded);
        mpsManager.setMpsManagerCallback(new CallbackAnswer(){
            @Override
            public void onInitAnswer(final MPSResult mpsResult) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        String text;
                        updateStatus(getApplicationContext());
                        if(mpsResult.getStatus() == MPSResult.Status.SUCCESS) {
                            text = getResources().getString(R.string.initialize_success);
                        }else{
                            text = mpsResult.getDescriptionError();
                        }
                        createToast(text);

                    }
                });
            }

            @Override
            public void onTransactionAnswer(final MPSResult mpsResult) {
                resultOfCall = mpsResult;

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mProgressDialog.dismiss();
                        mTextValue.setSelection(DEFAULT_POSITION);
                        if(resultOfCall != null){
                            if (resultOfCall.getStatus() == MPSResult.Status.SUCCESS){
                                Log.d(TAG,"MAIN RECEIPT " + resultOfCall.getClientReceipt());
                                dialogHelper.showTransactionDialog(getResources().getString(R.string.payment_approved),
                                        true, resultOfCall.getClientReceipt(),
                                        resultOfCall.getEstablishmentReceipt());
                                String date = FormatUtils.getCurrentDate();
                                String time = FormatUtils.getCurrentTime(false);
                                String dateTime = date+" "+time;
                                String valueLastTransaction = getResources().getString(R.string.prefix_currency)
                                        + currentNumericValue;
                                setLastTransactionData(getResources().getString(R.string.tv_last_transaction),typePayment.name(),
                                        valueLastTransaction,
                                        dateTime,
                                        getResources().getString(R.string.value_default), resultOfCall.getClientReceipt(),
                                        resultOfCall.getEstablishmentReceipt());
                            }else{
                                Log.d(TAG,"onError " + resultOfCall.getDescriptionError());
                                String descriptionError = resultOfCall.getDescriptionError();
                                dialogHelper.showTransactionDialog(descriptionError, false,descriptionError,"");
                            }
                        }else{
                            createToast(getString(R.string.generic_error));
                            Log.d(TAG,"Result of call null");
                        }
                    }
                });

            }

            @Override
            public void onCancelAnswer(MPSResult mpsResult) {
                resultOfCall = mpsResult;
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mProgressDialog.dismiss();
                        if (resultOfCall != null) {
                            if (resultOfCall.getStatus() == MPSResult.Status.SUCCESS) {
                                Log.d(TAG, "MAIN RECEIPT " + resultOfCall.getClientReceipt());
                                dialogHelper.showTransactionDialog(getResources().getString(R.string.cancel_approved), true,
                                        resultOfCall.getClientReceipt(), resultOfCall.getEstablishmentReceipt());
                                setLastTransactionData(getResources().getString(R.string.tv_no_last_transaction), "",
                                        "",
                                        "",
                                        getResources().getString(R.string.value_default), resultOfCall.getClientReceipt(), resultOfCall.getEstablishmentReceipt());
                            } else {
                                String descriptionError = resultOfCall.getDescriptionError();
                                Log.d(TAG, "onError " + descriptionError);
                                dialogHelper.showTransactionDialog(descriptionError, false, descriptionError, "");
                            }
                        }else{
                            createToast(getResources().getString(R.string.generic_error));
                            Log.d(TAG,"Result of call null");
                        }
                    }
                });
            }

            @Override
            public void onDeconfigureAnswer(MPSResult mpsResult) {
                if(mpsResult.getStatus() == MPSResult.Status.SUCCESS){
                    bluetoothList.setWhenDeconfigure();
                    bluetoothList.updateItemsOnSpinner(adapter);
                    mCurrentSelectedDevice = getResources().getString(R.string.no_pinpad_selected);
                    setLastTransactionData(getResources().getString(R.string.tv_no_last_transaction),"",
                            "",
                            "",
                            getResources().getString(R.string.value_default),"", "");
                    updateStatus(MainActivity.this);
                    createToast(getResources().getString(R.string.deconfig_success));
                }else{
                    createToast(getResources().getString(R.string.deconfig_error));
                }
            }

            @Override
            public void onGenericCommand(MPSResult mpsResult) {
                resultOfCall = mpsResult;

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mProgressDialog.dismiss();
                        if(resultOfCall != null) {
                            if (resultOfCall.getStatus() == MPSResult.Status.SUCCESS){
                                String applicationVersion = resultOfCall.getApplicationVersion();
                                String poswebVersion = resultOfCall.getPoswebVersion();
                                dialogHelper.showVersionsDialog(applicationVersion, poswebVersion);
                            } else {
                                String descriptionError = resultOfCall.getDescriptionError();
                                Log.d(TAG, "onError " + descriptionError);
                                createToast(descriptionError);
                            }
                        } else {
                            createToast(getString(R.string.generic_error));
                            Log.d(TAG,"Result of call null");
                        }
                    }
                });
            }

            @Override
            public void onPrintAnswer(MPSResult mpsResult) {
                super.onPrintAnswer(mpsResult);
            }


            @Override
            public void onServiceDisconnected() {
                Log.d(TAG,"onServiceDisconnected sample");
                createToast(getResources().getString(R.string.service_not_initialized));
                isBounded = false;
            }

            @Override
            public void onServiceConnected() {
                super.onServiceConnected();
                isBounded = true;
                switch (currentCommand){
                    case INIT:
                        callInit();
                        break;
                    case DECONFIGURE:
                        mpsManager.deconfigure(true);
                        break;
                    case GENERIC:
                        callGeneric();
                        break;
                    case TRANSACT:
                        makePayment(transactionMode,defaultInstalments);
                        break;
                    case CANCEL:
                        callTransact(currentMpsTransaction, AppConstants.TransactionState.cancel);
                        break;
                }
            }
        });
        bluetoothList.updateItemsOnSpinner(adapter);
    }

    @Override
    protected void onDestroy() {
        mpsManager.stopService(getApplicationContext());
        super.onDestroy();
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        // Sync the toggle state after onRestoreInstanceState has occurred.
        mDrawerToggle.syncState();
    }

    @Override
    public void onClickPay(MPSTransaction.TransactionMode transactionMode, int installmentsNumber, boolean isAdmRate) {
        this.rate = isAdmRate;
        this.transactionMode = transactionMode;
        this.defaultInstalments = installmentsNumber;
        if(isBounded){
            makePayment(transactionMode,installmentsNumber);
        }else{
            currentCommand = COMMAND.TRANSACT;
            isBounded = mpsManager.bindService(this);
        }
    }

    @Override
    public void onClickEstablishment(String numberOfEstablishment) {
        createToast(getResources().getString(R.string.changed_merchant_id));
        updateMerchantId(numberOfEstablishment);
    }

    @Override
    public void onClickVoidAny(RadioGroup typePaymentChecked, String cv, String auth) {
        MPSTransaction.TransactionMode transactionMode;
        transactionMode = getSelectedType(typePaymentChecked);
        Log.d(TAG,"Transaction type for cancel "+ transactionMode.name());
        currentMpsTransaction = createTransaction("", transactionMode,
                cv, auth, defaultInstalments,rate);

        if(isBounded){
            callTransact(currentMpsTransaction, AppConstants.TransactionState.cancel);
        }else{
            isBounded = mpsManager.bindService(this);
            currentCommand = COMMAND.CANCEL;
        }
    }

    private void callTransact(MPSTransaction currentMpsTransaction, AppConstants.TransactionState type) {
        TransactionTask transactionTask = new TransactionTask(mProgressDialog,
                mpsManager, currentMpsTransaction, type);
        transactionTask.execute();
    }

    @Override
    public void onClickReprint(boolean isEstablishmentReceipt) {
        ReceiptType receiptType = isEstablishmentReceipt ? ReceiptType.ESTABLISHMENT:ReceiptType.CUSTOMER;
        mpsManager.reprintLastTransaction(receiptType);
    }

    @Override
    public void onClickPrintCustomer() {
        mpsManager.printCustomerReceipt();
    }
}
