/*
 * Copyright (C) 2019-2024 Hedera Hashgraph, LLC
 *
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
 */

package com.hedera.mirror.importer.parser.record.transactionhandler;

import static com.hedera.mirror.common.domain.token.TokenFreezeStatusEnum.FROZEN;
import static com.hedera.mirror.common.domain.token.TokenFreezeStatusEnum.NOT_APPLICABLE;
import static com.hedera.mirror.common.domain.token.TokenFreezeStatusEnum.UNFROZEN;

import com.hedera.mirror.common.domain.entity.Entity;
import com.hedera.mirror.common.domain.entity.EntityId;
import com.hedera.mirror.common.domain.entity.EntityType;
import com.hedera.mirror.common.domain.token.Token;
import com.hedera.mirror.common.domain.token.TokenAccount;
import com.hedera.mirror.common.domain.token.TokenKycStatusEnum;
import com.hedera.mirror.common.domain.token.TokenPauseStatusEnum;
import com.hedera.mirror.common.domain.token.TokenSupplyTypeEnum;
import com.hedera.mirror.common.domain.token.TokenTypeEnum;
import com.hedera.mirror.common.domain.transaction.RecordItem;
import com.hedera.mirror.common.domain.transaction.Transaction;
import com.hedera.mirror.common.domain.transaction.TransactionType;
import com.hedera.mirror.common.util.DomainUtils;
import com.hedera.mirror.importer.domain.EntityIdService;
import com.hedera.mirror.importer.parser.record.entity.EntityListener;
import com.hedera.mirror.importer.parser.record.entity.EntityProperties;
import com.hedera.mirror.importer.util.Utility;
import com.hederahashgraph.api.proto.java.TokenAssociation;
import jakarta.inject.Named;
import lombok.CustomLog;

@CustomLog
@Named
class TokenCreateTransactionHandler extends AbstractEntityCrudTransactionHandler {

    private final EntityProperties entityProperties;
    private final TokenFeeScheduleUpdateTransactionHandler tokenFeeScheduleUpdateTransactionHandler;

    TokenCreateTransactionHandler(
            EntityIdService entityIdService,
            EntityListener entityListener,
            EntityProperties entityProperties,
            TokenFeeScheduleUpdateTransactionHandler tokenFeeScheduleUpdateTransactionHandler) {
        super(entityIdService, entityListener, TransactionType.TOKENCREATION);
        this.entityProperties = entityProperties;
        this.tokenFeeScheduleUpdateTransactionHandler = tokenFeeScheduleUpdateTransactionHandler;
    }

    @Override
    public EntityId getEntity(RecordItem recordItem) {
        return EntityId.of(recordItem.getTransactionRecord().getReceipt().getTokenID());
    }

    @Override
    protected void doUpdateEntity(Entity entity, RecordItem recordItem) {
        var transactionBody = recordItem.getTransactionBody().getTokenCreation();

        if (transactionBody.hasAdminKey()) {
            entity.setKey(transactionBody.getAdminKey().toByteArray());
        }

        if (transactionBody.hasAutoRenewAccount()) {
            var autoRenewAccountId = entityIdService
                    .lookup(transactionBody.getAutoRenewAccount())
                    .orElse(EntityId.EMPTY);
            if (EntityId.isEmpty(autoRenewAccountId)) {
                Utility.handleRecoverableError("Invalid autoRenewAccountId at {}", recordItem.getConsensusTimestamp());
            } else {
                entity.setAutoRenewAccountId(autoRenewAccountId.getId());
                recordItem.addEntityId(autoRenewAccountId);
            }
        }

        if (transactionBody.hasAutoRenewPeriod()) {
            entity.setAutoRenewPeriod(transactionBody.getAutoRenewPeriod().getSeconds());
        }

        if (transactionBody.hasExpiry()) {
            entity.setExpirationTimestamp(DomainUtils.timestampInNanosMax(transactionBody.getExpiry()));
        }

        entity.setMemo(transactionBody.getMemo());
        entity.setType(EntityType.TOKEN);
        entityListener.onEntity(entity);
    }

    @Override
    @SuppressWarnings("java:S3776")
    protected void doUpdateTransaction(Transaction transaction, RecordItem recordItem) {
        if (!entityProperties.getPersist().isTokens() || !recordItem.isSuccessful()) {
            return;
        }

        var transactionBody = recordItem.getTransactionBody().getTokenCreation();
        long consensusTimestamp = transaction.getConsensusTimestamp();
        boolean freezeDefault = transactionBody.getFreezeDefault();
        var tokenId = transaction.getEntityId();
        var treasury = EntityId.of(transactionBody.getTreasury());

        var token = new Token();
        token.setCreatedTimestamp(consensusTimestamp);
        token.setDecimals(transactionBody.getDecimals());
        token.setFreezeDefault(freezeDefault);
        token.setInitialSupply(transactionBody.getInitialSupply());
        token.setMaxSupply(transactionBody.getMaxSupply());
        token.setMetadata(DomainUtils.toBytes(transactionBody.getMetadata()));
        token.setName(transactionBody.getName());
        token.setSupplyType(TokenSupplyTypeEnum.fromId(transactionBody.getSupplyTypeValue()));
        token.setSymbol(transactionBody.getSymbol());
        token.setTimestampLower(consensusTimestamp);
        token.setTokenId(tokenId.getId());
        token.setTotalSupply(transactionBody.getInitialSupply());
        token.setTreasuryAccountId(treasury);
        token.setType(TokenTypeEnum.fromId(transactionBody.getTokenTypeValue()));

        if (transactionBody.hasFeeScheduleKey()) {
            token.setFeeScheduleKey(transactionBody.getFeeScheduleKey().toByteArray());
        }

        if (transactionBody.hasFreezeKey()) {
            token.setFreezeKey(transactionBody.getFreezeKey().toByteArray());
            token.setFreezeStatus(freezeDefault ? FROZEN : UNFROZEN);
        } else {
            token.setFreezeStatus(NOT_APPLICABLE);
        }

        if (transactionBody.hasKycKey()) {
            token.setKycKey(transactionBody.getKycKey().toByteArray());
            token.setKycStatus(TokenKycStatusEnum.REVOKED);
        } else {
            token.setKycStatus(TokenKycStatusEnum.NOT_APPLICABLE);
        }

        if (transactionBody.hasMetadataKey()) {
            token.setMetadataKey(transactionBody.getMetadataKey().toByteArray());
        }

        if (transactionBody.hasPauseKey()) {
            token.setPauseKey(transactionBody.getPauseKey().toByteArray());
            token.setPauseStatus(TokenPauseStatusEnum.UNPAUSED);
        } else {
            token.setPauseStatus(TokenPauseStatusEnum.NOT_APPLICABLE);
        }

        if (transactionBody.hasSupplyKey()) {
            token.setSupplyKey(transactionBody.getSupplyKey().toByteArray());
        }

        if (transactionBody.hasWipeKey()) {
            token.setWipeKey(transactionBody.getWipeKey().toByteArray());
        }

        var customFees = transactionBody.getCustomFeesList();
        var autoAssociatedAccounts =
                tokenFeeScheduleUpdateTransactionHandler.updateCustomFees(customFees, recordItem, transaction);
        autoAssociatedAccounts.add(treasury);

        // automatic_token_associations does not exist prior to services 0.18.0
        if (recordItem.getTransactionRecord().getAutomaticTokenAssociationsCount() > 0) {
            autoAssociatedAccounts.clear();
            recordItem.getTransactionRecord().getAutomaticTokenAssociationsList().stream()
                    .map(TokenAssociation::getAccountId)
                    .map(EntityId::of)
                    .forEach(autoAssociatedAccounts::add);
        }

        var freezeStatus = token.getFreezeKey() != null ? UNFROZEN : NOT_APPLICABLE;
        var kycStatus = token.getKycKey() != null ? TokenKycStatusEnum.GRANTED : TokenKycStatusEnum.NOT_APPLICABLE;

        autoAssociatedAccounts.forEach(account -> {
            var tokenAccount = new TokenAccount();
            tokenAccount.setAccountId(account.getId());
            tokenAccount.setAssociated(true);
            tokenAccount.setAutomaticAssociation(false);
            tokenAccount.setBalance(0L);
            tokenAccount.setBalanceTimestamp(consensusTimestamp);
            tokenAccount.setCreatedTimestamp(consensusTimestamp);
            tokenAccount.setFreezeStatus(freezeStatus);
            tokenAccount.setKycStatus(kycStatus);
            tokenAccount.setTimestampLower(consensusTimestamp);
            tokenAccount.setTokenId(tokenId.getId());
            entityListener.onTokenAccount(tokenAccount);

            recordItem.addEntityId(account);
        });

        entityListener.onToken(token);
    }
}
