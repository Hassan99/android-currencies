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
package fi.kinetik.android.currencies.util;

import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;

/**
 *
 * @author jsr
 */
public class IOUtils {
    
    
    public static void close(Closeable in) {

	if (in != null) {
	    try {
		in.close();
	    } catch (Exception ignore) {
		//
	    }
	}

    }

    public static String toString(InputStream in, String charset) throws IOException {

	ByteArrayOutputStream out = new ByteArrayOutputStream();
	
	byte[] buffer = new byte[1024];
	int n = 0;
	while (-1 != (n = in.read(buffer))) {
	    out.write(buffer, 0, n);	
	}
	return out.toString(charset);

    }

}

