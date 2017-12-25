/*
 *
 *  * Copyright (C) 2017 Allegiant Travel Company - All Rights Reserved
 *  *
 *  * This file is subject to the terms and conditions defined in
 *  * file 'LICENSE.txt', which is part of this source code package.
 *
 *
 */

package com.allegianttravel.ota.flight.shop.g4.lookup;

import org.apache.commons.lang3.tuple.Pair;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;

@RunWith(Parameterized.class)
public class G4NamedFareDetailsLookupTest {

    private final String input;
    private final List<Pair<String,Integer>> expected;

    public G4NamedFareDetailsLookupTest(@SuppressWarnings("unused") String name,
                                        String input, List<Pair<String,Integer>> expected) {
        this.input = input;
        this.expected = expected;
    }

    @SuppressWarnings("unchecked")
    @Parameterized.Parameters(name = "{0}")
    public static List<Object[]> params() throws Exception {
        List<Pair<String, Integer>> expectedList = list(Pair.of("STD", 1), Pair.of("FLEX", 2));
        return Arrays.asList(
                new Object[] {"missing property", null, expectedList},
                new Object[] {"empty property", "", expectedList},
                new Object[] {"effectively empty property", "    ", expectedList},
                new Object[] {"order flipped", "FLEX=2,STD=1", expectedList},
                new Object[] {"well formed", "STD=1,FLEX=2", expectedList},
                new Object[] {"extra spaces", "  STD = 1 , FLEX= 2 ", expectedList},
                new Object[] {"dupe", "STD=0,FLEX=2,STD=1", expectedList},
                new Object[] {"missing comma", "STD=1 FLEX= 2", expectedList},
                new Object[] {"number format", "STD=1,FLEX=abc", list(Pair.of("STD", 1))},
                new Object[] {"single", "STD=1", list(Pair.of("STD", 1))},
                new Object[] {"missing STD", "FLEX=2,BUNDLE=3", list(Pair.of("FLEX", 2), Pair.of("BUNDLE", 3))}
        );
    }


    @Test
    public void test() throws Exception {
        Map<String, Integer> actualMap = G4NamedFareDetailsLookup.toMap(input);
        expected.forEach(pair -> assertEquals("failed to match:" + pair,
                pair.getValue().intValue(),
                G4NamedFareDetailsLookup.toSwimlane(actualMap, pair.getKey())));
    }

    @SuppressWarnings("unchecked")
    private static List<Pair<String, Integer>> list(Pair<String, Integer>...pairs) {
        return Arrays.asList(pairs);
    }
}
