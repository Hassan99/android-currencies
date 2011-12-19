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
package fi.kinetik.android.currencies.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import fi.kinetik.android.currencies.service.CurrencySyncService;

/**
 * Broadcast receiver that runs sync service once. This receiver should be 
 * mapped to receive:
 * <ul>
 * <li>android.intent.action.BOOT_COMPLETED</li>
 * <li>external apps available</li>
 * </ul>
 * events.
 * 
 * @author Jukka Raanamo <jukka.raanamo@kinetik.fi>
 */
public class BootstrapReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
	
	context.startService(new Intent(CurrencySyncService.SYNC_ACTION));
	
    }
    
    
    
}

