/*
 * Copyright 2018 Heiko Scherrer
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
package org.openwms.common.location.api;

import org.openwms.common.LocationVO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Optional;

/**
 * A LocationApi.
 *
 * @author <a href="mailto:scherrer@openwms.org">Heiko Scherrer</a>
 */
@FeignClient(name = "common-service", qualifier = "locationApi", decode404 = true)
public interface LocationApi {

    /**
     * Find and return all existing {@code LocationGroup} representations.
     *
     * @return Never {@literal null}
     */
    @GetMapping(value = "/v1/locations", params = {"locationPK"})
    Optional<LocationVO> findLocationByCoordinate(@RequestParam("locationPK") String locationPK);
}
