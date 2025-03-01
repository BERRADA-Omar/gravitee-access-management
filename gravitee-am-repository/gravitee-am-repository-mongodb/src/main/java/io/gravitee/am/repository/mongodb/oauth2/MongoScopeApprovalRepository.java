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
package io.gravitee.am.repository.mongodb.oauth2;

import com.mongodb.client.model.IndexOptions;
import com.mongodb.reactivestreams.client.MongoCollection;
import io.gravitee.am.common.utils.RandomString;
import io.gravitee.am.model.oauth2.ScopeApproval;
import io.gravitee.am.repository.mongodb.oauth2.internal.model.ScopeApprovalMongo;
import io.gravitee.am.repository.oauth2.api.ScopeApprovalRepository;
import io.reactivex.*;
import org.bson.Document;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.Date;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import static com.mongodb.client.model.Filters.and;
import static com.mongodb.client.model.Filters.eq;

/**
 * @author David BRASSELY (david.brassely at graviteesource.com)
 * @author Titouan COMPIEGNE (titouan.compiegne at graviteesource.com)
 * @author GraviteeSource Team
 */
@Component
public class MongoScopeApprovalRepository extends AbstractOAuth2MongoRepository implements ScopeApprovalRepository {

    private static final String FIELD_TRANSACTION_ID = "transactionId";
    private static final String FIELD_USER_ID = "userId";
    private static final String FIELD_CLIENT_ID = "clientId";
    private static final String FIELD_EXPIRES_AT = "expiresAt";
    private static final String FIELD_SCOPE = "scope";
    private MongoCollection<ScopeApprovalMongo> scopeApprovalsCollection;

    @PostConstruct
    public void init() {
        scopeApprovalsCollection = mongoOperations.getCollection("scope_approvals", ScopeApprovalMongo.class);
        super.init(scopeApprovalsCollection);
        super.createIndex(scopeApprovalsCollection, new Document(FIELD_TRANSACTION_ID, 1));
        super.createIndex(scopeApprovalsCollection, new Document(FIELD_DOMAIN, 1).append(FIELD_USER_ID, 1));
        super.createIndex(scopeApprovalsCollection, new Document(FIELD_DOMAIN, 1).append(FIELD_CLIENT_ID, 1).append(FIELD_USER_ID, 1));
        super.createIndex(scopeApprovalsCollection, new Document(FIELD_DOMAIN, 1).append(FIELD_CLIENT_ID, 1).append(FIELD_USER_ID, 1).append(FIELD_SCOPE, 1));

        // expire after index
        super.createIndex(scopeApprovalsCollection, new Document(FIELD_EXPIRES_AT, 1),  new IndexOptions().expireAfter(0l, TimeUnit.SECONDS));
    }

    @Override
    public Flowable<ScopeApproval> findByDomainAndUserAndClient(String domain, String userId, String clientId) {
        return Flowable.fromPublisher(scopeApprovalsCollection.find(and(eq(FIELD_DOMAIN, domain), eq(FIELD_CLIENT_ID, clientId), eq(FIELD_USER_ID, userId)))).map(this::convert);
    }

    @Override
    public Flowable<ScopeApproval> findByDomainAndUser(String domain, String user) {
        return Flowable.fromPublisher(scopeApprovalsCollection.find(and(eq(FIELD_DOMAIN, domain), eq(FIELD_USER_ID, user)))).map(this::convert);
    }

    @Override
    public Maybe<ScopeApproval> findById(String id) {
        return Observable.fromPublisher(scopeApprovalsCollection.find(eq(FIELD_ID, id)).first()).firstElement().map(this::convert);
    }

    @Override
    public Single<ScopeApproval> create(ScopeApproval scopeApproval) {
        ScopeApprovalMongo scopeApprovalMongo = convert(scopeApproval);
        scopeApprovalMongo.setId(scopeApprovalMongo.getId() == null ? RandomString.generate() : scopeApprovalMongo.getId());
        return Single.fromPublisher(scopeApprovalsCollection.insertOne(scopeApprovalMongo)).flatMap(success -> Single.just(scopeApproval));
    }

    @Override
    public Single<ScopeApproval> update(ScopeApproval scopeApproval) {
        ScopeApprovalMongo scopeApprovalMongo = convert(scopeApproval);

        return Single.fromPublisher(scopeApprovalsCollection.replaceOne(
                and(eq(FIELD_DOMAIN, scopeApproval.getDomain()),
                        eq(FIELD_CLIENT_ID, scopeApproval.getClientId()),
                        eq(FIELD_USER_ID, scopeApproval.getUserId()),
                        eq(FIELD_SCOPE, scopeApproval.getScope()))
                , scopeApprovalMongo)).flatMap(updateResult -> Single.just(scopeApproval));
    }

    @Override
    public Single<ScopeApproval> upsert(ScopeApproval scopeApproval) {
        return Observable.fromPublisher(scopeApprovalsCollection.find(
                and(eq(FIELD_DOMAIN, scopeApproval.getDomain()),
                        eq(FIELD_CLIENT_ID, scopeApproval.getClientId()),
                        eq(FIELD_USER_ID, scopeApproval.getUserId()),
                        eq(FIELD_SCOPE, scopeApproval.getScope()))).first())
                .firstElement()
                .map(Optional::of)
                .defaultIfEmpty(Optional.empty())
                .flatMapSingle(optionalApproval -> {
                    if (optionalApproval.isEmpty()) {
                        scopeApproval.setCreatedAt(new Date());
                        scopeApproval.setUpdatedAt(scopeApproval.getCreatedAt());
                        return create(scopeApproval);
                    } else {
                        scopeApproval.setId(optionalApproval.get().getId());
                        scopeApproval.setUpdatedAt(new Date());
                        return update(scopeApproval);
                    }
                });
    }

    @Override
    public Completable deleteByDomainAndScopeKey(String domain, String scope) {
        return Completable.fromPublisher(scopeApprovalsCollection.deleteMany(
                and(eq(FIELD_DOMAIN, domain), eq(FIELD_SCOPE, scope))));
    }

    @Override
    public Completable delete(String id) {
        return Completable.fromPublisher(scopeApprovalsCollection.deleteOne(eq(FIELD_ID, id)));
    }

    @Override
    public Completable deleteByDomainAndUserAndClient(String domain, String user, String client) {
        return Completable.fromPublisher(scopeApprovalsCollection.deleteMany(
                and(eq(FIELD_DOMAIN, domain), eq(FIELD_USER_ID, user), eq(FIELD_CLIENT_ID, client))));
    }

    @Override
    public Completable deleteByDomainAndUser(String domain, String user) {
        return Completable.fromPublisher(scopeApprovalsCollection.deleteMany(
                and(eq(FIELD_DOMAIN, domain), eq(FIELD_USER_ID, user))));
    }

    private Single<ScopeApproval> _findById(String id) {
        return Single.fromPublisher(scopeApprovalsCollection.find(eq(FIELD_ID, id)).first()).map(this::convert);
    }

    private ScopeApproval convert(ScopeApprovalMongo scopeApprovalMongo) {
        if (scopeApprovalMongo == null) {
            return null;
        }

        ScopeApproval scopeApproval = new ScopeApproval();
        scopeApproval.setId(scopeApprovalMongo.getId());
        scopeApproval.setTransactionId(scopeApprovalMongo.getTransactionId());
        scopeApproval.setClientId(scopeApprovalMongo.getClientId());
        scopeApproval.setUserId(scopeApprovalMongo.getUserId());
        scopeApproval.setScope(scopeApprovalMongo.getScope());
        scopeApproval.setExpiresAt(scopeApprovalMongo.getExpiresAt());
        scopeApproval.setStatus(ScopeApproval.ApprovalStatus.valueOf(scopeApprovalMongo.getStatus().toUpperCase()));
        scopeApproval.setDomain(scopeApprovalMongo.getDomain());
        scopeApproval.setCreatedAt(scopeApprovalMongo.getCreatedAt());
        scopeApproval.setUpdatedAt(scopeApprovalMongo.getUpdatedAt());

        return scopeApproval;
    }

    private ScopeApprovalMongo convert(ScopeApproval scopeApproval) {
        if (scopeApproval == null) {
            return null;
        }

        ScopeApprovalMongo scopeApprovalMongo = new ScopeApprovalMongo();
        scopeApprovalMongo.setId(scopeApproval.getId());
        scopeApprovalMongo.setTransactionId(scopeApproval.getTransactionId());
        scopeApprovalMongo.setClientId(scopeApproval.getClientId());
        scopeApprovalMongo.setUserId(scopeApproval.getUserId());
        scopeApprovalMongo.setScope(scopeApproval.getScope());
        scopeApprovalMongo.setExpiresAt(scopeApproval.getExpiresAt());
        scopeApprovalMongo.setStatus(scopeApproval.getStatus().name().toUpperCase());
        scopeApprovalMongo.setDomain(scopeApproval.getDomain());
        scopeApprovalMongo.setCreatedAt(scopeApproval.getCreatedAt());
        scopeApprovalMongo.setUpdatedAt(scopeApproval.getUpdatedAt());

        return scopeApprovalMongo;
    }
}
