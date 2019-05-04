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

package com.epam.viewmodelinjectionref.di;

import com.epam.viewmodelinjectionref.ViewModelInjectModule;
import com.epam.viewmodelinjectionref.di.module.ModelModule;
import com.epam.viewmodelinjectionref.ui.database.DaoFragment;
import com.epam.viewmodelinjectionref.ui.network.NetworkFragment;
import com.epam.viewmodelinjectionref.ui.repository.RepositoryFragment;

import dagger.Component;

@Component(modules = {ModelModule.class, ViewModelInjectModule.class})
public interface ApplicationComponent {

    void inject(DaoFragment daoFragment);

    void inject(RepositoryFragment repositoryFragment);

    void inject(NetworkFragment networkFragment);
}
