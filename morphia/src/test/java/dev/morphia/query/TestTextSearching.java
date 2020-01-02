package dev.morphia.query;

import dev.morphia.annotations.Entity;
import dev.morphia.mapping.Mapper;
import org.bson.types.ObjectId;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import dev.morphia.TestBase;
import dev.morphia.annotations.Field;
import dev.morphia.annotations.Id;
import dev.morphia.annotations.Index;
import dev.morphia.annotations.Indexes;
import dev.morphia.utils.IndexType;

import java.util.List;

import static dev.morphia.query.Sort.*;
import static java.util.Arrays.asList;

@Ignore("text searches need work")
public class TestTextSearching extends TestBase {
    @Test
    public void testTextSearch() {
        getMapper().map(Greeting.class);
        getDs().ensureIndexes();

        getDs().save(new Greeting("good morning", "english"));
        getDs().save(new Greeting("good afternoon", "english"));
        getDs().save(new Greeting("good night", "english"));
        getDs().save(new Greeting("good riddance", "english"));

        getDs().save(new Greeting("guten Morgen", "german"));
        getDs().save(new Greeting("guten Tag", "german"));
        getDs().save(new Greeting("gute Nacht", "german"));

        getDs().save(new Greeting("buenos días", "spanish"));
        getDs().save(new Greeting("buenas tardes", "spanish"));
        getDs().save(new Greeting("buenos noches", "spanish"));

        List<Greeting> good = getDs().find(Greeting.class)
                                     .search("good")
                                     .order(ascending("_id"))
                                     .execute().toList();
        Assert.assertEquals(4, good.size());
        Assert.assertEquals("good morning", good.get(0).value);
        Assert.assertEquals("good afternoon", good.get(1).value);
        Assert.assertEquals("good night", good.get(2).value);
        Assert.assertEquals("good riddance", good.get(3).value);

        good = getDs().find(Greeting.class)
                      .search("good", "english")
                      .order(ascending("_id"))
                      .execute().toList();
        Assert.assertEquals(4, good.size());
        Assert.assertEquals("good morning", good.get(0).value);
        Assert.assertEquals("good afternoon", good.get(1).value);
        Assert.assertEquals("good night", good.get(2).value);
        Assert.assertEquals("good riddance", good.get(3).value);

        Assert.assertEquals(1, getDs().find(Greeting.class)
                                      .search("riddance")
                                      .execute().toList().size());
        Assert.assertEquals(1, getDs().find(Greeting.class)
                                      .search("noches", "spanish")
                                      .execute().toList().size());
        Assert.assertEquals(1, getDs().find(Greeting.class)
                                      .search("Tag")
                                      .execute().toList().size());
    }

    @Test
    public void testTextSearchSorting() {
        getMapper().map(Book.class);
        getDs().ensureIndexes();

        getDs().save(asList(new Book("The Banquet", "Dante"),
                            new Book("Divine Comedy", "Dante"),
                            new Book("Eclogues", "Dante"),
                            new Book("The Odyssey", "Homer"),
                            new Book("Iliad", "Homer")));

        List<Book> books = getDs().find(Book.class)
                                  .search("Dante Comedy")
                                  .order(Meta.textScore("score"))
                                  .execute(new FindOptions()
                                          .projection().project(Meta.textScore("score")))
                                  .toList();
        Assert.assertEquals(3, books.size());
        Assert.assertEquals("Divine Comedy", books.get(0).title);
    }

    @Test
    public void testTextSearchValidationFailed() {
        getMapper().map(Book.class);
        getDs().ensureIndexes();

        getDs().save(asList(new Book("The Banquet", "Dante"),
                            new Book("Divine Comedy", "Dante"),
                            new Book("Eclogues", "Dante"),
                            new Book("The Odyssey", "Homer"),
                            new Book("Iliad", "Homer")));

        List<Book> books = getDs().find(Book.class)
                                  .search("Dante")
                                  .order(Meta.textScore())
                                  .execute(new FindOptions()
                                               .projection().project(Meta.textScore()))
                                  .toList();
        Assert.assertEquals(3, books.size());
        Assert.assertEquals("Dante", books.get(0).author);
    }

    @Test
    public void testTextSearchWithMeta() {
        getMapper().map(Book.class);
        getDs().ensureIndexes();

        getDs().save(asList(new Book("The Banquet", "Dante"),
                            new Book("Divine Comedy", "Dante"),
                            new Book("Eclogues", "Dante"),
                            new Book("The Odyssey", "Homer"),
                            new Book("Iliad", "Homer")));

        List<Book> books = getDs().find(Book.class)
                                  .search("Dante")
                                  .order(Meta.textScore("score"))
                                  .execute(new FindOptions()
                                               .projection().project(Meta.textScore("score")))
                                  .toList();
        Assert.assertEquals(3, books.size());
        for (Book book : books) {
            Assert.assertEquals("Dante", book.author);
        }
    }

    @Entity
    @Indexes(@Index(fields = @Field(value = "$**", type = IndexType.TEXT)))
    private static class Greeting {
        @Id
        private ObjectId id;
        private String value;
        private String language;

        Greeting() {
        }

        private Greeting(final String value, final String language) {
            this.language = language;
            this.value = value;
        }
    }

    @Entity
    @Indexes(@Index(fields = @Field(value = "$**", type = IndexType.TEXT)))
    private static class Book {
        @Id
        private ObjectId id;
        private String title;
        private String author;

        Book() {
        }

        private Book(final String title, final String author) {
            this.author = author;
            this.title = title;
        }
    }
}