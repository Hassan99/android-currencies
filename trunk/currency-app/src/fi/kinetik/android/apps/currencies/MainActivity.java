package fi.kinetik.android.apps.currencies;

import android.app.Activity;
import android.content.AsyncQueryHandler;
import android.content.ContentResolver;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.ResultReceiver;
import android.provider.BaseColumns;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import fi.kinetik.android.currencies.provider.CurrencyContract.ConversionRate;
import fi.kinetik.android.currencies.service.CurrencySyncService;

public class MainActivity extends Activity {

    private static final String TAG = "MainActivity";

    private TextView mStatus;

    private EditText mFromCurrency;

    private EditText mToCurrency;

    private EditText mAmount;

    private TextView mResult;

    private Handler mHandler = new Handler();

    private AsyncQueryHandler mQueryHandler;

    private ResultReceiver mReceiver = new ResultReceiver(mHandler) {

	@Override
	protected void onReceiveResult(final int resultCode,
		final Bundle resultData) {

	    if (resultCode == 100) {
		mStatus.setText("started");
	    } else if (resultCode == 200) {
		mStatus.setText("finished");
	    } else {
		String error = resultData.getString("_errorMessage");
		mStatus.setText("error: " + resultCode
			+ ", msg: " + error);
	    }

	}

    };

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {

	super.onCreate(savedInstanceState);
	setContentView(R.layout.main);

	mQueryHandler = new MyAsyncQueryHandler(getContentResolver());

	mStatus = (TextView) findViewById(R.id.status);
	mFromCurrency = (EditText) findViewById(R.id.from_currency);
	mToCurrency = (EditText) findViewById(R.id.to_currency);
	mAmount = (EditText) findViewById(R.id.amount);
	mResult = (TextView) findViewById(R.id.result);


    }

    public void doSyncNow(View view) {

	Intent intent = new Intent(CurrencySyncService.SYNC_ACTION);
	intent.putExtra(CurrencySyncService.EXTRA_RESULT_RECEIVER, mReceiver);
	startService(intent);

    }

    public void doSyncForce(View view) {

	Intent intent = new Intent(CurrencySyncService.SYNC_ACTION);
	intent.putExtra(CurrencySyncService.EXTRA_RESULT_RECEIVER, mReceiver);
	intent.putExtra(CurrencySyncService.EXTRA_FORCE, Boolean.TRUE);
	startService(intent);

    }

    public void doConvert(View view) {

	String fromCurrency = mFromCurrency.getText().toString();
	String toCurrency = mToCurrency.getText().toString();
	String amount = mAmount.getText().toString();

	Uri uri = ConversionRate.buildConversionUri(fromCurrency, toCurrency,
		amount);
	mQueryHandler.startQuery(ConversionQuery.TOKEN,
		null,
		uri,
		new String[]{BaseColumns._ID},
		null,
		null,
		null);

    }

    class MyAsyncQueryHandler extends AsyncQueryHandler {

	public MyAsyncQueryHandler(ContentResolver cr) {
	    super(cr);
	}

	@Override
	protected void onQueryComplete(int token, Object cookie, Cursor cursor) {

	    try {
		if (cursor.moveToFirst()) {
		    final double convertedAmount = cursor.getDouble(0);
		    mResult.setText(String.valueOf(convertedAmount));
		} else {
		    mResult.setText("no such currencies");
		}
	    } finally {
		cursor.close();
	    }

	}

    }


    interface ConversionQuery {

	int TOKEN = 1;

    }


}

