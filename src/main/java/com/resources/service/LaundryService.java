package com.resources.service;

import com.resources.dao.CustomerDAO;
import com.resources.dao.OrderDAO;
import com.resources.dao.PricingDAO;
import com.resources.model.Customer;
import com.resources.model.Order;
import com.resources.model.Pricing;

import java.sql.SQLException;
import java.util.List;

public class LaundryService {
    
    private CustomerDAO customerDAO;
    private OrderDAO orderDAO;
    private PricingDAO pricingDAO;
    
    public LaundryService() {
        this.customerDAO = new CustomerDAO();
        this.orderDAO = new OrderDAO();
        this.pricingDAO = new PricingDAO();
    }
    
    // ===== AUTHENTICATION =====
    public Customer login(String contact, String password) throws SQLException {
        return customerDAO.findByContactAndPassword(contact, password);
    }
    
    public boolean register(Customer customer) throws SQLException {
        Customer existing = customerDAO.findByContact(customer.getContact());
        if (existing != null) {
            throw new IllegalArgumentException("Contact number already registered");
        }
        return customerDAO.save(customer);
    }
    
    // ===== CUSTOMER METHODS =====
    public List<Customer> getAllCustomers() throws SQLException {
        return customerDAO.findAll();
    }
    
    public Customer getCustomerById(int id) throws SQLException {
        return customerDAO.findById(id);
    }
    
    // ===== ORDER METHODS =====
    public Order createOrder(Order order) throws SQLException {
        int queueNumber = orderDAO.getNextQueueNumber();
        order.setQueueNumber(queueNumber);
        order.setStatus("pending");
        return orderDAO.save(order);
    }
    
    public List<Order> getOrdersByCustomer(int customerId) throws SQLException {
        return orderDAO.findByCustomerId(customerId);
    }
    
    public List<Order> getOrdersByStatus(String status) throws SQLException {
        return orderDAO.findByStatus(status);
    }
    
    public List<Order> getAllOrders() throws SQLException {
        return orderDAO.findAll();
    }
    
    public Order getOrderById(int id) throws SQLException {
        return orderDAO.findById(id);
    }
    
    public Order updateOrderStatus(int orderId, String status) throws SQLException {
        return orderDAO.updateStatus(orderId, status);
    }
    
    public Order updateOrder(int orderId, String status, double weight, double price) throws SQLException {
        return orderDAO.updateOrder(orderId, status, weight, price);
    }
    
    public boolean deleteOrder(int orderId) throws SQLException {
        return orderDAO.delete(orderId);
    }
    
    // ===== PRICING METHODS =====
    public Pricing getPricing() throws SQLException {
        Pricing pricing = pricingDAO.getPricing();
        if (pricing == null) {
            pricingDAO.insertDefaultPricing();
            pricing = pricingDAO.getPricing();
        }
        return pricing;
    }
    
    public boolean updatePricing(Pricing pricing) throws SQLException {
        return pricingDAO.updatePricing(pricing);
    }
}