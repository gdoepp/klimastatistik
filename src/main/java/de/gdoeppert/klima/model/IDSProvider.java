package de.gdoeppert.klima.model;

import java.sql.Connection;
import java.sql.SQLException;

import javax.sql.DataSource;

public interface IDSProvider {

    DataSource getDS();

    Connection getConnection() throws SQLException;
}
