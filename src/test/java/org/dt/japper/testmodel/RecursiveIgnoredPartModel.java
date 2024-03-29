package org.dt.japper.testmodel;

/*
 * Copyright (c) 2018, David Sykes and Tomasz Orzechowski
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * - Redistributions of source code must retain the above copyright notice, this
 * list of conditions and the following disclaimer.
 *
 * - Redistributions in binary form must reproduce the above copyright notice,
 * this list of conditions and the following disclaimer in the documentation
 * and/or other materials provided with the distribution.
 *
 * - Neither the name David Sykes nor Tomasz Orzechowski may be used to endorse
 * or promote products derived from this software without specific prior written
 * permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 *
 *
 */

import java.math.BigDecimal;
import java.util.Map;

import org.dt.japper.JapperIgnore;

public class RecursiveIgnoredPartModel {

    private String partno;
    private String description;
    private String partType;
    private Map<String, BigDecimal> flexFields;

    // This is the recursive part!
    private RecursiveIgnoredPartModel otherPart;

    public String getPartno() {
        return partno;
    }
    public void setPartno(String partno) {
        this.partno = partno;
    }
    public String getDescription() {
        return description;
    }
    public void setDescription(String description) {
        this.description = description;
    }
    public String getPartType() {
        return partType;
    }
    public void setPartType(String partType) {
        this.partType = partType;
    }


    public Map<String, BigDecimal> getFlexFields() {
        return flexFields;
    }

    public void setFlexFields(Map<String, BigDecimal> flexFields) {
        this.flexFields = flexFields;
    }

    public RecursiveIgnoredPartModel getOtherPart() {
        return otherPart;
    }

    @JapperIgnore
    public void setOtherPart(RecursiveIgnoredPartModel otherPart) {
        this.otherPart = otherPart;
    }
}
