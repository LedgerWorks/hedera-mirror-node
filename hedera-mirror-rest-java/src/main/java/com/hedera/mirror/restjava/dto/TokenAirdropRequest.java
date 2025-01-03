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

package com.hedera.mirror.restjava.dto;

import static com.hedera.mirror.restjava.common.Constants.RECEIVER_ID;
import static com.hedera.mirror.restjava.common.Constants.SENDER_ID;
import static com.hedera.mirror.restjava.jooq.domain.Tables.TOKEN_AIRDROP;

import com.hedera.mirror.restjava.common.EntityIdParameter;
import com.hedera.mirror.restjava.service.Bound;
import java.util.List;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.jooq.Field;
import org.springframework.data.domain.Sort;

@Data
@Builder
public class TokenAirdropRequest {

    // Sender Id for Outstanding Airdrops, Receiver Id for Pending Airdrops
    private EntityIdParameter accountId;

    @Builder.Default
    private int limit = 25;

    @Builder.Default
    private Sort.Direction order = Sort.Direction.ASC;

    // Receiver Id for Outstanding Airdrops, Sender Id for Pending Airdrops
    @Builder.Default
    private Bound entityIds = Bound.EMPTY;

    @Builder.Default
    private Bound serialNumbers = Bound.EMPTY;

    @Builder.Default
    private Bound tokenIds = Bound.EMPTY;

    @Builder.Default
    private AirdropRequestType type = AirdropRequestType.OUTSTANDING;

    @Getter
    @RequiredArgsConstructor
    public enum AirdropRequestType {
        OUTSTANDING(TOKEN_AIRDROP.SENDER_ACCOUNT_ID, TOKEN_AIRDROP.RECEIVER_ACCOUNT_ID, RECEIVER_ID),
        PENDING(TOKEN_AIRDROP.RECEIVER_ACCOUNT_ID, TOKEN_AIRDROP.SENDER_ACCOUNT_ID, SENDER_ID);

        // The base field is the conditional clause for the base DB query.
        // The base field is the path parameter accountId, which is Sender Id for Outstanding Airdrops and Receiver Id
        // for Pending Airdrops
        private final Field<Long> baseField;

        // The primary field is the primary sort field for the DB query.
        // The primary field is the optional query parameter 'entityIds', which is Receiver Id for Outstanding Airdrops
        // and Sender Id for Pending Airdrops
        private final Field<Long> primaryField;

        // The primary query parameter
        private final String parameter;
    }

    public List<Bound> getBounds() {
        var primaryBound = !entityIds.isEmpty() ? entityIds : tokenIds;
        if (primaryBound.isEmpty()) {
            return List.of(serialNumbers);
        }

        var secondaryBound = !tokenIds.isEmpty() ? tokenIds : serialNumbers;
        if (secondaryBound.isEmpty()) {
            return List.of(primaryBound);
        }

        return List.of(primaryBound, secondaryBound, serialNumbers);
    }
}
