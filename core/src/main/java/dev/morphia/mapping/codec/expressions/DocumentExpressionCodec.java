package dev.morphia.mapping.codec.expressions;

import dev.morphia.MorphiaDatastore;
import dev.morphia.aggregation.expressions.impls.DocumentExpression;
import dev.morphia.aggregation.expressions.impls.Expression;
import dev.morphia.aggregation.expressions.impls.Fields;
import dev.morphia.aggregation.expressions.impls.PipelineField;
import org.bson.BsonWriter;
import org.bson.codecs.Codec;
import org.bson.codecs.EncoderContext;

import java.util.List;

import static dev.morphia.aggregation.codecs.ExpressionHelper.document;
import static dev.morphia.mapping.codec.expressions.ExpressionCodecHelper.encodeIfNotNull;

public class DocumentExpressionCodec extends BaseExpressionCodec<DocumentExpression> {
    public DocumentExpressionCodec(MorphiaDatastore datastore) {
        super(datastore);
    }

    @Override
    @SuppressWarnings({"rawtypes", "unchecked"})
    public void encode(BsonWriter writer, DocumentExpression value, EncoderContext encoderContext) {
        document(writer, () -> {
            for (PipelineField field : value.fields().getFields()) {
                encodeIfNotNull(datastore.getCodecRegistry(), writer, field.getName(), field.getValue(), encoderContext);
            }
        });
    }

    @Override
    public Class<DocumentExpression> getEncoderClass() {
        return DocumentExpression.class;
    }
}