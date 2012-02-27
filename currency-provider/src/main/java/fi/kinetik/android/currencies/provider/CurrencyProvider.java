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
package fi.kinetik.android.currencies.provider;

import android.content.ContentProvider;
import android.content.ContentProviderOperation;
import android.content.ContentProviderResult;
import android.content.ContentValues;
import android.content.Context;
import android.content.OperationApplicationException;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.net.Uri;
import android.provider.BaseColumns;
import fi.kinetik.android.currencies.provider.CurrencyContract.ConversionRate;
import fi.kinetik.android.currencies.provider.CurrencyContract.ConversionRateColumns;
import fi.kinetik.android.currencies.provider.CurrencyDatabaseHelper.Tables;
import fi.kinetik.android.currencies.spi.RatesSpiFactory;
import fi.kinetik.android.currencies.util.SelectionBuilder;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author jsr
 */
public class CurrencyProvider extends ContentProvider {

    private static final String TAG = "CurrencyRateProvider";

    private SQLiteOpenHelper mDbHelper;

    private static final int RATES = 100;

    private static final int RATES_ID = 101;

    private static final int RATES_CONVERSION = 102;

    private static final UriMatcher sUriMatcher = buildUriMatcher();

    interface Query {

	/**
	 * A quite inefficient query that performs the math of:
	 * 
	 * ((input-currency / base-currency) / (output-currency / base-currency) * amount)
	 * 
	 */
	String CONVERSION_SQL =
		"select ((" + ConversionRateColumns.RATE_VALUE
		+ " / (select " + ConversionRateColumns.RATE_VALUE
		+ " from " + Tables.CURRENCY_RATE + " where "
		+ ConversionRateColumns.RATE_CURRENCY + "=?))" // input currency
		+ "/ (" + ConversionRateColumns.RATE_VALUE + " / (select "
		+ ConversionRateColumns.RATE_VALUE
		+ " from " + Tables.CURRENCY_RATE + " where "
		+ ConversionRateColumns.RATE_CURRENCY + "=?)) * ?)" // output currency, amount
		+ " from " + Tables.CURRENCY_RATE
		+ " where rate_currency =?"; // base currency

    }


    @Override
    public boolean onCreate() {

	final Context context = getContext();
	mDbHelper = createDatabaseHelper(context);
	return true;

    }

    @Override
    public int update(Uri uri, ContentValues values, String selection,
	    String[] selectionArgs) {

	final SQLiteDatabase db = mDbHelper.getWritableDatabase();
	final SelectionBuilder builder = buildSimpleSelection(uri);

	final int retValue = builder.where(selection, selectionArgs).update(db,
		values);

	getContext().getContentResolver().notifyChange(uri, null);
	return retValue;

    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {

	final SQLiteDatabase db = mDbHelper.getWritableDatabase();
	final SelectionBuilder builder = buildSimpleSelection(uri);
	int retVal = builder.where(selection, selectionArgs).delete(db);
	getContext().getContentResolver().notifyChange(uri, null);
	return retVal;

    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {

	final int match = sUriMatcher.match(uri);
	switch (match) {
	    case RATES:
		final SQLiteDatabase db = mDbHelper.getWritableDatabase();
		db.insertOrThrow(Tables.CURRENCY_RATE, null, values);
		getContext().getContentResolver().notifyChange(uri, null);
		final String currency =
			values.getAsString(
			ConversionRateColumns.RATE_CURRENCY);
		return ConversionRate.buildRateUri(currency);
	    default:
		throw new IllegalArgumentException("unknown uri: " + uri);
	}
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection,
	    String[] selectionArgs, String sortOrder) {

	switch (sUriMatcher.match(uri)) {

	    case RATES_CONVERSION: {

		final List<String> segments = uri.getPathSegments();
		final String fromCurrency = segments.get(1);
		final String toCurrency = segments.get(2);
		final String amount = segments.get(3);

		final SQLiteDatabase db = mDbHelper.getReadableDatabase();
		return db.rawQuery(Query.CONVERSION_SQL, new String[]{
			    fromCurrency,
			    toCurrency,
			    amount,
			    getBaseCurrency()});

	    }

	    case RATES: {
		final SQLiteDatabase db = mDbHelper.getReadableDatabase();
		return buildSimpleSelection(ConversionRate.CONTENT_URI).
			query(db, projection, sortOrder);

	    }

	    case RATES_ID: {
		final List<String> segments = uri.getPathSegments();
		final String currency = segments.get(1).toUpperCase();
		final SQLiteDatabase db = mDbHelper.getReadableDatabase();
		return buildSimpleSelection(ConversionRate.CONTENT_URI).
			whereEq(ConversionRateColumns.RATE_CURRENCY, currency).
			query(db, projection, null);
	    }

	    default:
		throw new IllegalArgumentException("unsupported uri: " + uri);
	}


    }
    
    
    @Override
    public ContentProviderResult[] applyBatch(ArrayList<ContentProviderOperation> operations)
            throws OperationApplicationException {
        final SQLiteDatabase db = mDbHelper.getWritableDatabase();
        db.beginTransaction();
        try {
            final int numOperations = operations.size();
            final ContentProviderResult[] results = new ContentProviderResult[numOperations];
            for (int i = 0; i < numOperations; i++) {
                results[i] = operations.get(i).apply(this, results, i);
            }
            db.setTransactionSuccessful();
            return results;
        } finally {
            db.endTransaction();
        }
    }


    private String getBaseCurrency() {
	// TODO
	try {
	    return RatesSpiFactory.getInstance(getContext()).getBaseCurrency();
	} catch (Exception e) {
	    throw new IllegalStateException(e);
	}
    }

    @Override
    public String getType(Uri uri) {

	switch (sUriMatcher.match(uri)) {
	    case RATES:
		return ConversionRate.CONTENT_TYPE;
	    case RATES_ID:
	    case RATES_CONVERSION:
		return ConversionRate.CONTENT_ITEM_TYPE;
	    default:
		throw new IllegalArgumentException("unknown uri: " + uri);
	}

    }

    private SelectionBuilder buildSimpleSelection(Uri uri) {

	final SelectionBuilder builder = new SelectionBuilder();
	final int match = sUriMatcher.match(uri);

	switch (match) {
	    case RATES:
		return builder.table(Tables.CURRENCY_RATE);
	    case RATES_ID: {
		final List<String> segments = uri.getPathSegments();
		return builder.table(Tables.CURRENCY_RATE).whereEq(
			BaseColumns._ID, segments.get(1));
	    }
	    default:
		throw new IllegalArgumentException("unsupported uri: " + uri);

	}

    }

    private SQLiteOpenHelper createDatabaseHelper(Context context) {

	return new CurrencyDatabaseHelper(context);

    }

    private static UriMatcher buildUriMatcher() {

	final UriMatcher uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);

	uriMatcher.addURI(CurrencyContract.CONTENT_AUTHORITY,
		"rates", RATES);
	uriMatcher.addURI(CurrencyContract.CONTENT_AUTHORITY,
		"rates/*", RATES_ID);
	uriMatcher.addURI(CurrencyContract.CONTENT_AUTHORITY,
		"rates/*/*/*", RATES_CONVERSION);

	return uriMatcher;


    }

}

