/*
 * Copyright (c) 2021, MegaEase
 * All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */
package com.megaease.easeagent.report.encoder;

import com.megaease.easeagent.plugin.report.EncodedData;
import com.megaease.easeagent.plugin.report.Packer;

import java.util.ArrayList;
import java.util.List;

public interface PackedMessage {
    List<EncodedData> getMessages();

    int packSize();

    int calculateAppendSize(int size);

    void addMessage(EncodedData msg);

    class DefaultPackedMessage implements PackedMessage {
        ArrayList<EncodedData> items;
        int packSize;
        Packer packer;

        public DefaultPackedMessage(int count, Packer packer) {
            this.items = new ArrayList<>(count);
            this.packSize = 0;
            this.packer = packer;
        }

        @Override
        public List<EncodedData> getMessages() {
            return items;
        }

        @Override
        public int packSize() {
            return packSize;
        }

        @Override
        public int calculateAppendSize(int size) {
            return this.packSize + this.packer.appendSizeInBytes(size);
        }

        public void addMessage(EncodedData msg) {
            this.items.add(msg);
            if (packSize == 0) {
                this.packSize = this.packer.messageSizeInBytes(items);
            } else {
                this.packSize += this.packer.appendSizeInBytes(msg.size());
            }
        }
    }
}
