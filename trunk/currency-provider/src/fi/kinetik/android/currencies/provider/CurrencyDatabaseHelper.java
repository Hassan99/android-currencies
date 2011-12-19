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

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.BaseColumns;
import static fi.kinetik.android.currencies.provider.CurrencyContract.ConversionRateColumns.*;

/**
 *
 * @author jsr
 */
class CurrencyDatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "currencies.db";

    private static final int VER_LAUNCH = 2;

    private static final int DATABASE_VERSION = VER_LAUNCH;

    public interface Tables {

	String CURRENCY_RATE = "currency_rate";

    }


    public CurrencyDatabaseHelper(Context context) {
	super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

	// no upgrade, allways drop & create
	db.execSQL("DROP TABLE IF EXISTS " + Tables.CURRENCY_RATE + ";");
	onCreate(db);

    }

    @Override
    public void onCreate(SQLiteDatabase db) {

	db.execSQL("CREATE TABLE " + Tables.CURRENCY_RATE + "("
		+ BaseColumns._ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
		+ RATE_CURRENCY + " TEXT NOT NULL,"
		+ RATE_PROVIDER + " TEXT NOT NULL,"
		+ RATE_UPDATED + " INTEGER NOT NULL,"
		+ RATE_VALUE + " REAL NOT NULL,"
		+ "UNIQUE (" + RATE_CURRENCY + ") ON CONFLICT REPLACE);");

    }

}

