/*
 * Copyright (C) 2014 enStage Inc. Cupertino, California USA
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
package com.enstage.wibmo.sdk.inapp.pojo;

/**
 * Created by akshath on 19/11/14.
 */
public class WPayInitRequest extends W2faInitRequest {
    private static final long serialVersionUID = 1L;

    private boolean performAuthorization = true;

    @Deprecated
    public boolean isPerformAuthorization() {
        return performAuthorization;
    }

    @Deprecated
    public void setPerformAuthorization(boolean performAuthorization) {
        this.performAuthorization = performAuthorization;
    }
}
