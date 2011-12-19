/*
 * Copyright 2011 Kinetik Oy http://www.kinetik.fi
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package fi.kinetik.android.currencies.service;

import android.app.IntentService;
import android.content.ContentProviderOperation;
import android.content.ContentResolver;
import android.content.Intent;
import android.os.Bundle;
import android.os.ResultReceiver;
import android.util.Log;
import fi.kinetik.android.currencies.spi.RatesSpi;
import fi.kinetik.android.currencies.spi.RatesSpiFactory;
import fi.kinetik.android.currencies.provider.CurrencyContract;
import fi.kinetik.android.currencies.provider.CurrencyContract.ConversionRate;
import java.util.ArrayList;

/**
 * Synchronizes currency conversion rates from remote site into local database.
 * 
 * @author jsr
 */
public class CurrencySyncService extends IntentService {

    /**
     * Intent extra that holds instance of ResultReceiver for receiving 
     * service events.
     */
    public static final String EXTRA_RESULT_RECEIVER = "_resultReceiver";

    /**
     * Bundle key for error message. Bundle is passed to ResultReceiver with
     * result code RESULT_CODE_ERROR.
     */
    public static final String EXTRA_ERROR_MESSAGE = "_errorMessage";

    /**
     * Event code sent to ResultReceiver when service starts processing intent.
     */
    public static final int RESULT_CODE_RUNNING = 100;

    /**
     * Event code sent to ResultReceiver when service completes processing 
     * without errors.
     */
    public static final int RESULT_CODE_FINISHED = 200;

    /**
     * Event code sent to ResultReceiver when service runs into an error.
     * Bundle contains error message.
     * 
     * @see #EXTRA_ERROR_MESSAGE
     */
    public static final int RESULT_CODE_ERROR = 300;

    /**
     * Event code sent to ResultReceiver when SPI in not available (throws 
     * an error while initialization) and rates cannot be updated.
     */
    public static final int RESULT_CODE_UNAVAILABLE = 301;

    /**
     * Intent action to start synchronizing conversion rates.
     */
    public static final String SYNC_ACTION =
	    "fi.kinetik.currencies.intent.action.SYNC_CURRENCIES";

    private static final String NAME = "ConversionSyncService";

    private static final String TAG = NAME;

    /**
     * A reused batch of update operations.
     */
    private final ArrayList<ContentProviderOperation> mOperations =
	    new ArrayList<ContentProviderOperation>();

    private RatesSpiFactory mFactory;

    private ResultReceiver mReceiver;

    private ContentResolver mResolver;

    public CurrencySyncService() {
	super(NAME);
    }

    @Override
    public void onCreate() {

	super.onCreate();

	try {
	    mFactory = RatesSpiFactory.getInstance(this);
	} catch (Exception e) {
	    Log.e(TAG, "failed to create currency conversion data factory, "
		    + "data updates not available", e);
	    mFactory = null;
	    return;
	}

	mResolver = getContentResolver();
    }

    @Override
    protected void onHandleIntent(Intent intent) {

	init(intent);

	sendEvent(RESULT_CODE_RUNNING);

	if (mFactory == null) {
	    sendEvent(RESULT_CODE_UNAVAILABLE);
	    return;
	}

	mOperations.clear();

	RatesSpi spi = mFactory.newSpi();

	try {

	    long startTime = System.currentTimeMillis();
	    
	    // retrieve new data
	    spi.loadData(mOperations);
	    
	    Log.d(TAG, "data loaded in ms" + (System.currentTimeMillis() - startTime));

	    // if we have any new rates, insert operation first to delete all
	    // previous values
	    if (!mOperations.isEmpty()) {
		mOperations.add(0, ConversionRate.newDeleteOperation());
	    }

	    // add base currency should one not exists yet.
	    mOperations.add(ConversionRate.newUpdateOperation(
		    mFactory.getBaseCurrency(),
		    spi.getProviderName(),
		    System.currentTimeMillis(),
		    1.0));
	    
	    startTime = System.currentTimeMillis();

	    mResolver.applyBatch(CurrencyContract.CONTENT_AUTHORITY,
		    mOperations);
	    
	    Log.d(TAG, "batch applied in ms" + (System.currentTimeMillis() - startTime));

	    sendEvent(RESULT_CODE_FINISHED);

	} catch (Exception e) {

	    Log.e(TAG, "synching rates failed", e);
	    sendEvent(RESULT_CODE_ERROR, e);

	}

    }

    /**
     * Initialize per intent data.
     * 
     * @param intent
     */
    private void init(Intent intent) {

	mReceiver = intent.getParcelableExtra(EXTRA_RESULT_RECEIVER);

    }

    private void sendEvent(int eventId) {

	sendEvent(eventId, Bundle.EMPTY);

    }

    private void sendEvent(int eventId, Exception e) {

	final Bundle args = new Bundle(1);
	args.putString(EXTRA_ERROR_MESSAGE, e.getMessage());
	sendEvent(eventId, args);

    }

    private void sendEvent(int eventId, Bundle args) {

	if (mReceiver != null) {
	    mReceiver.send(eventId, args);
	} else {
	    if (Log.isLoggable(TAG, Log.INFO)) {
		Log.i(TAG, "event: " + eventId + ", args: " + args);
	    }
	}

    }

}

