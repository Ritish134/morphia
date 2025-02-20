package dev.morphia.test.aggregation.expressions;

import java.time.LocalDate;

import dev.morphia.aggregation.expressions.impls.Expression;
import dev.morphia.aggregation.stages.Projection;
import dev.morphia.mapping.codec.writer.DocumentWriter;
import dev.morphia.test.TemplatedTestBase;
import dev.morphia.test.models.User;

import org.bson.Document;
import org.bson.codecs.Codec;
import org.bson.codecs.EncoderContext;
import org.testng.annotations.BeforeMethod;

import static dev.morphia.mapping.codec.CodecHelper.document;
import static org.testng.Assert.assertEquals;

public class ExpressionsTestBase extends TemplatedTestBase {
    @BeforeMethod
    public void seed() {
        getDs().getCollection(User.class).drop();
        getDs().save(new User("", LocalDate.now()));
    }

    @SuppressWarnings("unchecked")
    protected void assertAndCheckDocShape(String expectedString, Expression value, Object expectedValue) {
        Document expected = Document.parse(expectedString);
        DocumentWriter writer = new DocumentWriter(getMapper().getConfig());
        document(writer, () -> {
            Codec codec = getDs().getCodecRegistry().get(value.getClass());
            codec.encode(writer, value, EncoderContext.builder().build());
        });

        Document actual = writer.getDocument();
        assertDocumentEquals(actual, expected);

        Document test = getDs().aggregate(User.class)
                .project(Projection.project()
                        .include("test", value))
                .execute(Document.class)
                .next();
        assertEquals(test.get("test"), expectedValue);
    }
}
