/**
 * Copyright (C) 2015 The Gravitee team (http://gravitee.io)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.gravitee.am.plugins.reporter.core;

import io.gravitee.am.common.utils.GraviteeContext;
import io.gravitee.am.model.Reporter;
import io.gravitee.am.plugins.handlers.api.provider.ProviderConfiguration;

/**
 * @author Rémi SULTAN (remi.sultan at graviteesource.com)
 * @author GraviteeSource Team
 */
public class ReporterProviderConfiguration extends ProviderConfiguration {

    private final GraviteeContext graviteeContext;

    public ReporterProviderConfiguration(String type, String configuration) {
        super(type, configuration);
        this.graviteeContext = null;
    }

    public ReporterProviderConfiguration(Reporter reporter, GraviteeContext graviteeContext) {
        super(reporter.getType(), reporter.getConfiguration());
        this.graviteeContext = graviteeContext;
    }

    public GraviteeContext getGraviteeContext() {
        return graviteeContext;
    }
}
