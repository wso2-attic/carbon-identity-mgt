/*
 * Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.identity.mgt.impl.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Support class to implement Unit of work patter.
 */
public class UnitOfWork implements AutoCloseable {

    private static Logger log = LoggerFactory.getLogger(UnitOfWork.class);

    private Connection connection = null;
    private List<AutoCloseable> listToClose = new ArrayList<>();
    private AtomicInteger transactionLevel = new AtomicInteger(0);

    private UnitOfWork() throws SQLException {
        super();
    }

    /**
     * Begin the transaction process.
     *
     * @param connection Database connection.
     * @param autoCommit Set auto commit status of this transaction.
     * @return Instance of @see UnitOfWork.
     * @throws SQLException SQL Exception.
     */
    public static UnitOfWork beginTransaction(Connection connection, boolean autoCommit) throws SQLException {

        connection.setAutoCommit(autoCommit);
        return beginTransaction(connection);
    }

    /**
     * Begin the transaction process.
     *
     * @param connection Database connection
     * @return Instance of UnitOfWork
     * @throws SQLException SQL Exception.
     */
    public static UnitOfWork beginTransaction(Connection connection) throws SQLException {

        UnitOfWork unitOfWork = new UnitOfWork();
        unitOfWork.connection = connection;
        unitOfWork.transactionLevel.incrementAndGet();

        return unitOfWork;
    }

    /**
     * Queue any auto closable to close at the end.
     *
     * @param closeable Auto closable to be closed.
     */
    public void queueToClose(AutoCloseable closeable) {
        listToClose.add(closeable);
    }

    /**
     * End the transaction by committing to the database.
     *
     * @throws SQLException SQL Exception.
     */
    public void endTransaction() throws SQLException {
        transactionLevel.decrementAndGet();
        if (transactionLevel.get() <= 0) {
            connection.commit();
        }
    }

    /**
     * Revoke the transaction when catch then sql transaction errors.
     *
     * @param connection Database connection.
     */
    private void rollbackTransaction(Connection connection) {

        try {
            if (connection != null) {
                connection.rollback();
            }
        } catch (SQLException e1) {
            log.error("An error occurred while rolling back transactions. ", e1);
        }
    }

    /**
     * Get the underlying connection object.
     *
     * @return instance of Connection.
     */
    public Connection getConnection() {
        return connection;
    }

    /**
     * Commit and close connection.
     *
     * @throws SQLException SQL Exception.
     */
    @Override
    public void close() throws SQLException {

        SQLException exception = null;

        if (transactionLevel.get() > 0) {
            rollbackTransaction(connection);
            log.warn("The database connection is being closed without properly committing. Hence rollback any "
                    + "transaction. Connection Information  " + connection.getMetaData().getURL());
            transactionLevel.set(0);
        }
        for (AutoCloseable closeable : listToClose) {
            try {
                closeable.close();
            } catch (Exception e) {
                if (exception == null) {
                    exception = new SQLException(e);
                    log.debug("Exception occurred while closing the closable.", e);
                } else {
                    exception.addSuppressed(e);
                    log.debug("Exception occurred and suppressed while closing the closable.", e);
                }
            }
        }

        connection.close();

        if (exception != null) {
            throw exception;
        }
    }
}
