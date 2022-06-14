package com.hedera.mirror.importer.parser.record.entity;

/*-
 * ‌
 * Hedera Mirror Node
 * ​
 * Copyright (C) 2019 - 2022 Hedera Hashgraph, LLC
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

import java.util.List;

import javax.inject.Named;
import org.springframework.context.annotation.Primary;
import com.hedera.mirror.common.domain.transaction.RecordFile;
import com.hedera.mirror.importer.exception.ImporterException;
import com.hedera.mirror.importer.parser.StreamFileListener;
import com.hedera.mirror.importer.parser.record.RecordStreamFileListener;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;

@Log4j2
@Named
@Primary
@RequiredArgsConstructor
public class CompositeRecordFileListener implements RecordStreamFileListener {

    private final List<StreamFileListener<RecordFile>> recordStreamFileListeners;

    @Override
    public void onStart() throws ImporterException {
        for (StreamFileListener<RecordFile> recordFileListener : recordStreamFileListeners) {
            recordFileListener.onStart();
        }
    }

    @Override
    public void onEnd(RecordFile streamFile) throws ImporterException {
        for (StreamFileListener<RecordFile> recordFileListener : recordStreamFileListeners) {
            recordFileListener.onEnd(streamFile);
        }

    }

    @Override
    public void onError() {
        for (StreamFileListener<RecordFile> recordFileListener : recordStreamFileListeners) {
            recordFileListener.onError();
        }
    }
}
