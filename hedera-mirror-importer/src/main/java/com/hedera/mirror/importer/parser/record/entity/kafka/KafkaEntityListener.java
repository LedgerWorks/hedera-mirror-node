package com.hedera.mirror.importer.parser.record.entity.kafka;

import org.springframework.core.annotation.Order;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import com.google.common.base.MoreObjects;
import com.hedera.mirror.common.domain.entity.EntityId;
import com.hedera.mirror.common.domain.transaction.Transaction;
import com.hedera.mirror.importer.exception.ImporterException;
import com.hedera.mirror.importer.parser.record.entity.ConditionOnEntityRecordParser;
import com.hedera.mirror.importer.parser.record.entity.EntityListener;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;

@Log4j2
// Write records to Kafka Queue BEFORE the database to guarantee we do not drop
// records.
// The downside with this approach is potentially duplicating records, which we
// will need to deal with the in the kafka consumers.
// Long term, we should probably write the records to the database in a kafka
// consumer
@Order(0)
@Component
@ConditionOnEntityRecordParser
@RequiredArgsConstructor
public class KafkaEntityListener implements EntityListener {

    private final KafkaProperties kafkaProperties;
    private final KafkaTemplate<String, Object> kafkaTemplate;

    @Override
    public void onTransaction(Transaction txn) throws ImporterException {
        try {
            String topic = kafkaProperties.getTransactionsTopic();
            EntityId payerAccountId = txn.getPayerAccountId();
            long consensusTimestamp = txn.getConsensusTimestamp();
            var key = MoreObjects.firstNonNull(payerAccountId, consensusTimestamp);
            if (key != payerAccountId) {
                log.info("payerAccountId is null. Using consensusTimestamp instead: {}", consensusTimestamp);
            }

            log.debug("Processing transaction {} - {}", key, consensusTimestamp);
            kafkaTemplate.send(topic, key.toString(), txn);
            log.debug("Processed transaction {} - {}", key, consensusTimestamp);
        }
        catch (Exception ex) {
            log.error("Failed to process transaction", ex);
        }
    }

    @Override
    public boolean isEnabled() {
        return kafkaProperties.isEnabled();
    }
}
