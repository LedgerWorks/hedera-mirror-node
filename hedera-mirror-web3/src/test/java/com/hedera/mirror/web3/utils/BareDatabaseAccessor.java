/*
 * Copyright (C) 2024-2025 Hedera Hashgraph, LLC
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

package com.hedera.mirror.web3.utils;

import com.hedera.mirror.web3.evm.store.accessor.DatabaseAccessor;
import edu.umd.cs.findbugs.annotations.NonNull;
import java.util.Optional;

public class BareDatabaseAccessor<K, V> extends DatabaseAccessor<K, V> {
    @NonNull
    @Override
    public Optional<V> get(@NonNull final K key, Optional<Long> timestamp) {
        throw new UnsupportedOperationException("BareGroundTruthAccessor.get");
    }
}
