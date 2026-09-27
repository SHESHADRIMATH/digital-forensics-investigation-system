package com.digitalforensics.dao;

import com.digitalforensics.exception.DataAccessException;
import com.digitalforensics.util.DBConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Shared JDBC plumbing for every DAO (Data Access Object).
 *
 * Every query uses a PreparedStatement with "?" placeholders. User input is
 * sent separately from the SQL text, so it can never change the query -
 * this is what prevents SQL injection.
 */
public abstract class BaseDAO {

    /** Converts the current ResultSet row into an object. Implemented with lambdas in each DAO. */
    @FunctionalInterface
    protected interface RowMapper<T> {
        T map(ResultSet rs) throws SQLException;
    }

    protected <T> List<T> queryList(String sql, RowMapper<T> mapper, Object... params) {
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = prepare(con, sql, false, params);
             ResultSet rs = ps.executeQuery()) {
            List<T> rows = new ArrayList<>();
            while (rs.next()) {
                rows.add(mapper.map(rs));
            }
            return rows;
        } catch (SQLException e) {
            throw new DataAccessException("Query failed: " + e.getMessage(), e);
        }
    }

    protected <T> T queryOne(String sql, RowMapper<T> mapper, Object... params) {
        List<T> rows = queryList(sql, mapper, params);
        return rows.isEmpty() ? null : rows.get(0);
    }

    protected int queryInt(String sql, Object... params) {
        Integer value = queryOne(sql, rs -> rs.getInt(1), params);
        return value == null ? 0 : value;
    }

    /** INSERT/UPDATE/DELETE. Returns number of rows changed. */
    protected int update(String sql, Object... params) {
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = prepare(con, sql, false, params)) {
            return ps.executeUpdate();
        } catch (SQLException e) {
            throw new DataAccessException("Update failed: " + e.getMessage(), e);
        }
    }

    /** INSERT that returns the new AUTO_INCREMENT id. */
    protected int insert(String sql, Object... params) {
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = prepare(con, sql, true, params)) {
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                return keys.next() ? keys.getInt(1) : 0;
            }
        } catch (SQLException e) {
            throw new DataAccessException("Insert failed: " + e.getMessage(), e);
        }
    }

    private PreparedStatement prepare(Connection con, String sql, boolean returnKeys, Object... params)
            throws SQLException {
        PreparedStatement ps = returnKeys
                ? con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)
                : con.prepareStatement(sql);
        for (int i = 0; i < params.length; i++) {
            Object p = params[i];
            // enums are stored by name, e.g. Priority.HIGH -> 'HIGH'
            ps.setObject(i + 1, p instanceof Enum<?> e ? e.name() : p);
        }
        return ps;
    }

    // ---- small helpers used by the row mappers ----

    protected static LocalDateTime time(ResultSet rs, String column) throws SQLException {
        return rs.getObject(column, LocalDateTime.class);
    }

    /** Reads a nullable INT column as Integer (null instead of 0). */
    protected static Integer nullableInt(ResultSet rs, String column) throws SQLException {
        int value = rs.getInt(column);
        return rs.wasNull() ? null : value;
    }
}
