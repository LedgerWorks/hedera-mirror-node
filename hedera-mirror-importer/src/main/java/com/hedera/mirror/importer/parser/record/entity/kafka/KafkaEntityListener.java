package com.hedera.mirror.importer.parser.record.entity.kafka;

import java.util.ArrayList;
import java.util.Collection;

import javax.inject.Named;

import org.springframework.beans.factory.BeanCreationNotAllowedException;
import org.springframework.core.annotation.Order;

import com.google.common.base.Stopwatch;
import com.hedera.mirror.common.domain.transaction.RecordFile;
import com.hedera.mirror.common.domain.transaction.Transaction;
import com.hedera.mirror.importer.exception.ImporterException;
import com.hedera.mirror.importer.exception.ParserException;
import com.hedera.mirror.importer.parser.StreamFileListener;
import com.hedera.mirror.importer.parser.record.entity.ConditionOnEntityRecordParser;
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
            log.info("Processing {} transactions", transactions.size());
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
