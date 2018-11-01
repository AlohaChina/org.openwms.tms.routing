/*
 * openwms.org, the Open Warehouse Management System.
 * Copyright (C) 2014 Heiko Scherrer
 *
 * This file is part of openwms.org.
 *
 * openwms.org is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as
 * published by the Free Software Foundation; either version 2 of the
 * License, or (at your option) any later version.
 *
 * openwms.org is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this software. If not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.openwms.common.comm.upd;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * A UpdateVO.
 *
 * @author <a href="mailto:scherrer@openwms.org">Heiko Scherrer</a>
 */
// ajc has a problem here with lombok
public class UpdateVO implements Serializable {

    private String actualLocation, barcode, errorCode;

    public void setActualLocation(String actualLocation) {
        this.actualLocation = actualLocation;
    }

    public void setBarcode(String barcode) {
        this.barcode = barcode;
    }

    public void setHeader(UpdateHeaderVO header) {
        this.header = header;
    }

    public String getErrorCode() {
        return errorCode;
    }

    public void setErrorCode(String errorCode) {
        this.errorCode = errorCode;
    }

    private UpdateHeaderVO header;

    public Map<String, Object> getAll() {
        Map<String, Object> result = new HashMap<>(4);
        result.put("actualLocation", actualLocation);
        result.put("barcode", barcode);
        result.put("errorCode", errorCode);
        result.put("reqHeader", header);
        return result;
    }

    public String getActualLocation() {
        return actualLocation;
    }

    public String getBarcode() {
        return barcode;
    }

    public UpdateHeaderVO getHeader() {
        return header;
    }
}