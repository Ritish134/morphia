package dev.morphia.query;

import com.mongodb.client.ClientSession;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.result.UpdateResult;
import dev.morphia.Datastore;
import dev.morphia.UpdateOptions;
import dev.morphia.internal.PathTarget;
import dev.morphia.query.experimental.updates.UpdateOperator;
import org.bson.Document;

import java.util.List;

/**
 * Defines an update operation
 *
 * @param <T>
 */
public class Update<T> extends UpdateBase<T, UpdateOperator> {
    @SuppressWarnings("rawtypes")
    Update(Datastore datastore, MongoCollection<T> collection,
           Query<T> query, Class<T> type, UpdateOpsImpl operations) {
        super(datastore, collection, query, type, operations.getUpdates());
    }

    Update(Datastore datastore, MongoCollection<T> collection,
           Query<T> query, Class<T> type, List<UpdateOperator> updates) {
        super(datastore, collection, query, type, updates);
    }

    @Override
    public UpdateResult execute(UpdateOptions options) {
        Document updateOperations = toDocument();
        final Document queryObject = getQuery().toDocument();

        ClientSession session = getDatastore().findSession(options);
        MongoCollection<T> mongoCollection = options.prepare(getCollection());
        if (options.isMulti()) {
            return session == null ? mongoCollection.updateMany(queryObject, updateOperations, options)
                                   : mongoCollection.updateMany(session, queryObject, updateOperations, options);

        } else {
            return session == null ? mongoCollection.updateOne(queryObject, updateOperations, options)
                                   : mongoCollection.updateOne(session, queryObject, updateOperations, options);
        }
    }

    private Document toDocument() {
        final Operations operations = new Operations(getDatastore(), getMapper().getEntityModel(getType()));

        for (UpdateOperator update : getUpdates()) {
            PathTarget pathTarget = new PathTarget(getMapper(), getMapper().getEntityModel(getType()), update.field(), true);
            operations.add(update.operator(), update.toTarget(pathTarget));
        }
        return operations.toDocument();
    }
}
