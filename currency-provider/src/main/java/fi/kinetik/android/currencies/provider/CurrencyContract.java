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

import android.content.ContentProviderOperation;
import android.net.Uri;
import android.provider.BaseColumns;

/**
 * Contract for currency models.
 * 
 * @author Jukka Raanamo <jukka.raanamo@kinetik.fi>
 */
public final class CurrencyContract {

    public static final String CONTENT_AUTHORITY =
	    "fi.kinetik.android.currencies";

    public static final Uri AUTHORITY_URI = Uri.parse(
	    "content://" + CONTENT_AUTHORITY);

    interface ConversionRateColumns {

	/**
	 * 3-letter currency code of the conversion rate.
	 */
	String RATE_CURRENCY = "rate_currency";

	/**
	 * Timestamp when this conversion rate was updated.
	 */
	String RATE_UPDATED = "rate_updated";

	/**
	 * Name of the conversion rate provider (the source of data).
	 */
	String RATE_PROVIDER = "rate_provider";

	/**
	 * Currency conversion rate, double.
	 */
	String RATE_VALUE = "rate_value";

    }


    /**
     * Currency conversion rate.
     */
    public static final class ConversionRate implements
	    BaseColumns, ConversionRateColumns {

	private ConversionRate() {
	    // N/A
	}

	/**                                                                                                                                             
	 * The content:// style URI for this table.
	 */
	public static final Uri CONTENT_URI = Uri.withAppendedPath(AUTHORITY_URI,
		"rates");

	/**                                                                                                                                             
	 * The MIME-type of {@link #CONTENT_URI} providing a directory of                                                                               
	 * currency conversions.                                                                                                                         
	 */
	public static final String CONTENT_TYPE =
		"vnd.android.cursor.dir/currency_rates";

	/**                                                                                                                                             
	 * The MIME type of a {@link #CONTENT_URI} item.                                                                                                
	 */
	public static final String CONTENT_ITEM_TYPE =
		"vnd.android.cursor.item/currency_rates";

	/**
	 * Creates an Uri that points to a conversion rate item.
	 * 
	 * @param currency
	 * @return
	 */
	public static Uri buildRateUri(String currency) {

	    return CONTENT_URI.buildUpon().
		    appendPath(currency.toUpperCase()).build();

	}

	/**
	 * Returns Uri that can be passed to ContentResolver to ask for currency
	 * conversion from <code>fromCurrency</code> to <code>toCurrency</code>.
	 * 
	 * @param fromCurrency 3-letter currency code of the origin currency
	 * @param toCurrency 3-letter currency code of the target currency
	 * @param amount amount to convert, double value
	 * @return
	 */
	public static Uri buildConversionUri(String fromCurrency,
		String toCurrency,
		String amount) {

	    return CONTENT_URI.buildUpon().
		    appendPath(fromCurrency.toUpperCase()).
		    appendPath(toCurrency.toUpperCase()).
		    appendPath(amount).build();

	}

	/**
	 * Creates a ContentProviderOperation that can be added to a batch of 
	 * operations that updates or inserts currency conversion rate by
	 * given values.
	 * 
	 * @param currency target currency of conversion
	 * @param provider symbolic name of the provider for this rate
	 * @param updated timestamp when the rate was acquired
	 * @param rate conversion rate
	 * 
	 * @return operation to be added to batch of operations
	 */
	public static ContentProviderOperation newUpdateOperation(
		String currency,
		String provider,
		Long updated,
		double rate) {

	    return ContentProviderOperation.newInsert(CONTENT_URI).withValue(
		    RATE_CURRENCY, currency).
		    withValue(RATE_PROVIDER, provider).
		    withValue(RATE_UPDATED,
		    updated != null ? updated : System.currentTimeMillis()).
		    withValue(RATE_VALUE, rate).
		    build();


	}

	
	/**
	 * Returns operation that will delete all conversion rates.
	 * 
	 * @return
	 */
	public static ContentProviderOperation newDeleteOperation() {
	    
	    return ContentProviderOperation.newDelete(CONTENT_URI).build();
	    
	}
	
    }


}

