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
package fi.kinetik.android.currencies.spi;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;
import fi.kinetik.android.currencies.util.IOUtils;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * <p>Abstract factory that loads RatesSpiFactory implementation based on property
 * file under assets or using defaults if one does not exist.</p>
 * 
 * <p>To configure your own SPI:
 * <ul>
 * <li>add file currency_spi.properties under /assets
 * <li>add property rates_spi.factory=class-name
 * </ul>
 * where class-name is the classname of your RatesSpiFactory implementation. 
 * This property file is passed to your implementation in onCreate -method so 
 * you may add other implementation specific properties into same file.
 * </p>
 * 
 * @author Jukka Raanamo <jukka.raanamo@kinetik.fi>
 */
public abstract class RatesSpiFactory {

    private static final String TAG = "RateSpiFactory";

    /**
     * Property name that declares implementation of SPI factory. 
     */
    private static final String PROPERTY_SPI_FACTORY_CLASS_NAME =
	    "rates_spi.factory";

    /**
     * Default SPI factory implementation.
     */
    public static final String DEFAULT_SPI_FACTORY_CLASS_NAME =
	    "fi.kinetik.android.currencies.spi.openexchange.OpenExchangeRatesSpiFactory";

    /**
     * Name of the property file that is loaded from assets directory. Add
     * this file with property 
     */
    public static final String PROPERTY_FILENAME =
	    "currency_spi.properties";

    /**
     * Singleton factory implementation.
     */
    private static RatesSpiFactory sFactory;

    /**
     * Lazy loads and returns singleton instance of factory implementation.
     * 
     * @param context
     * @return
     * @throws IOException
     * @throws RatesSpiException
     */
    public static synchronized RatesSpiFactory getInstance(
	    Context context) throws IOException, RatesSpiException {

	if (sFactory == null) {
	    sFactory = createFactory(context);
	}

	return sFactory;

    }

    /**
     * Creates an initializes factory impl.
     * 
     * @param context
     * @return
     * @throws IOException
     * @throws ConversionDataSpiException
     */
    private static RatesSpiFactory createFactory(Context context)
	    throws IOException, RatesSpiException {

	InputStream in = null;
	final Properties props = new Properties();

	try {

	    in = context.getResources().getAssets().open(PROPERTY_FILENAME);
	    props.load(in);

	} catch (FileNotFoundException e) {

	    Log.w(TAG, "no " + PROPERTY_FILENAME
		    + " defined in assets, using default spi impl: "
		    + DEFAULT_SPI_FACTORY_CLASS_NAME);

	    props.setProperty(PROPERTY_SPI_FACTORY_CLASS_NAME,
		    DEFAULT_SPI_FACTORY_CLASS_NAME);

	} finally {
	    IOUtils.close(in);
	}

	final RatesSpiFactory factory = createFactory(props);
	factory.onCreate(context, props);
	return factory;

    }

    /**
     * Instantiates factory implementation by classname.
     * 
     * @param props
     * @return
     * @throws RatesSpiException
     */
    private static RatesSpiFactory createFactory(Properties props)
	    throws RatesSpiException {

	final String cls = props.getProperty(PROPERTY_SPI_FACTORY_CLASS_NAME);
	
	if (TextUtils.isEmpty(cls)) {
	    throw new IllegalStateException(
		    PROPERTY_SPI_FACTORY_CLASS_NAME + " property not set");
	}

	try {
	    return (RatesSpiFactory) Class.forName(cls).newInstance();
	} catch (Exception e) {
	    throw new RatesSpiException("creating factory failed: " + cls, e);
	}
	
    }

    /**
     * Invoked once after instance of the factory implementation is created.
     * 
     * @param context
     * @param props
     * @throws RatesSpiException
     */
    protected abstract void onCreate(Context context, Properties props)
	    throws RatesSpiException;

    /**
     * Invoked each time when rates should be uploaded. The same instance may 
     * be returned if implementation is thread safe.
     * 
     * @return
     */
    public abstract RatesSpi newSpi();
    
    
    /**
     * Return the currency conversion rates are based on.
     * 
     * @return
     */
    public abstract String getBaseCurrency();

}

