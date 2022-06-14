package com.hedera.mirror.importer.parser.record.entity.kafka;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import java.util.Objects;

import javax.inject.Named;

import org.springframework.beans.factory.BeanCreationNotAllowedException;
import org.springframework.core.annotation.Order;

import com.google.common.base.Stopwatch;
import com.hedera.mirror.common.domain.addressbook.NodeStake;
import com.hedera.mirror.common.domain.contract.Contract;
import com.hedera.mirror.common.domain.contract.ContractLog;
import com.hedera.mirror.common.domain.contract.ContractResult;
import com.hedera.mirror.common.domain.contract.ContractStateChange;
import com.hedera.mirror.common.domain.entity.AbstractEntity;
import com.hedera.mirror.common.domain.entity.CryptoAllowance;
import com.hedera.mirror.common.domain.entity.Entity;
import com.hedera.mirror.common.domain.entity.EntityId;
import com.hedera.mirror.common.domain.entity.NftAllowance;
import com.hedera.mirror.common.domain.entity.TokenAllowance;
import com.hedera.mirror.common.domain.file.FileData;
import com.hedera.mirror.common.domain.schedule.Schedule;
import com.hedera.mirror.common.domain.token.Nft;
import com.hedera.mirror.common.domain.token.NftTransfer;
import com.hedera.mirror.common.domain.token.Token;
import com.hedera.mirror.common.domain.token.TokenAccount;
import com.hedera.mirror.common.domain.token.TokenAccountKey;
import com.hedera.mirror.common.domain.token.TokenTransfer;
import com.hedera.mirror.common.domain.topic.TopicMessage;
import com.hedera.mirror.common.domain.transaction.AssessedCustomFee;
import com.hedera.mirror.common.domain.transaction.CryptoTransfer;
import com.hedera.mirror.common.domain.transaction.CustomFee;
import com.hedera.mirror.common.domain.transaction.EthereumTransaction;
import com.hedera.mirror.common.domain.transaction.LiveHash;
import com.hedera.mirror.common.domain.transaction.NonFeeTransfer;
import com.hedera.mirror.common.domain.transaction.RecordFile;
import com.hedera.mirror.common.domain.transaction.StakingRewardTransfer;
import com.hedera.mirror.common.domain.transaction.Transaction;
import com.hedera.mirror.common.domain.transaction.TransactionSignature;
import com.hedera.mirror.importer.exception.ImporterException;
import com.hedera.mirror.importer.exception.ParserException;
import com.hedera.mirror.importer.parser.StreamFileListener;
import com.hedera.mirror.importer.parser.record.RecordStreamFileListener;
import com.hedera.mirror.importer.parser.record.entity.ConditionOnEntityRecordParser;
import com.hedera.mirror.importer.parser.record.entity.EntityBatchCleanupEvent;
import com.hedera.mirror.importer.parser.record.entity.EntityBatchSaveEvent;
import com.hedera.mirror.importer.parser.record.entity.EntityListener;

import lombok.extern.log4j.Log4j2;

@Log4j2
@Named
// Write records to Kafka Queue BEFORE the database to guarantee we do not drop
// records.
// The downside with this approach is potentially duplicating records, which we
// will need to deal with the in the kafka consumers.
// Long term, we should probably write the records to the database in a kafka
// consumer
@Order(0)
@ConditionOnEntityRecordParser
public class KafkaEntityListener implements EntityListener, StreamFileListener<RecordFile> {

    private final KafkaProperties kafkaProperties;

    private final Collection<Transaction> transactions;

    public KafkaEntityListener(
            KafkaProperties kafkaProperties) {
        this.kafkaProperties = kafkaProperties;

        transactions = new ArrayList<>();
    }

    @Override
    public boolean isEnabled() {
        return kafkaProperties.isEnabled();
    }

    @Override
    public void onStart() {
        cleanup();
    }

    @Override
    public void onEnd(RecordFile recordFile) {
        executeBatches();
    }

    @Override
    public void onError() {
        cleanup();
    }

    private void cleanup() {
        try {
            transactions.clear();
        } catch (BeanCreationNotAllowedException e) {
            // This error can occur during shutdown
        }
    }

    private void executeBatches() {
        try {
            Stopwatch stopwatch = Stopwatch.createStarted();
            log.info("Processing transactions: {}", transactions);
            log.info("Completed batch inserts in {}", stopwatch);
        } catch (ParserException e) {
            throw e;
        } catch (Exception e) {
            throw new ParserException(e);
        } finally {
            cleanup();
        }
    }

    @Override
    public void onTransaction(Transaction transaction) throws ImporterException {
        transactions.add(transaction);
        if (transactions.size() == kafkaProperties.getBatchSize()) {
            executeBatches();
        }
    }

}
