/*
 * Copyright (C) 2022-2025 Hedera Hashgraph, LLC
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

package com.hedera.services.utils.accessors;

import static org.junit.jupiter.api.Assertions.assertThrows;

import com.google.protobuf.ByteString;
import com.hederahashgraph.api.proto.java.Transaction;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class AccessorFactoryTest {

    AccessorFactory subject;

    @BeforeEach
    void setUp() {
        subject = new AccessorFactory();
    }

    @Test
    void uncheckedSpecializedAccessorThrows() {
        final var invalidTxnBytes = "InvalidTxnBytes".getBytes();
        final var txn = Transaction.newBuilder()
                .setSignedTransactionBytes(ByteString.copyFrom(invalidTxnBytes))
                .build();
        assertThrows(IllegalArgumentException.class, () -> subject.uncheckedSpecializedAccessor(txn));
    }
}
