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

import java.util.ArrayList;
import android.content.ContentProviderOperation;
import java.io.IOException;

/**
 * Contract for loading conversion rates from remote source.
 * 
 * @author Jukka Raanamo <jukka.raanamo@kinetik.fi>
 */
public interface RatesSpi {
    
    /**
     * Retrieve conversion rate updates and add them as update operations using
     * factory method from CurrencyRate.
     * 
     * @see CurrencyRateContract
     * @param operations 
     * @throws IOException
     * @throws ConversionDataSpiException  
     */
    public void loadData(ArrayList<ContentProviderOperation> operations)
	    throws IOException, RatesSpiException;
    
    
    
    /**
     * Return symbolic name for this provider.
     * 
     * @return
     */
    public String getProviderName();
    
}

