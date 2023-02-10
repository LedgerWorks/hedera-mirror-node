package com.hedera.mirror.importer.parser.record.kafka;

import com.hederahashgraph.api.proto.java.TransactionBody;
import com.hederahashgraph.api.proto.java.TransactionRecord;
import io.lworks.importer.protobuf.RecordItemOuterClass;
import javax.inject.Named;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.core.annotation.Order;
import org.springframework.kafka.core.KafkaTemplate;

import com.hedera.mirror.common.domain.transaction.RecordItem;
import com.hedera.mirror.common.util.DomainUtils;
import com.hedera.mirror.importer.exception.ImporterException;
import com.hedera.mirror.importer.parser.record.RecordItemListener;
import com.hedera.mirror.importer.util.Utility;

@Log4j2
@Named
@RequiredArgsConstructor
@ConditionalOnKafkaRecordParser
@Order(0)
public class KafkaRecordItemListener implements RecordItemListener {

    private final KafkaProperties kafkaProperties;
    private final KafkaTemplate<String, byte[]> kafkaTemplate;

    @Override
    public void onItem(RecordItem recordItem) throws ImporterException {
        TransactionBody body = recordItem.getTransactionBody();
        TransactionRecord txRecord = recordItem.getTransactionRecord();
        log.trace("Storing transaction body: {}", () -> Utility.printProtoMessage(body));
        long consensusTimestamp = DomainUtils.timeStampInNanos(txRecord.getConsensusTimestamp());

        String key = String.valueOf(txRecord.getTransactionID().getAccountID().getAccountNum());

        io.lworks.importer.protobuf.RecordItemOuterClass.RecordItem kafkaRecordItem =
                buildRecordItem(consensusTimestamp, txRecord, body);
        log.debug("Processing transaction {} - {}", key, consensusTimestamp);
        kafkaTemplate.send(kafkaProperties.getRecordItemsTopic(), key, kafkaRecordItem.toByteArray());
        log.debug("Processed transaction {} - {}", key, consensusTimestamp);
    }

    private io.lworks.importer.protobuf.RecordItemOuterClass.RecordItem buildRecordItem(long consensusTimestamp,
                                                                                        TransactionRecord transactionRecord,
                                                                                        TransactionBody transactionBody) {
        return RecordItemOuterClass.RecordItem.newBuilder()
                .setConsensusTimestamp(consensusTimestamp)
                .setTransactionRecord(transactionRecord)
                .setTransactionBody(transactionBody)
                .build();
    }
}
