/*
 * Copyright 1999-2018 Alibaba Group Holding Ltd.
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
package com.alibaba.csp.sentinel.slotchain;

import com.alibaba.csp.sentinel.log.RecordLog;
import com.alibaba.csp.sentinel.slots.DefaultSlotChainBuilder;
import com.alibaba.csp.sentinel.spi.SpiLoader;

/**
 * A provider for creating slot chains via resolved slot chain builder SPI.
 *
 * @author Eric Zhao
 * @since 0.2.0
 */
public final class SlotChainProvider {

    private static volatile SlotChainBuilder slotChainBuilder = null;

    /**
     * The load and pick process is not thread-safe, but it's okay since the method should be only invoked
     * via {@code lookProcessChain} in {@link com.alibaba.csp.sentinel.CtSph} under lock.
     *
     * @return new created slot chain
     */
    public static ProcessorSlotChain newSlotChain() {
        // 通过slotChainBuilder创建slotChain
        // 如果slotChainBuilder不为空则直接创建 否则先创建一个slotChainBuilder
        if (slotChainBuilder != null) {
            return slotChainBuilder.build();
        }

        // Resolve the slot chain builder SPI.
        // 通过SPI创建一个slotChainBuilder
        slotChainBuilder = SpiLoader.of(SlotChainBuilder.class).loadFirstInstanceOrDefault();

        // 如果SPI创建失败 则创建一个默认的
        if (slotChainBuilder == null) {
            // Should not go through here.
            RecordLog.warn("[SlotChainProvider] Wrong state when resolving slot chain builder, using default");
            slotChainBuilder = new DefaultSlotChainBuilder();
        } else {
            RecordLog.info("[SlotChainProvider] Global slot chain builder resolved: {}",
                slotChainBuilder.getClass().getCanonicalName());
        }
        return slotChainBuilder.build();
    }

    private SlotChainProvider() {}
}
