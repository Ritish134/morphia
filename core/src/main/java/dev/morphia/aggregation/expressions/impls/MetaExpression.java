package dev.morphia.aggregation.expressions.impls;

import dev.morphia.annotations.internal.MorphiaInternal;

/**
 * @hidden
 * @morphia.internal
 */
@MorphiaInternal
public class MetaExpression extends Expression {

    public MetaExpression() {
        super("$meta");
    }
}
