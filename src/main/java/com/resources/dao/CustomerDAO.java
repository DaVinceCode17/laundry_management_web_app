package com.resources.dao;

import com.resources.model.Customer;
import com.resources.DatabaseConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class CustomerDAO {
    
    // Register new customer
    public boolean save(Customer customer) throws SQLException {
        String sql = "INSERT INTO customers (first_name, last_name, middle_initial, address, contact, nickname, password, role) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            
            pstmt.setString(1, customer.getFirstName());
            pstmt.setString(2, customer.getLastName());
            pstmt.setString(3, customer.getMiddleInitial());
            pstmt.setString(4, customer.getAddress());
            pstmt.setString(5, customer.getContact());
            pstmt.setString(6, customer.getNickname());
            pstmt.setString(7, customer.getPassword());
            pstmt.setString(8, customer.getRole() != null ? customer.getRole() : "customer");
            
            int affected = pstmt.executeUpdate();
            
            if (affected > 0) {
                ResultSet rs = pstmt.getGeneratedKeys();
                if (rs.next()) {
                    customer.setId(rs.getInt(1));
                }
                return true;
            }
            return false;
        }
    }
    
    // Find by contact and password (LOGIN)
    public Customer findByContactAndPassword(String contact, String password) throws SQLException {
        String sql = "SELECT * FROM customers WHERE contact = ? AND password = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, contact);
            pstmt.setString(2, password);
            
            ResultSet rs = pstmt.executeQuery();
            
            if (rs.next()) {
                return mapResultSetToCustomer(rs);
            }
            return null;
        }
    }
    
    // Find by contact
    public Customer findByContact(String contact) throws SQLException {
        String sql = "SELECT * FROM customers WHERE contact = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, contact);
            ResultSet rs = pstmt.executeQuery();
            
            if (rs.next()) {
                return mapResultSetToCustomer(rs);
            }
            return null;
        }
    }
    
    // Find by ID
    public Customer findById(int id) throws SQLException {
        String sql = "SELECT * FROM customers WHERE id = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, id);
            ResultSet rs = pstmt.executeQuery();
            
            if (rs.next()) {
                return mapResultSetToCustomer(rs);
            }
            return null;
        }
    }
    
    // Get all customers
    public List<Customer> findAll() throws SQLException {
        List<Customer> customers = new ArrayList<>();
        String sql = "SELECT * FROM customers ORDER BY id DESC";
        
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                customers.add(mapResultSetToCustomer(rs));
            }
        }
        return customers;
    }
    
    // Helper method
    private Customer mapResultSetToCustomer(ResultSet rs) throws SQLException {
        Customer customer = new Customer();
        customer.setId(rs.getInt("id"));
        customer.setFirstName(rs.getString("first_name"));
        customer.setLastName(rs.getString("last_name"));
        customer.setMiddleInitial(rs.getString("middle_initial"));
        customer.setAddress(rs.getString("address"));
        customer.setContact(rs.getString("contact"));
        customer.setNickname(rs.getString("nickname"));
        customer.setPassword(rs.getString("password"));
        customer.setRole(rs.getString("role"));
        customer.setCreatedAt(rs.getString("created_at"));
        return customer;
    }
}