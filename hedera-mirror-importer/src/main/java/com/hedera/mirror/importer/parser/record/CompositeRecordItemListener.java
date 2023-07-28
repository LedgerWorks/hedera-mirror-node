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

package com.hedera.mirror.importer.parser.record;

import com.hedera.mirror.common.domain.transaction.RecordItem;
import com.hedera.mirror.importer.exception.ImporterException;
import java.util.List;
import java.util.function.BiConsumer;
import jakarta.inject.Named;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.context.annotation.Primary;

@Log4j2
@Named
@Primary
@RequiredArgsConstructor
public class CompositeRecordItemListener implements RecordItemListener {

  private final List<RecordItemListener> recordItemListeners;

  private <T> void onEach(BiConsumer<RecordItemListener, T> consumer, T t) {
    for (RecordItemListener recordItemListener : recordItemListeners) {
      consumer.accept(recordItemListener, t);
    }
  }

  @Override
  public void onItem(RecordItem item) throws ImporterException {
    onEach(RecordItemListener::onItem, item);
  }
}
