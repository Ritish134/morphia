package dev.morphia.test.aggregation.expressions;

import dev.morphia.test.aggregation.AggregationTest;

import org.testng.annotations.Test;

import static dev.morphia.aggregation.expressions.AccumulatorExpressions.bottom;
import static dev.morphia.aggregation.expressions.ArrayExpressions.array;
import static dev.morphia.aggregation.expressions.Expressions.field;
import static dev.morphia.aggregation.stages.Group.group;
import static dev.morphia.aggregation.stages.Group.id;
import static dev.morphia.query.Sort.descending;
import static dev.morphia.query.filters.Filters.eq;
import static dev.morphia.test.ServerVersion.v52;

public class TestBottom extends AggregationTest {
    @Test
    public void testSingleGame() {
        testPipeline(v52, "singleGame", false, false, (aggregation) -> {
            return aggregation
                    .match(eq("gameId", "G1"))
                    .group(group(id(field("gameId")))
                            .field("playerId", bottom(
                                    array(field("playerId"), field("score")),
                                    descending("score"))));
        });
    }

    @Test
    public void testAcrossGames() {
        testPipeline(v52, "acrossGames", false, false, (aggregation) -> {
            return aggregation
                    .group(group(id(field("gameId")))
                            .field("playerId", bottom(
                                    array(field("playerId"), field("score")),
                                    descending("score"))));
        });
    }
}
