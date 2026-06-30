package com.logistics.rag.infrastructure.persistence;

import com.pgvector.PGvector;
import org.springframework.jdbc.datasource.DelegatingDataSource;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

/**
 * Registers pgvector's {@code vector} type on every connection obtained from the pool.
 * Type registration is per-{@link Connection}, not per-{@link DataSource}, so it must be
 * repeated on each checkout from a pooled DataSource (e.g. HikariCP) rather than once at startup.
 */
class VectorAwareDataSource extends DelegatingDataSource {

    VectorAwareDataSource(DataSource targetDataSource) {
        super(targetDataSource);
    }

    @Override
    public Connection getConnection() throws SQLException {
        Connection connection = super.getConnection();
        PGvector.addVectorType(connection);
        return connection;
    }

    @Override
    public Connection getConnection(String username, String password) throws SQLException {
        Connection connection = super.getConnection(username, password);
        PGvector.addVectorType(connection);
        return connection;
    }
}
