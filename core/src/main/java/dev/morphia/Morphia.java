package dev.morphia;

import com.mongodb.MongoClientSettings;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;

import dev.morphia.config.MorphiaConfig;
import dev.morphia.mapping.MapperOptions;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static dev.morphia.config.MorphiaConfigHelper.*;

/**
 * Entry point for working with Morphia
 */
public final class Morphia {

    private Morphia() {
    }

    /**
     * Creates a Datastore configured via config file
     *
     * @param mongoClient the client to use
     * @return a Datastore that you can use to interact with MongoDB
     * @since 2.4.0
     */
    public static Datastore createDatastore(MongoClient mongoClient) {
        return new DatastoreImpl(mongoClient, MorphiaConfig.load());
    }

    /**
     * Creates a Datastore configured via config file
     *
     * @param mongoClient the client to use
     * @param config      the configuration to use
     * @return a Datastore that you can use to interact with MongoDB
     * @since 3.0.0
     */
    public static Datastore createDatastore(MongoClient mongoClient, MorphiaConfig config) {
        return new DatastoreImpl(mongoClient, config);
    }

}
