package muxi.sample_bluetooth;

import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;


public class MaskWatcher  {
    private  static  boolean beforeChanged = true;
    private static int repeat = 1;
    public static TextWatcher mask(final EditText ediTxt) {
        return new TextWatcher() {

            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                int selection = ediTxt.getSelectionEnd();
                if(selection != ediTxt.getText().length() && beforeChanged){
                    beforeChanged = false;
                    ediTxt.setText(charSequence);
                }
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                if(beforeChanged){
                    if (!charSequence.toString().matches(AppConstants.REGEX_MATCHES)) {
                        String userInput = "" + charSequence.toString().replaceAll(AppConstants.REGEX_REPLACE_ALL, "");
                        StringBuilder cashAmountBuilder = new StringBuilder(userInput);

                        while (cashAmountBuilder.length() > AppConstants.SET_MASK_CURRENCY_HELPER
                                && cashAmountBuilder.charAt(0) == '0') {
                            cashAmountBuilder.deleteCharAt(0);
                        }
                        while (cashAmountBuilder.length() < AppConstants.SET_MASK_CURRENCY_HELPER) {
                            cashAmountBuilder.insert(0, '0');
                        }
                        cashAmountBuilder.insert(cashAmountBuilder.length() - 2, '.');
                        ediTxt.setText(cashAmountBuilder);
                        ediTxt.setSelection(ediTxt.getText().length());


                    }
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {
                if(!beforeChanged && repeat == 1){
                    repeat += 1;
                    ediTxt.setSelection(ediTxt.getText().length());
                }
                else{
                    repeat = 1;
                    beforeChanged = true;
                }
            }
        };
    }



}

