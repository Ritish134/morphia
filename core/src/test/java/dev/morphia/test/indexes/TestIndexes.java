package dev.morphia.test.indexes;

import com.mongodb.MongoCommandException;
import com.mongodb.client.model.CollationCaseFirst;
import com.mongodb.client.model.CollationMaxVariable;
import com.mongodb.client.model.CollationStrength;
import dev.morphia.Datastore;
import dev.morphia.annotations.Collation;
import dev.morphia.annotations.Entity;
import dev.morphia.annotations.Field;
import dev.morphia.annotations.Id;
import dev.morphia.annotations.Index;
import dev.morphia.annotations.IndexOptions;
import dev.morphia.annotations.Indexed;
import dev.morphia.annotations.Indexes;
import dev.morphia.annotations.Property;
import dev.morphia.annotations.Text;
import dev.morphia.mapping.MapperOptions;
import dev.morphia.mapping.MapperOptions.PropertyDiscovery;
import dev.morphia.test.TestBase;
import dev.morphia.test.models.methods.MethodMappedUser;
import dev.morphia.utils.IndexType;
import org.bson.Document;
import org.bson.types.ObjectId;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.Date;
import java.util.List;

import static com.mongodb.client.model.CollationAlternate.SHIFTED;
import static dev.morphia.utils.IndexType.DESC;
import static dev.morphia.utils.IndexType.TEXT;
import static org.bson.Document.parse;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;

public class TestIndexes extends TestBase {
    @Test(expectedExceptions = MongoCommandException.class)
    public void shouldNotAllowMultipleTextIndexes() {
        getMapper().map(MultipleTextIndexes.class);
        getDs().ensureIndexes();
    }

    @Test
    public void testExpireAfterClassAnnotation() {
        getMapper().map(ClassAnnotation.class);
        getDs().ensureIndexes(ClassAnnotation.class);

        getDs().save(new ClassAnnotation());

        final List<Document> indexes = getIndexInfo(ClassAnnotation.class);
        assertEquals(indexes.size(), 2);
        Document index = null;
        for (Document candidateIndex : indexes) {
            if (candidateIndex.containsKey("expireAfterSeconds")) {
                index = candidateIndex;
            }
        }
        assertNotNull(index);
        assertTrue(index.containsKey("expireAfterSeconds"));
        assertEquals(((Number) index.get("expireAfterSeconds")).intValue(), 5);
    }

    @Test
    public void testIndexedField() {
        getMapper().map(HasExpiryField.class);
        getDs().ensureIndexes(HasExpiryField.class);

        getDs().save(new HasExpiryField());

        final List<Document> indexes = getIndexInfo(HasExpiryField.class);

        assertNotNull(indexes);
        assertEquals(indexes.size(), 2);
        Document index = null;
        for (Document candidateIndex : indexes) {
            if (candidateIndex.containsKey("expireAfterSeconds")) {
                index = candidateIndex;
            }
        }
        assertNotNull(index);
        assertEquals(((Number) index.get("expireAfterSeconds")).intValue(), 5);
    }

    @Test
    public void testIndexes() {
        final Datastore datastore = getDs();
        datastore.ensureIndexes(TestWithIndexOption.class);
        List<Document> indexInfo = getIndexInfo(TestWithIndexOption.class);
        assertEquals(indexInfo.size(), 2);
        assertBackground(indexInfo);
        for (Document document : indexInfo) {
            if (document.get("name").equals("collated")) {
                assertEquals(document.get("partialFilterExpression"),
                    parse("{ name : { $exists : true } }"));
                Document collation = (Document) document.get("collation");
                collation.remove("version");

                Document parse = parse("{ 'locale': 'en_US', "
                                       + "'alternate': 'shifted',"
                                       + "'backwards': true,"
                                       + "'caseFirst': 'upper',"
                                       + "'caseLevel': true,"
                                       + "'maxVariable': 'space',"
                                       + "'normalization': true,"
                                       + "'numericOrdering': true,"
                                       + "'strength': 5 }");
                assertEquals(collation, parse, collation.toJson());
            }
        }

        datastore.ensureIndexes(TestWithDeprecatedIndex.class);
        assertEquals(getIndexInfo(TestWithDeprecatedIndex.class).size(), 2);
        assertBackground(getIndexInfo(TestWithDeprecatedIndex.class));

        datastore.ensureIndexes(TestWithHashedIndex.class);
        assertEquals(getIndexInfo(TestWithHashedIndex.class).size(), 2);
        assertHashed(getIndexInfo(TestWithHashedIndex.class));
    }

    @Test
    public void testMethodMapping() {
        withOptions(MapperOptions.builder().propertyDiscovery(PropertyDiscovery.METHODS).build(),
            () -> {
                getMapper().map(MethodMappedUser.class);
                getDs().ensureIndexes(MethodMappedUser.class);
                assertEquals(getIndexInfo(MethodMappedUser.class).size(), 3);
            });
    }

    @Test
    public void testSingleAnnotation() {
        getMapper().map(CompoundTextIndex.class);
        getDs().ensureIndexes();

        List<Document> indexInfo = getIndexInfo(CompoundTextIndex.class);
        Assert.assertEquals(indexInfo.size(), 2);
        boolean found = false;
        for (Document document : indexInfo) {
            if (document.get("name").equals("indexing_test")) {
                found = true;
                Assert.assertEquals(document.get("default_language"), "russian", document.toString());
                Assert.assertEquals(document.get("language_override"), "nativeTongue", document.toString());
                Assert.assertEquals(((Document) document.get("weights")).get("name"), 1, document.toString());
                Assert.assertEquals(((Document) document.get("weights")).get("nick"), 10, document.toString());
                Assert.assertEquals(((Document) document.get("key")).get("age"), 1, document.toString());
            }
        }
        Assert.assertTrue(found);
    }

    @Test
    public void testTextAnnotation() {
        Class<SingleFieldTextIndex> clazz = SingleFieldTextIndex.class;

        getMapper().map(clazz);
        getDs().ensureIndexes();

        List<Document> indexInfo = getIndexInfo(clazz);
        Assert.assertEquals(indexInfo.size(), 2, indexInfo.toString());
        boolean found = false;
        for (Document document : indexInfo) {
            if (document.get("name").equals("single_annotation")) {
                found = true;
                Assert.assertEquals(document.get("default_language"), "english", document.toString());
                Assert.assertEquals(document.get("language_override"), "nativeTongue", document.toString());
                Assert.assertEquals(((Document) document.get("weights")).get("nickName"), 10, document.toString());
            }
        }
        Assert.assertTrue(found, indexInfo.toString());

    }

    private void assertBackground(List<Document> indexInfo) {
        for (Document document : indexInfo) {
            if (!document.getString("name").equals("_id_")) {
                assertTrue(document.getBoolean("background"));
            }
        }
    }

    private void assertHashed(List<Document> indexInfo) {
        for (Document document : indexInfo) {
            if (!document.getString("name").equals("_id_")) {
                assertEquals(((Document) document.get("key")).get("hashedValue"), "hashed");
            }
        }
    }

    @Entity
    @Indexes(@Index(fields = @Field("offerExpiresAt"), options = @IndexOptions(expireAfterSeconds = 5)))
    private static class ClassAnnotation {
        private final Date offerExpiresAt = new Date();
        @Id
        private ObjectId id;
    }

    @Entity
    @Indexes(@Index(fields = {@Field(value = "name", type = TEXT),
                              @Field(value = "nick", type = TEXT, weight = 10),
                              @Field(value = "age")}, options = @IndexOptions(name = "indexing_test", language = "russian",
        languageOverride = "nativeTongue")))
    private static class CompoundTextIndex {
        @Id
        private ObjectId id;
        private String name;
        private Integer age;
        @Property("nick")
        private String nickName;
        private String nativeTongue;
    }

    @Entity
    @Indexes(@Index(fields = {@Field("actor.actorObject.userId"), @Field(value = "actor.actorType", type = DESC)},
        options = @IndexOptions(disableValidation = true,
            partialFilter = "{ 'actor.actorObject.userId': { $exists: true }, 'actor.actorType': { $exists: true } }")))
    private static class FeedEvent {
        @Id
        private ObjectId id;
    }

    @Entity
    private static class HasExpiryField {
        @Indexed(options = @IndexOptions(expireAfterSeconds = 5))
        private final Date offerExpiresAt = new Date();
        @Id
        private ObjectId id;
    }

    @Entity
    private static class InboxEvent {
        @Id
        private ObjectId id;
        private FeedEvent feedEvent;
    }

    @Entity
    @Indexes({@Index(fields = @Field(value = "name", type = TEXT)),
              @Index(fields = @Field(value = "nickName", type = TEXT))})
    private static class MultipleTextIndexes {
        @Id
        private ObjectId id;
        private String name;
        private String nickName;
    }

    @Entity
    private static class SingleFieldTextIndex {
        @Id
        private ObjectId id;
        private String name;
        @Text(value = 10, options = @IndexOptions(name = "single_annotation", languageOverride = "nativeTongue"))
        private String nickName;

    }

    @Entity(useDiscriminator = false)
    @Indexes({@Index(options = @IndexOptions(background = true),
        fields = @Field("name"))})
    private static class TestWithDeprecatedIndex {
        @Id
        private ObjectId id;
        private String name;

    }

    @Entity(useDiscriminator = false)
    @Indexes({@Index(options = @IndexOptions(), fields = {@Field(value = "hashedValue", type = IndexType.HASHED)})})
    private static class TestWithHashedIndex {
        @Id
        private ObjectId id;
        private String hashedValue;

    }

    @Entity(useDiscriminator = false)
    @Indexes({@Index(options = @IndexOptions(name = "collated",
        partialFilter = "{ name : { $exists : true } }",
        collation = @Collation(locale = "en_US", alternate = SHIFTED, backwards = true,
            caseFirst = CollationCaseFirst.UPPER, caseLevel = true, maxVariable = CollationMaxVariable.SPACE, normalization = true,
            numericOrdering = true, strength = CollationStrength.IDENTICAL),
        background = true),
        fields = {@Field(value = "name")})})
    private static class TestWithIndexOption {
        @Id
        private ObjectId id;
        private String name;

    }

}