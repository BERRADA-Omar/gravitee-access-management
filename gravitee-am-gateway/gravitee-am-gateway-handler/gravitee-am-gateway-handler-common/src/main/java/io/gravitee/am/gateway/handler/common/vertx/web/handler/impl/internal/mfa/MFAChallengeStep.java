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
package io.gravitee.am.gateway.handler.common.vertx.web.handler.impl.internal.mfa;

import io.gravitee.am.gateway.handler.common.factor.FactorManager;
import io.gravitee.am.gateway.handler.common.ruleengine.RuleEngine;
import io.gravitee.am.common.utils.ConstantKeys;
import io.gravitee.am.gateway.handler.common.vertx.web.auth.user.User;
import io.gravitee.am.gateway.handler.common.vertx.web.handler.impl.internal.AuthenticationFlowChain;
import io.gravitee.am.gateway.handler.common.vertx.web.handler.impl.internal.mfa.chain.MfaFilterChain;
import io.gravitee.am.gateway.handler.common.vertx.web.handler.impl.internal.mfa.filter.*;
import io.gravitee.am.model.oidc.Client;
import io.vertx.core.Handler;
import io.vertx.reactivex.ext.web.RoutingContext;
import io.vertx.reactivex.ext.web.Session;

/**
 * @author Titouan COMPIEGNE (titouan.compiegne at graviteesource.com)
 * @author Rémi SULTAN (remi.sultan at graviteesource.com)
 * @author GraviteeSource Team
 */
public class MFAChallengeStep extends MFAStep {

    private final FactorManager factorManager;

    public MFAChallengeStep(Handler<RoutingContext> wrapper, RuleEngine ruleEngine, FactorManager factorManager) {
        super(wrapper, ruleEngine);
        this.factorManager = factorManager;
    }

    @Override
    public void execute(RoutingContext routingContext, AuthenticationFlowChain flow) {
        final Client client = routingContext.get(ConstantKeys.CLIENT_CONTEXT_KEY);
        final Session session = routingContext.session();
        final io.gravitee.am.model.User endUser = ((User) routingContext.user().getDelegate()).getUser();
        var context = new MfaFilterContext(client, session, endUser);

        // Rules that makes you skip MFA challenge
        var mfaFilterChain = new MfaFilterChain(
                new ClientNullFilter(client),
                new NoFactorFilter(client.getFactors(), factorManager),
                new MfaChallengeCompleteFilter(context),
                new AdaptiveMfaFilter(context, ruleEngine, routingContext.request(), routingContext.data()),
                new StepUpAuthenticationFilter(context, ruleEngine, routingContext.request(), routingContext.data()),
                new RememberDeviceFilter(context),
                new MfaSkipUserStronglyAuthFilter(context)
        );
        mfaFilterChain.doFilter(this, flow, routingContext);
    }
}
