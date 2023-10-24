/*
 * Copyright (C) 2023 Hedera Hashgraph, LLC
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

package com.hedera.mirror.importer.parser.record.kafka;

import com.hedera.mirror.common.domain.transaction.RecordItem;
import com.hedera.mirror.common.util.DomainUtils;
import com.hedera.mirror.importer.exception.ImporterException;
import com.hedera.mirror.importer.parser.record.RecordItemListener;
import com.hedera.mirror.importer.util.Utility;
import com.hederahashgraph.api.proto.java.TransactionBody;
import com.hederahashgraph.api.proto.java.TransactionRecord;
import io.lworks.importer.protobuf.RecordItemOuterClass;
import jakarta.inject.Named;
import lombok.RequiredArgsConstructor;
import lombok.CustomLog;
import org.springframework.core.annotation.Order;
import org.springframework.kafka.core.KafkaTemplate;

@CustomLog
@Named
@RequiredArgsConstructor
@ConditionalOnKafkaRecordParser
@Order(0)
public class KafkaRecordItemListener implements RecordItemListener {

  private final CommonParserProperties commonParserProperties;
  private final KafkaProperties kafkaProperties;
  private final KafkaTemplate<String, byte[]> kafkaTemplate;
  private final TransactionHandlerFactory transactionHandlerFactory;

  @Override
  public void onItem(RecordItem recordItem) throws ImporterException {
    TransactionBody body = recordItem.getTransactionBody();
    TransactionRecord txRecord = recordItem.getTransactionRecord();

    int transactionTypeValue = recordItem.getTransactionType();
    TransactionType transactionType = TransactionType.of(transactionTypeValue);
    TransactionHandler transactionHandler = transactionHandlerFactory.get(transactionType);

    long consensusTimestamp = DomainUtils.timeStampInNanos(txRecord.getConsensusTimestamp());

    EntityId entityId;
    try {
        entityId = transactionHandler.getEntity(recordItem);
    } catch (InvalidEntityException e) { // transaction can have invalid topic/contract/file id
        log.error(
                RECOVERABLE_ERROR + "Invalid entity encountered for consensusTimestamp {} : {}",
                consensusTimestamp,
                e.getMessage());
        entityId = EntityId.EMPTY;
    }

    // to:do - exclude Freeze from Filter transaction type
    TransactionFilterFields transactionFilterFields = getTransactionFilterFields(entityId, recordItem);
    Collection<EntityId> entities = transactionFilterFields.getEntities();
    log.debug("Processing {} transaction {} for entities {}", transactionType, consensusTimestamp, entities);
    if (!commonParserProperties.getFilter().test(transactionFilterFields)) {
        log.debug(
                "Ignoring transaction. consensusTimestamp={}, transactionType={}, entities={}",
                consensusTimestamp,
                transactionType,
                entities);
        return;
    }

    log.trace("Storing transaction body: {}", () -> Utility.printProtoMessage(body));
    String key = String.valueOf(txRecord.getTransactionID().getAccountID().getAccountNum());

    io.lworks.importer.protobuf.RecordItemOuterClass.RecordItem kafkaRecordItem = buildRecordItem(consensusTimestamp,
        txRecord, body);
    log.debug("Processing transaction {} - {}", key, consensusTimestamp);
    kafkaTemplate.send(kafkaProperties.getRecordItemsTopic(), key, kafkaRecordItem.toByteArray());
    log.debug("Processed transaction {} - {}", key, consensusTimestamp);
  }

  private io.lworks.importer.protobuf.RecordItemOuterClass.RecordItem buildRecordItem(
      long consensusTimestamp, TransactionRecord transactionRecord, TransactionBody transactionBody) {
    return RecordItemOuterClass.RecordItem.newBuilder()
        .setConsensusTimestamp(consensusTimestamp)
        .setTransactionRecord(transactionRecord)
        .setTransactionBody(transactionBody)
        .build();
  }
}
