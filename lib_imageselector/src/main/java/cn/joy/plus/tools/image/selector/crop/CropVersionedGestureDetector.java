/*******************************************************************************
 * Copyright  2015 Albin Mathew.
 *  <p/>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p/>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p/>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/

package cn.joy.plus.tools.image.selector.crop;

import android.content.Context;
import android.os.Build;

final class CropVersionedGestureDetector {

   public static GestureDetector newInstance(Context context,
											 OnGestureListener listener) {
	   final int sdkVersion = Build.VERSION.SDK_INT;
	   GestureDetector detector;

	   if (sdkVersion < Build.VERSION_CODES.ECLAIR) {
		   detector = new CropCupcakeGestureDetector(context);
	   } else if (sdkVersion < Build.VERSION_CODES.FROYO) {
		   detector = new CropEclairGestureDetector(context);
	   } else {
		   detector = new CropFroyoGestureDetector(context);
	   }

	   detector.setOnGestureListener(listener);

	   return detector;
   }

}