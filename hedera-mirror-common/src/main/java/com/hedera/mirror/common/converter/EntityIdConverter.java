/*
 * Copyright (C) 2019-2025 Hedera Hashgraph, LLC
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

package com.hedera.mirror.common.converter;

import com.hedera.mirror.common.domain.entity.EntityId;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import org.springframework.boot.context.properties.ConfigurationPropertiesBinding;

@Converter
@ConfigurationPropertiesBinding
@SuppressWarnings("java:S6548")
public class EntityIdConverter implements AttributeConverter<EntityId, Long> {

    public static final EntityIdConverter INSTANCE = new EntityIdConverter();

    @Override
    public Long convertToDatabaseColumn(EntityId entityId) {
        if (EntityId.isEmpty(entityId)) {
            return null;
        }
        return entityId.getId();
    }

    @Override
    public EntityId convertToEntityAttribute(Long encodedId) {
        if (encodedId == null) {
            return null;
        }
        return EntityId.of(encodedId);
    }
}
