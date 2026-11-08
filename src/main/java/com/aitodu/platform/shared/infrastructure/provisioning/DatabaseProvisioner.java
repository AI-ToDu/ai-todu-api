package com.aitodu.platform.shared.infrastructure.provisioning;

import java.net.URI;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.sql.*;

/** Explicit, one-time provisioning of an isolated database on an existing PostgreSQL instance. */
public final class DatabaseProvisioner {
  private DatabaseProvisioner() {}

  public static void runIfRequested() throws SQLException {
    String adminUrl = System.getenv("BOOTSTRAP_DATABASE_URL");
    if (adminUrl == null || adminUrl.isBlank()) return;
    String password = System.getenv("DATABASE_PASSWORD");
    if (password == null || !password.matches("[A-Za-z0-9_-]{40,100}"))
      throw new IllegalStateException("A generated database password is required");
    var uri = URI.create(adminUrl);
    String[] credentials = uri.getUserInfo().split(":", 2);
    var props = new java.util.Properties();
    props.setProperty("user", URLDecoder.decode(credentials[0], StandardCharsets.UTF_8));
    props.setProperty("password", URLDecoder.decode(credentials[1], StandardCharsets.UTF_8));
    try (var connection =
        DriverManager.getConnection(
            "jdbc:postgresql://"
                + uri.getHost()
                + ":"
                + (uri.getPort() < 0 ? 5432 : uri.getPort())
                + uri.getPath(),
            props)) {
      try (var statement = connection.createStatement()) {
        try (var result =
            statement.executeQuery("select 1 from pg_roles where rolname='aitodu_app'")) {
          if (!result.next())
            statement.execute(
                "create role aitodu_app login password '"
                    + password
                    + "' nosuperuser nocreatedb nocreaterole noinherit");
        }
        boolean exists;
        try (var result =
            statement.executeQuery(
                "select pg_get_userbyid(datdba) from pg_database where datname='aitodu_av2'")) {
          exists = result.next();
          if (exists && !"aitodu_app".equals(result.getString(1)))
            throw new IllegalStateException("Existing database has another owner");
        }
        if (!exists) statement.execute("create database aitodu_av2 owner aitodu_app");
        statement.execute("revoke connect on database aitodu_av2 from public");
        statement.execute("grant connect on database aitodu_av2 to aitodu_app");
      }
    }
    // Fails fast if an existing role has different credentials. No role/password rotation is
    // attempted.
    try (var scoped =
            DriverManager.getConnection(
                System.getenv("JDBC_DATABASE_URL"), "aitodu_app", password);
        var statement = scoped.createStatement()) {
      try (var result = statement.executeQuery("select current_database(), current_user")) {
        result.next();
        if (!"aitodu_av2".equals(result.getString(1)) || !"aitodu_app".equals(result.getString(2)))
          throw new IllegalStateException("Isolation check failed");
      }
    }
    System.out.println(
        "AI-ToDu database provisioned with a dedicated database and restricted role.");
  }
}
