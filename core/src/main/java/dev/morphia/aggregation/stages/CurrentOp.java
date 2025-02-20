package dev.morphia.aggregation.stages;

import dev.morphia.annotations.internal.MorphiaInternal;

/**
 * Returns a stream of documents containing information on active and/or dormant operations as well as inactive sessions that are
 * holding locks as part of a transaction.
 *
 * @aggregation.expression $currentOp
 */
public class CurrentOp extends Stage {
    private boolean allUsers;
    private boolean idleConnections;
    private boolean idleCursors;
    private boolean idleSessions;
    private boolean localOps;

    /**
     * @hidden
     * @morphia.internal
     */
    @MorphiaInternal
    protected CurrentOp() {
        super("$currentOp");
    }

    /**
     * Creates a new stage
     *
     * @return the new stage
     * @since 2.2
     */
    public static CurrentOp currentOp() {
        return new CurrentOp();
    }

    /**
     * <ol>
     * <li>If set to false, $currentOp will only report on operations/idle connections/idle cursors/idle sessions belonging to the user who
     * ran the command.
     * <li>If set to true, $currentOp will report operations belonging to all users.
     * </ol>
     *
     * @param allUsers include allUsers if true
     * @return this
     */
    public CurrentOp allUsers(boolean allUsers) {
        this.allUsers = allUsers;
        return this;
    }

    /**
     * If set to false, $currentOp will only report active operations. If set to true, all operations including idle connections will
     * be returned.
     *
     * @param idleConnections include idle connections if true
     * @return this
     */
    public CurrentOp idleConnections(boolean idleConnections) {
        this.idleConnections = idleConnections;
        return this;
    }

    /**
     * If set to true, $currentOp will report on cursors that are “idle”; i.e. open but not currently active in a getMore operation.
     *
     * @param idleCursors include idle cursors if true
     * @return this
     */
    public CurrentOp idleCursors(boolean idleCursors) {
        this.idleCursors = idleCursors;
        return this;
    }

    /**
     * Include idle sessions or not
     *
     * @param idleSessions true to include idle sessions
     * @return this
     */
    public CurrentOp idleSessions(boolean idleSessions) {
        this.idleSessions = idleSessions;
        return this;
    }

    /**
     * @return include all users?
     * @hidden
     * @morphia.internal
     */
    @MorphiaInternal
    public boolean isAllUsers() {
        return allUsers;
    }

    /**
     * @return include idle connections?
     * @hidden
     * @morphia.internal
     */
    @MorphiaInternal
    public boolean isIdleConnections() {
        return idleConnections;
    }

    /**
     * @return include idle cursors?
     * @hidden
     * @morphia.internal
     */
    @MorphiaInternal
    public boolean isIdleCursors() {
        return idleCursors;
    }

    /**
     * @return include idle sessions?
     * @hidden
     * @morphia.internal
     */
    @MorphiaInternal
    public boolean isIdleSessions() {
        return idleSessions;
    }

    /**
     * @return is local ops?
     * @hidden
     * @morphia.internal
     */
    @MorphiaInternal
    public boolean isLocalOps() {
        return localOps;
    }

    /**
     * If set to true for an aggregation running on mongos, $currentOp reports only those operations running locally on that mongos. If
     * false, then the $currentOp will instead report operations running on the shards.
     *
     * @param localOps true to include only local ops
     * @return this
     */
    public CurrentOp localOps(boolean localOps) {
        this.localOps = localOps;
        return this;
    }
}
