/*
 * Copyright (C) 2021 iofairy, <https://github.com/iofairy/rainforest>
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
 */
package com.iofairy.rainforest.zip.error;


import com.iofairy.si.SI;
import com.iofairy.rainforest.zip.ac.SuperAC;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

/**
 * When using {@link SuperAC}, {@code SuperACException} may be thrown. <br>
 * 使用 {@link SuperAC} 中的方法时，可能会抛出此异常
 *
 * @since 0.5.1
 */
public class SuperACException extends RuntimeException {

    private static final long serialVersionUID = 99953678985535555L;

    /**
     * error code
     */
    @Getter
    @Setter
    @Accessors(chain = true)
    protected String code;

    /**
     * Constructs a {@code SuperACException} <br>
     *
     * @param msgTemplate message template. It is recommended to use any one of <b>{@code ${0}}</b> or <b>{@code ${?}}</b> or <b>{@code ${…}}</b>
     *                    or <b>{@code ${_}}</b> or <b>meaningful names</b> as placeholders
     * @param args        arguments use to fill placeholder
     */
    public SuperACException(String msgTemplate, Object... args) {
        super(getMsg(msgTemplate, args));
    }

    public SuperACException(Throwable cause, String msgTemplate, Object... args) {
        super(getMsg(msgTemplate, args), cause);
    }

    public SuperACException(Throwable cause) {
        super(cause);
    }

    private static String getMsg(String msgTemplate, Object... args) {
        if (msgTemplate == null) return null;
        return SI.$(msgTemplate, args);
    }

}