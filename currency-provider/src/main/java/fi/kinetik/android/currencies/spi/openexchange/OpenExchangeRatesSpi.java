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
package fi.kinetik.android.currencies.spi.openexchange;

import android.content.ContentProviderOperation;
import fi.kinetik.android.currencies.spi.RatesSpi;
import fi.kinetik.android.currencies.spi.RatesSpiException;
import fi.kinetik.android.currencies.provider.CurrencyContract.ConversionRate;
import fi.kinetik.android.currencies.util.IOUtils;
import java.io.IOException;
import java.io.InputStream;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Iterator;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * 
 * Expected JSON format:
 * <code> 
 * {
 *	"disclaimer": "disclaimer text",
 *	"license": "copyright text",
 *	"timestamp": 1324116096,
 *	"base": "USD",
 *	"rates": {
 *		"AED": 3.673,
 *		"AFN": 42.950001
 *	}
 * }
 * </code>
 *
 * @author jsr
 */
public class OpenExchangeRatesSpi implements RatesSpi {

    private static final String TAG = "RatesSpi";
    

    /* package */ OpenExchangeRatesSpi() {
    }

    public void loadData(ArrayList<ContentProviderOperation> operations)
	    throws IOException, RatesSpiException {

	final URLConnection connection = OpenExchangeRatesSpiFactory.sFeedURL.
		openConnection();

	InputStream in = null;

	try {

	    in = connection.getInputStream();

	    // not efficient to read the entire feed into memory but since its
	    // not that big...

	    JSONObject json = new JSONObject(IOUtils.toString(in,
		    OpenExchangeRatesSpiFactory.CHARSET));
	    
	    parseRates(operations, json);

	} catch (JSONException e) {
	    throw new RatesSpiException("parsing json failed", e);
	} finally {
	    IOUtils.close(in);
	}
    }

    private void parseRates(ArrayList<ContentProviderOperation> operations,
	    JSONObject jsonObj) throws JSONException {

	final long updated = jsonObj.getLong(Keys.TIMESTAMP);
	final String base = jsonObj.getString(Keys.BASE);
	final JSONObject rates = jsonObj.getJSONObject(Keys.RATES);
	final Iterator iter = rates.keys();

	while (iter.hasNext()) {

	    final String currency = (String) iter.next();
	    final double rate = rates.getDouble(currency);

	    operations.add(ConversionRate.newUpdateOperation(
		    currency,
		    OpenExchangeRatesSpiFactory.PROVIDER_NAME,
		    updated,
		    rate));
	}

    }

    public String getProviderName() {
	return OpenExchangeRatesSpiFactory.PROVIDER_NAME;
    }

    interface Keys {

	String TIMESTAMP = "timestamp";

	String BASE = "base";

	String RATES = "rates";

    }


}

