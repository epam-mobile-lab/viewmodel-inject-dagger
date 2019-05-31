/*
 * Copyright 2019 EPAM Systems, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.epam.viewmodelinjectionref

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity

import com.epam.viewmodelinjectionref.R

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val fragmentManager = supportFragmentManager
        if (fragmentManager.findFragmentByTag(FRAGMENT_SAMPLE) == null) {
            fragmentManager.beginTransaction()
                .add(SampleFragment(), FRAGMENT_SAMPLE)
                .commit()
        }
    }

    private companion object {

        private const val FRAGMENT_SAMPLE = "sample"
    }
}
