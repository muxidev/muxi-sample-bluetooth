package muxi.sample_bluetooth;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Set;

import muxi.payservices.sdk.service.IMPSManager;


public class BluetoothList {
    private ArrayList<String> pairedName;
    private ArrayList<String> pairedAddress;
    private SharedPreferences sharedPreferences;
    private Context context;
    private Spinner mBluetoothDevice;
    private ArrayList<String> listPairedNames;
    private ArrayList<String> listPairedAddress;
    private IMPSManager mpsManager;
    private BtComunication btComunication;
    private static String mCurrentSelectedDevice = " ";
    private static String mCurrentSelectedName = " ";

    private String TAG = BluetoothList.class.getSimpleName();

    public BluetoothList(Context context,
                         Spinner mBluetoothDevice){
        this.context = context;
        listPairedAddress = new ArrayList<>();
        this.mBluetoothDevice = mBluetoothDevice;
        listPairedNames = new ArrayList<>();
        pairedName = new ArrayList<>();
        pairedAddress = new ArrayList<>();
        sharedPreferences = context.getSharedPreferences(context.getResources().getString(R.string.key_sharedPreference),Context.MODE_PRIVATE);
    }

    public void setMpsManager(IMPSManager mpsManager) {
        this.mpsManager = mpsManager;
    }

    public void setBtLinester(BtComunication btComunication){
        this.btComunication = btComunication;
    }
    public void updateItemsOnSpinner(final ArrayAdapter<String> adapter) {
        boolean statusBluetoothOn = checkBluetoothOn();
        if(statusBluetoothOn){
            listPairedNames.clear();
            listPairedAddress.clear();

            updatePairedDevices();

            String btStatus = checkBluetoothStatus();
            //btData is a array that receive one string with a data
            //the bluetooth separated by '\n'
            //btData[0] is bluetooth name
            //btData[1] is bluetooth address
            String btData[] = btStatus.split("\n");
            if(btData[0].equals(" ") && btData[1].equals(" ")){
                listPairedNames.add(context.getResources().getString(R.string.no_pinpad_selected));
            }else{
                listPairedNames.add(btData[0]);
            }
            listPairedAddress.add(btData[1]);


            listPairedNames.addAll(getPairedName());
            listPairedAddress.addAll(getPairedAddress());

            adapter.setDropDownViewResource(R.layout.simple_dropdown_item);
            mBluetoothDevice.setAdapter(adapter);
            mBluetoothDevice.setOnTouchListener(new View.OnTouchListener() {
                @SuppressLint("ClickableViewAccessibility")
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    if (event.getAction() == MotionEvent.ACTION_DOWN) {
                        boolean checkBt = checkBluetoothOn();

                        if(checkBt){

                            //Check if exists one or more listPairedNames bonded
                            if(!checkPairedDevices()){

                                openBluetooth(context.getApplicationContext());
                            }
                            else{
                                updateItemsOnSpinner(adapter);
                            }
                        }
                        else{
                            enableBluetooth();
                        }

                    }
                    return false;
                }
            });

            mBluetoothDevice.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    mCurrentSelectedName = mBluetoothDevice.getSelectedItem().toString();
                    mCurrentSelectedDevice = listPairedAddress.get(position);
                    if(mpsManager != null){
                        Log.d(TAG,"Saving current bluetooth mac address " + mCurrentSelectedDevice);
                        mpsManager.setCurrentBluetoothDevice(mCurrentSelectedDevice);
                    }
                    //When select new device, update the position 0
                    //to indicate that this device is selected
                    listPairedNames.remove(0);
                    listPairedNames.add(0, mCurrentSelectedName);
                    adapter.notifyDataSetChanged();
                    btComunication.updateStatusBt();
                }

                @Override
                public void onNothingSelected(AdapterView<?> parent) {

                }
            });
        }
        else{
            enableBluetooth();
            updateItemsOnSpinner(adapter);
        }


    }

    public String getBTName(){
        return mCurrentSelectedName;
    }
    public String getBTDevice(){
        return mCurrentSelectedDevice;
    }

    public interface BtComunication{
        void updateStatusBt();
    }

    public ArrayAdapter<String> createAdapter(){
        return new ArrayAdapter<String>(context,
                R.layout.simple_list_item,R.id.tv_spinner, listPairedNames){
            @Override
            public boolean isEnabled(int position){
                return position != 0;
            }

            @Override
            public View getDropDownView(int position, View convertView, @NonNull ViewGroup parent) {

                View view = super.getDropDownView(position, null, parent);
                // If this is the selected item position
                if (position == 0) {
                    TextView textview = view.findViewById(R.id.tv_spinner);
                    textview.setTextColor(context.getResources().getColor(R.color.turquoise_blue_dark));
                    ImageView imageView = view.findViewById(R.id.iv_bluetooth_icon);
                    imageView.setVisibility(View.INVISIBLE);
                }
                else {
                    // for other views
                    view.setBackgroundColor(Color.WHITE);

                }
                return view;

            }
        };
    }
    public void updatePairedDevices(){
        BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        Set<BluetoothDevice> pairedDevices = mBluetoothAdapter.getBondedDevices();
        pairedName.clear();
        pairedAddress.clear();
        for(BluetoothDevice bt : pairedDevices){
            if(!mCurrentSelectedName.equals(bt.getName())){
                pairedName.add(bt.getName());
                pairedAddress.add(bt.getAddress());
            }
        }

    }
    public void setWhenDeconfigure(){
        mCurrentSelectedName = " ";
        mCurrentSelectedDevice = " ";
    }

    public ArrayList<String> getPairedName(){
        return pairedName;
    }

    public ArrayList<String> getPairedAddress(){
        return pairedAddress;
    }

    public boolean checkPairedDevices(){
        BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        Set<BluetoothDevice> pairedDevices = mBluetoothAdapter.getBondedDevices();

        if(pairedDevices.size() > 0){
            return true;
        }
        else{
            return false;
        }


    }


    public boolean checkBluetoothOn(){
        BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (mBluetoothAdapter != null) {
            if (!mBluetoothAdapter.isEnabled()) {
                return false;
            }
            else{
                return true;
            }
        } else {
            return false;
        }

    }

    public void openBluetooth(Context context){
        Intent intent = new Intent();
        intent.setAction(Settings.ACTION_BLUETOOTH_SETTINGS);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
    }

    public void enableBluetooth(){
        BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (mBluetoothAdapter != null) {
            if (!mBluetoothAdapter.isEnabled()) {
                mBluetoothAdapter.enable();
            }
        }
    }

    private String checkBluetoothStatus(){
        Log.d(TAG,"checkBluetoothStatus " + mCurrentSelectedDevice);
        return mCurrentSelectedName+"\n"+mCurrentSelectedDevice;
    }
}