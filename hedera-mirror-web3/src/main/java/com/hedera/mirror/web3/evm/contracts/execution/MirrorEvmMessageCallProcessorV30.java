/*
 * Copyright (C) 2023-2024 Hedera Hashgraph, LLC
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

package com.hedera.mirror.web3.evm.contracts.execution;

import com.hedera.mirror.web3.evm.config.PrecompiledContractProvider;
import jakarta.inject.Named;
import java.util.function.Predicate;
import org.hyperledger.besu.datatypes.Address;
import org.hyperledger.besu.evm.EVM;
import org.hyperledger.besu.evm.gascalculator.GasCalculator;
import org.hyperledger.besu.evm.precompile.MainnetPrecompiledContracts;
import org.hyperledger.besu.evm.precompile.PrecompileContractRegistry;

@Named
public class MirrorEvmMessageCallProcessorV30 extends AbstractEvmMessageCallProcessor {

    public MirrorEvmMessageCallProcessorV30(
            @Named("evm030") EVM v30,
            PrecompileContractRegistry precompiles,
            final PrecompiledContractProvider precompilesHolder,
            final GasCalculator gasCalculator,
            final Predicate<Address> systemAccountDetector) {
        super(v30, precompiles, precompilesHolder.getHederaPrecompiles(), systemAccountDetector);
        MainnetPrecompiledContracts.populateForIstanbul(precompiles, gasCalculator);
    }
}
