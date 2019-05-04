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

package com.epam.viewmodelinjectionref.ui.network;

import androidx.lifecycle.ViewModelProviders;
import android.content.Context;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.epam.viewmodelinjectionref.GeneratedViewModelFactory;
import com.epam.viewmodelinjectionref.MainApplication;
import com.epam.viewmodelinjectionref.R;

import javax.inject.Inject;

public class NetworkFragment extends Fragment {

        private NetworkViewModel mViewModel;

        @Override
        public void onAttach(Context context) {
            super.onAttach(context);
            ((MainApplication)context.getApplicationContext()).getApplicationComponent().inject(this);
        }

        @Inject
        void initViewModel(GeneratedViewModelFactory viewModelFactory) {
            mViewModel = ViewModelProviders.of(this, viewModelFactory).get(NetworkViewModel.class);
        }

        @Nullable
        @Override
        public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
            View view = inflater.inflate(R.layout.network_fragment, container, false);
            TextView textView = view.findViewById(R.id.network_textView);
            textView.setText(mViewModel.getService().getName());
            return view;
        }
    }
