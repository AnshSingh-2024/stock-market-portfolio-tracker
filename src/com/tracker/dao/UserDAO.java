package com.tracker.dao;

import com.tracker.config.DBConnection;
import com.tracker.model.User;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.*;

/**
 * Data Access Object for User-related database operations.
 * Handles registration, authentication, and user lookup.
 */
public class UserDAO {

    /**
     * Registers a new user. The password is hashed using SHA-256 before storage.
     *
     * @param name     Full name of the user
     * @param email    Email address (must be unique)
     * @param password Raw password (will be hashed)
     * @return true if registration succeeds, false otherwise
     */
    public boolean registerUser(String name, String email, String password) {
        String sql = "INSERT INTO User (name, email, password_hash) VALUES (?, ?, ?)";
        String hash = hashPassword(password);

        try (Connection conn = DBConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, name);
            ps.setString(2, email);
            ps.setString(3, hash);

            int rows = ps.executeUpdate();
            return rows > 0;

        } catch (SQLException e) {
            // Duplicate email will throw an integrity constraint violation
            System.err.println("[UserDAO] Registration failed: " + e.getMessage());
            return false;
        }
    }

    /**
     * Authenticates a user by email and password.
     *
     * @param email    User's email
     * @param password Raw password to verify
     * @return User object if credentials match, null otherwise
     */
    public User authenticate(String email, String password) {
        String sql = "SELECT * FROM User WHERE email = ? AND password_hash = ?";
        String hash = hashPassword(password);

        try (Connection conn = DBConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, email);
            ps.setString(2, hash);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    User user = new User();
                    user.setUserId(rs.getInt("user_id"));
                    user.setName(rs.getString("name"));
                    user.setEmail(rs.getString("email"));
                    user.setPasswordHash(rs.getString("password_hash"));
                    return user;
                }
            }

        } catch (SQLException e) {
            System.err.println("[UserDAO] Authentication error: " + e.getMessage());
        }
        return null;
    }

    /**
     * Hashes a password using the SHA-256 algorithm.
     * Produces a 64-character hex string identical to the seed data format.
     */
    private String hashPassword(String password) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] digest = md.digest(password.getBytes());
            StringBuilder sb = new StringBuilder();
            for (byte b : digest) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 algorithm not available", e);
        }
    }
}
