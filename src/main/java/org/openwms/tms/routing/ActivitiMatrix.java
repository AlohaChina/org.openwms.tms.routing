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
package org.openwms.tms.routing;

import org.openwms.common.location.api.LocationGroupApi;
import org.openwms.common.location.api.LocationGroupVO;
import org.openwms.common.location.api.LocationVO;
import org.openwms.core.SecurityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.hateoas.Link;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Optional;

/**
 * A ActivitiMatrix.
 *
 * @author <a href="mailto:scherrer@openwms.org">Heiko Scherrer</a>
 */
@Component
class ActivitiMatrix implements Matrix {

    private static final Logger LOGGER = LoggerFactory.getLogger(ActivitiMatrix.class);

    private final ActionRepository repository;
    private final RestTemplate restTemplate;
    private final LocationGroupApi locationGroupApi;
    private final DiscoveryClient dc;

    ActivitiMatrix(ActionRepository repository, @Qualifier("simpleRestTemplate") RestTemplate restTemplate, LocationGroupApi locationGroupApi, DiscoveryClient dc) {
        this.repository = repository;
        this.restTemplate = restTemplate;
        this.locationGroupApi = locationGroupApi;
        this.dc = dc;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Action findBy(String actionType, Route route, @Nullable LocationVO location, @Nullable LocationGroupVO locationGroup) {
        // search explicitly...
        Optional<Action> prg = Optional.empty();
        if (null != location && location.getLocationId() != null) {

            // First explicitly search for the Location and Route
            prg = repository.findByActionTypeAndRouteAndLocationKey(actionType, route.getRouteId(), location.getLocationId());
            if (!prg.isPresent()) {

                if (!RouteImpl.DEF_ROUTE.getRouteId().equals(route.getRouteId())) {
                    prg = repository.findByActionTypeAndRouteAndLocationKey(actionType, RouteImpl.DEF_ROUTE.getRouteId(), location.getLocationId());
                }
                if (!prg.isPresent()) {
                    // Not found with Location => check by Location's.LocationGroup
                    prg = findInLocationGroupHierarchy(actionType, route, locationGroupApi.findByName(location.getLocationGroupName()));
                    if (!prg.isPresent()) {

                        // Search the LocationGroup hierarchy the way up...
                        if (locationGroup == null) {
                            String message = String.format("No Action found for Route [%s] on source Location [%s] and source LocationGroup [%s]", route, location.getLocationId(), location.getLocationGroupName());
                            LOGGER.info(message);
                            throw new NoRouteException(message);
                        }
                        prg = findInLocationGroupHierarchy(actionType, route, locationGroup);

                        if (!prg.isPresent() && route.equals(RouteImpl.NO_ROUTE)) {
                            String message = String.format("No Action found for Route [%s] on source Location [%s] and source LocationGroup [%s]", route, location.getLocationId(), location.getLocationGroupName());
                            LOGGER.info(message);
                            throw new NoRouteException(message);
                        }

                        if (!prg.isPresent() && !route.equals(RouteImpl.DEF_ROUTE)) {
                            // Last chance: Search for the default route
                            prg = findInLocationGroupHierarchy(actionType, RouteImpl.DEF_ROUTE, locationGroup);
                        }

                        if (!prg.isPresent()) {
                            String message = String.format("No Action found for Route [%s] on source Location [%s] and source LocationGroup [%s]", route, location.getLocationId(), location.getLocationGroupName());
                            LOGGER.info(message);
                            throw new NoRouteException(message);
                        }
                    }
                }
            }
        }

        // search for locgroup...
        if (!prg.isPresent()) {
            if (null == locationGroup) {
                String message = String.format("No Action found for Route [%s] and Location [%s] without LocationGroup", route, location);
                LOGGER.info(message);
                throw new NoRouteException(message);
            }
            prg = findInLocationGroupHierarchy(actionType, route, locationGroup);
        }
        return prg.orElseThrow(() -> {
            String message = String.format("No Action found for Route [%s], Location [%s], LocationGroup [%s]", route, location, locationGroup);
            LOGGER.info(message);
            return new NoRouteException(message);
        });
    }

    private Optional<Action> findInLocationGroupHierarchy(String actionType, Route route, LocationGroupVO locationGroup) {
        Optional<Action> cp = repository.findByActionTypeAndRouteAndLocationGroupName(actionType, route.getRouteId(), locationGroup.getName());
        if (!cp.isPresent()) {

            if (locationGroup.hasParent()) {
                cp = findInLocationGroupHierarchy(actionType, route, locationGroupApi.findByName(locationGroup.getParent()));

            } else if (locationGroup.hasLink("_parent")) {
                cp = findInLocationGroupHierarchy(actionType, route, findLocationGroup(locationGroup.getLink("_parent")));

            }
        }
        return cp;
    }

    private LocationGroupVO findLocationGroup(Link parent) {
        List<ServiceInstance> list = dc.getInstances("common-service");
        if (list == null || list.size() == 0) {
            throw new RuntimeException("No deployed service with name common-service found");
        }
        ServiceInstance si = list.get(0);
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Calling common-service URL [{}]", parent.getHref());
        }
        ResponseEntity<LocationGroupVO> lg = restTemplate.exchange(parent.getHref(), HttpMethod.GET, new HttpEntity<>(SecurityUtils.createHeaders(si.getMetadata().get("username"), si.getMetadata().get("password"))), LocationGroupVO.class);
        return lg.getBody();
    }

    private Optional<Action> findByActionAndRouteAndLocationGroup(String actionType, Route route, String locationGroupName) {
        return repository.findByActionTypeAndRouteAndLocationGroupName(actionType, route.getRouteId(), locationGroupName);
    }
}
