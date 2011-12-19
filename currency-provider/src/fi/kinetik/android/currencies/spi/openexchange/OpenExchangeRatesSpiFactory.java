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

import android.content.Context;
import fi.kinetik.android.currencies.spi.RatesSpi;
import fi.kinetik.android.currencies.spi.RatesSpiException;
import fi.kinetik.android.currencies.spi.RatesSpiFactory;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Properties;

/**
 * SPI implementation that loads conversion rates from OpenExhangeRates JSON
 * feed. Make sure you read from where and how this data is collected from:
 * 
 * http://josscrowcroft.github.com/open-exchange-rates/
 * 
 * 
 * @author Jukka Raanamo <jukka.raanamo@kinetik.fi>
 */
public class OpenExchangeRatesSpiFactory extends RatesSpiFactory {

    /* package */ static URL sFeedURL;

    public static final String PROVIDER_NAME = "OpenExchangeRates";

    /*
     *
     */
    private static final String PROPERTY_PREFIX = "openexchangerates.";

    /**
     * URL to the JSON feed of currency conversion rates.
     */
    public static final String PROPERTY_FEED_URL = PROPERTY_PREFIX + "feedURL";

    /**
     * Default url to the conversion feed.
     */
    public static final String DEFAULT_FEED_URL =
	    "http://openexchangerates.org/latest.json";

    /**
     * Base currency for all conversion rates provided by this service.  .
     */
    public static final String BASE_CURRENCY = "USD";

    /**
     * Singleton of SPI impl. Same instance is always returned.
     */
    private static final OpenExchangeRatesSpi sSpi = new OpenExchangeRatesSpi();

    /**
     * Charset of the feed.
     */
    public static final String CHARSET = "UTF-8";

    @Override
    protected void onCreate(Context context, Properties props) throws
	    RatesSpiException {

	String value = null;

	try {
	    value = props.getProperty(PROPERTY_FEED_URL, DEFAULT_FEED_URL);
	    sFeedURL = new URL(value);
	} catch (MalformedURLException e) {
	    throw new RatesSpiException("bad feed url: " + value, e);
	}

    }

    @Override
    public RatesSpi newSpi() {
	return sSpi;
    }

    @Override
    public String getBaseCurrency() {
	return BASE_CURRENCY;
    }

}

