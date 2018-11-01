/*
 * openwms.org, the Open Warehouse Management System.
 * Copyright (C) 2018 Heiko Scherrer
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
package org.openwms.tms.routing;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * A ExplicitRouteSearchIT.
 *
 * @author <a href="mailto:scherrer@openwms.org">Heiko Scherrer</a>
 */
@RunWith(SpringRunner.class)
@DataJpaTest
@EnableTransactionManagement
@EnableJpaRepositories(basePackages = "org.openwms")
@EntityScan(basePackages = "org.openwms")
@ActiveProfiles("SIMPLE")
public class ExplicitRouteSearchIT {

    @Autowired
    private ExplicitRouteSearch testee;


    public @Test
    void shall_fail_when_no_source_is_given() {
        try {
            testee.findBy(null, "IPUNKT", "LAGER");
            fail("Should fail because no source is given");
        } catch (IllegalArgumentException iae) {
            assertTrue(iae.getMessage().contains("sourceLocation"));
            // ok
        }
    }

    public @Test
    void shall_fail_when_no_target_is_given() {
        try {
            testee.findBy("STCK/0001/0001/0000/0000", null, null);
            fail("Should fail because no target is given at all");
        } catch (IllegalArgumentException iae) {
            assertTrue(iae.getMessage().contains("target"));
            // ok
        }
    }

    public @Test
    void shall_fail_when_no_target_is_given3() {
        try {
            testee.findBy("STCK/0001/0001/0000/0000", " ", null);
            fail("Should fail because no target is given");
        } catch (IllegalArgumentException iae) {
            assertTrue(iae.getMessage().contains("target"));
            // ok
        }
    }

    public @Test
    void shall_fail_when_no_target_is_given4() {
        try {
            testee.findBy("STCK/0001/0001/0000/0000", null, " ");
            fail("Should fail because no target is given");
        } catch (IllegalArgumentException iae) {
            assertTrue(iae.getMessage().contains("targetLocation"));
            // ok
        }
    }

    public @Test
    void shall_fail_with_invalid_target() {
        try {
            testee.findBy("STCK/0001/0001/0000/0000", "", "STOCK");
            fail("Should fail because no route exists");
        } catch (NoRouteException iae) {
            assertTrue(iae.getMessage().contains("No route"));
            // ok
        }
    }

    public @Test
    void shall_pass_when_at_least_one_target_is_given() {
        Route result = testee.findBy("STCK/0001/0001/0000/0000", "STCK/0001/0002/0000/0000", null);
        Assert.assertNotEquals("", result.getRouteId());
    }

    public @Test
    void shall_pass_when_at_least_one_target_is_given2() {
        Route result = testee.findBy("STCK/0001/0001/0000/0000", null, "FGRECEIVING");
        Assert.assertEquals("REC_CONV", result.getRouteId());
    }

    public @Test
    void shall_pass_when_all_targets_are_given() {
        Route result = testee.findBy("STCK/0001/0001/0000/0000", "STCK/0001/0002/0000/0000", "FGRECEIVING");
        Assert.assertEquals("SRC_TRG", result.getRouteId());
    }

    @TestConfiguration
    static class TestConfig {
        @Bean
        ExplicitRouteSearch explicitRouteSearch(RouteRepository routeRepository) {
            return new ExplicitRouteSearch(routeRepository);
        }
    }
}
