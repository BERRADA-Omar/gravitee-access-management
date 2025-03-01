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
package io.gravitee.am.management.service;

import freemarker.template.TemplateException;
import io.gravitee.am.common.email.Email;
import io.gravitee.am.model.Application;
import io.gravitee.am.model.Domain;
import io.gravitee.am.model.Template;
import io.gravitee.am.model.User;

import java.io.IOException;
import java.util.Map;

/**
 * @author Titouan COMPIEGNE (titouan.compiegne at graviteesource.com)
 * @author GraviteeSource Team
 */
public interface EmailService {

    void send(Domain domain, Application client, Template template, User user);

    io.gravitee.am.model.Email getEmailTemplate(Template template, User user);

    default void send(Domain domain, Template template, User user) {
        send(domain, null, template, user);
    }

    Email getFinalEmail(Domain domain, Application client, io.gravitee.am.model.Template template, User user, Map<String, Object> params) throws IOException, TemplateException;

}
