package com.hedera.mirror.importer.parser.record.transactionhandler;

/*-
 * ‌
 * Hedera Mirror Node
 * ​
 * Copyright (C) 2019 - 2023 Hedera Hashgraph, LLC
 * ​
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * ‍
 */

import javax.inject.Named;

import com.hedera.mirror.common.domain.entity.Entity;
import com.hedera.mirror.common.domain.entity.EntityId;
import com.hedera.mirror.common.domain.transaction.RecordItem;
import com.hedera.mirror.common.domain.transaction.TransactionType;
import com.hedera.mirror.importer.domain.EntityIdService;
import com.hedera.mirror.importer.parser.record.entity.EntityListener;

@Named
class SystemDeleteTransactionHandler extends AbstractEntityCrudTransactionHandler {

    SystemDeleteTransactionHandler(EntityIdService entityIdService, EntityListener entityListener) {
        super(entityIdService, entityListener, TransactionType.SYSTEMDELETE);
    }

    @Override
    public EntityId getEntity(RecordItem recordItem) {
        var systemDelete = recordItem.getTransactionBody().getSystemDelete();

        if (systemDelete.hasContractID()) {
            return entityIdService.lookup(systemDelete.getContractID()).orElse(EntityId.EMPTY);
        } else if (systemDelete.hasFileID()) {
            return EntityId.of(systemDelete.getFileID());
        }

        return null;
    }

    @Override
    protected void doUpdateEntity(Entity entity, RecordItem recordItem) {
        entityListener.onEntity(entity);
    }
}
