package muxi.sample_bluetooth.utils;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class FormatUtils {


   public static String getCurrentDate(){
        SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yy",Locale.getDefault());
        return sdf.format(Calendar.getInstance(Locale.getDefault()).getTime());
    }

    public static String getCurrentTime(boolean format24) {

        String dateFor = "HH:mm";
        if (!format24){
            dateFor += " a";
        }

        SimpleDateFormat sdf = new SimpleDateFormat(dateFor,Locale.getDefault());
        Calendar now = Calendar.getInstance(Locale.getDefault());
        return sdf.format(now.getTime());
    }

    public static String getValueReplaced(String value) {

        value = value.replace("R","");
        value = value.replace("$","");
        value = value.replace(",","");
        // Para valores maiores que R$ 999,99
        value = value.replace(".","");
        return value;
    }
}
