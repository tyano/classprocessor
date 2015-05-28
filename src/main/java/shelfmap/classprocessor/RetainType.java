/*
 * Copyright 2011 Tsutomu YANO.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package shelfmap.classprocessor;

/**
 *
 * @author Tsutomu YANO
 */
public enum RetainType implements RetainCode {
    HOLD {
        @Override
        public String codeFor(String argName, Attribute attribute) {
            return argName;
        }
    },
    NEW {
        @Override
        public String codeFor(String argName, Attribute attribute) {
            return String.format("%2$s == null ? null : new %1$s(%2$s)", attribute.getRealType().toString(), argName);
        }
    },
    CLONE {
        @Override
        public String codeFor(String argName, Attribute attribute) {
            return argName + ".clone()";
        }
    };
}
