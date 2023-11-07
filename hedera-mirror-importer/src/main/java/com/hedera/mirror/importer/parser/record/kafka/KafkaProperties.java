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

import com.google.api.client.util.Sets;
import com.google.common.collect.ImmutableSet;
import lombok.Data;
import lombok.Getter;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.HashSet;
import java.util.Set;

@Data
@ConfigurationProperties("hedera.mirror.importer.parser.record.kafka")
public class KafkaProperties {

    private boolean enabled = false;

    private String bootstrapServers = "";

    private String producerApiKey = "";

    private String producerApiKeySecret = "";

    private String recordItemsTopic = "";

    private String ignorePayers = "";

    @Getter(lazy = true)
    private final Set<String> ignoredPayersSet = ignoredPayersSet();

    private Set<String> ignoredPayersSet() {
        return ImmutableSet.copyOf(ignorePayers.split(","));
    }
}
