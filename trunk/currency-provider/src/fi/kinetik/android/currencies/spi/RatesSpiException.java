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

/**
 *
 * @author jsr
 */
public class RatesSpiException extends Exception {

    public RatesSpiException(String msg) {
	super(msg);
    }

    public RatesSpiException(String msg, Throwable cause) {
	super(msg, cause);
    }
    
    
    
    
}

