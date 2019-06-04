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
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProviders
import com.epam.inject.viewmodel.AssistedViewModelFactory

import javax.inject.Inject

class MainActivity : AppCompatActivity() {

    private lateinit var viewModel: SampleViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        (this.applicationContext as MainApplication).applicationComponent.inject(this)
        setContentView(R.layout.sample_activity)

        findViewById<TextView>(R.id.repoName).text = viewModel.repository.name
    }


    @Inject
    internal fun initViewModel(viewModelFactory: AssistedViewModelFactory) {
        viewModel = ViewModelProviders.of(this, viewModelFactory).get(SampleViewModel::class.java)
    }
}
