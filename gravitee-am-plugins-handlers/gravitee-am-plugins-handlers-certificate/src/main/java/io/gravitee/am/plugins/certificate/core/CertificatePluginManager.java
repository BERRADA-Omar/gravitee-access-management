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

package io.gravitee.am.plugins.certificate.core;

import io.gravitee.am.certificate.api.Certificate;
import io.gravitee.am.certificate.api.CertificateConfiguration;
import io.gravitee.am.certificate.api.CertificateMetadata;
import io.gravitee.am.certificate.api.CertificateProvider;
import io.gravitee.am.plugins.handlers.api.core.AMPluginManager;
import io.gravitee.am.plugins.handlers.api.core.NamedBeanFactoryPostProcessor;
import io.gravitee.am.plugins.handlers.api.core.ProviderPluginManager;
import io.gravitee.plugin.core.api.PluginContextFactory;

/**
 * @author Rémi SULTAN (remi.sultan at graviteesource.com)
 * @author GraviteeSource Team
 */
public abstract class CertificatePluginManager
        extends ProviderPluginManager<Certificate, CertificateProvider, CertificateProviderConfiguration>
        implements AMPluginManager<Certificate> {

    protected CertificatePluginManager(PluginContextFactory pluginContextFactory) {
        super(pluginContextFactory);
    }

    protected static class CertificateMetadataBeanFactoryPostProcessor extends NamedBeanFactoryPostProcessor<CertificateMetadata> {

        public CertificateMetadataBeanFactoryPostProcessor(CertificateMetadata metadata) {
            super("metadata", metadata);
        }
    }

    protected static class CertificateConfigurationBeanFactoryPostProcessor extends NamedBeanFactoryPostProcessor<CertificateConfiguration> {

        public CertificateConfigurationBeanFactoryPostProcessor(CertificateConfiguration configuration) {
            super("configuration", configuration);
        }

    }
}
