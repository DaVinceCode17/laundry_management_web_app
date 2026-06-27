package com.resources.controller;

import com.resources.model.Order;
import com.resources.service.LaundryService;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

@WebServlet("/api/orders/*")
public class OrderController extends HttpServlet {
    
    private LaundryService laundryService;
    
    @Override
    public void init() throws ServletException {
        laundryService = new LaundryService();
    }
    
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        String path = request.getPathInfo();
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        PrintWriter out = response.getWriter();
        
        if ("/create".equals(path)) {
            StringBuilder sb = new StringBuilder();
            try (BufferedReader reader = request.getReader()) {
                String line;
                while ((line = reader.readLine()) != null) {
                    sb.append(line);
                }
            }
            String jsonBody = sb.toString();
            
            try {
                int customerId = Integer.parseInt(extractValue(jsonBody, "customerId"));
                String services = extractValue(jsonBody, "services");
                String serviceType = extractValue(jsonBody, "serviceType");
                
                Order order = new Order(customerId, services, serviceType);
                Order created = laundryService.createOrder(order);
                
                out.write("{\"success\":true,\"message\":\"Order created\",\"order\":{");
                out.write("\"id\":" + created.getId() + ",");
                out.write("\"queueNumber\":" + created.getQueueNumber() + ",");
                out.write("\"status\":\"" + created.getStatus() + "\"");
                out.write("}}");
                
            } catch (Exception e) {
                out.write("{\"success\":false,\"message\":\"" + e.getMessage() + "\"}");
            }
        }
    }
    
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        String path = request.getPathInfo();
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        PrintWriter out = response.getWriter();
        
        try {
            if (path != null && path.startsWith("/customer/")) {
                int customerId = Integer.parseInt(path.substring(10));
                List<Order> orders = laundryService.getOrdersByCustomer(customerId);
                buildOrdersJson(out, orders);
                
            } else if (path != null && path.startsWith("/status/")) {
                String status = path.substring(8);
                List<Order> orders = laundryService.getOrdersByStatus(status);
                buildOrdersJson(out, orders);
                
            } else if ("/all".equals(path)) {
                List<Order> orders = laundryService.getAllOrders();
                buildOrdersJson(out, orders);
                
            } else {
                out.write("{\"success\":false,\"message\":\"Invalid endpoint\"}");
            }
            
        } catch (Exception e) {
            out.write("{\"success\":false,\"message\":\"" + e.getMessage() + "\"}");
        }
    }
    
    @Override
    protected void doPut(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        String path = request.getPathInfo();
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        PrintWriter out = response.getWriter();
        
        if (path != null && path.contains("/status")) {
            String[] parts = path.split("/");
            int orderId = Integer.parseInt(parts[1]);
            
            StringBuilder sb = new StringBuilder();
            try (BufferedReader reader = request.getReader()) {
                String line;
                while ((line = reader.readLine()) != null) {
                    sb.append(line);
                }
            }
            String jsonBody = sb.toString();
            
            try {
                String newStatus = extractValue(jsonBody, "status");
                Order order = laundryService.updateOrderStatus(orderId, newStatus);
                
                out.write("{\"success\":true,\"message\":\"Status updated\",\"order\":{");
                out.write("\"id\":" + order.getId() + ",");
                out.write("\"status\":\"" + order.getStatus() + "\"");
                out.write("}}");
                
            } catch (Exception e) {
                out.write("{\"success\":false,\"message\":\"" + e.getMessage() + "\"}");
            }
        }
    }
    
    private void buildOrdersJson(PrintWriter out, List<Order> orders) {
        out.write("{\"success\":true,\"orders\":[");
        for (int i = 0; i < orders.size(); i++) {
            Order o = orders.get(i);
            out.write("{");
            out.write("\"id\":" + o.getId() + ",");
            out.write("\"customerId\":" + o.getCustomerId() + ",");
            out.write("\"customerName\":\"" + o.getCustomerName() + "\",");
            out.write("\"services\":\"" + o.getServices() + "\",");
            out.write("\"serviceType\":\"" + o.getServiceType() + "\",");
            out.write("\"status\":\"" + o.getStatus() + "\",");
            out.write("\"queueNumber\":" + o.getQueueNumber());
            out.write("}");
            if (i < orders.size() - 1) out.write(",");
        }
        out.write("]}");
    }
    
    private String extractValue(String json, String key) {
        String search = "\"" + key + "\":";
        int start = json.indexOf(search);
        if (start == -1) return "";
        
        start += search.length();
        char firstChar = json.charAt(start);
        
        if (firstChar == '"') {
            start++;
            int end = json.indexOf("\"", start);
            return json.substring(start, end);
        } else {
            int end = start;
            while (end < json.length() && json.charAt(end) != ',' && json.charAt(end) != '}') {
                end++;
            }
            return json.substring(start, end).trim();
        }
    }
}