package muxi.sample_bluetooth;

 public interface AppConstants {

    boolean DEFAULT_USE_PP = true;

    int RADIO_GROUP_CREDIT = 0;
    int RADIO_GROUP_DEBIT = 1;
    int RADIO_GROUP_VOUCHER= 2;

    int SET_MASK_CURRENCY_HELPER = 3 ;
    String REGEX_MATCHES = "^(\\d+\\.\\d{2})?$";
    String REGEX_REPLACE_ALL = "[^\\d]";

    enum TransactionState {
       payment,
       cancel
    }

    int RECEIPT_MARGIN_LEFT = 60;
    int RECEIPT_MARGIN_TOP = 15;
    int RECEIPT_MARGIN_RIGHT = 0;
    int RECEIPT_MARGIN_BOTTOM = 0;
}