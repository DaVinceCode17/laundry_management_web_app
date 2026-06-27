package com.resources.controller;

import com.resources.model.Customer;
import com.resources.model.Order;
import com.resources.model.Pricing;
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

@WebServlet("/api/laundry/*")
public class LaundryController extends HttpServlet {
    
    private LaundryService laundryService;
    
    @Override
    public void init() throws ServletException {
        laundryService = new LaundryService();
    }
    
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        String path = request.getPathInfo();
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        PrintWriter out = response.getWriter();
        
        try {
            if ("/customers".equals(path)) {
                List<Customer> customers = laundryService.getAllCustomers();
                buildCustomersJson(out, customers);
                
            } else if (path != null && path.startsWith("/customer/")) {
                int customerId = Integer.parseInt(path.substring(10));
                Customer customer = laundryService.getCustomerById(customerId);
                if (customer != null) {
                    out.write("{\"success\":true,\"customer\":{");
                    out.write("\"id\":" + customer.getId() + ",");
                    out.write("\"firstName\":\"" + customer.getFirstName() + "\",");
                    out.write("\"lastName\":\"" + customer.getLastName() + "\",");
                    out.write("\"contact\":\"" + customer.getContact() + "\",");
                    out.write("\"role\":\"" + customer.getRole() + "\"");
                    out.write("}}");
                } else {
                    out.write("{\"success\":false,\"message\":\"Customer not found\"}");
                }
                
            } else if ("/orders".equals(path)) {
                List<Order> orders = laundryService.getAllOrders();
                buildOrdersJson(out, orders);
                
            } else if (path != null && path.startsWith("/order/")) {
                int orderId = Integer.parseInt(path.substring(7));
                Order order = laundryService.getOrderById(orderId);
                if (order != null) {
                    out.write("{\"success\":true,\"order\":{");
                    out.write("\"id\":" + order.getId() + ",");
                    out.write("\"customerId\":" + order.getCustomerId() + ",");
                    out.write("\"services\":\"" + order.getServices() + "\",");
                    out.write("\"status\":\"" + order.getStatus() + "\",");
                    out.write("\"queueNumber\":" + order.getQueueNumber());
                    out.write("}}");
                } else {
                    out.write("{\"success\":false,\"message\":\"Order not found\"}");
                }
                
            } else if (path != null && path.startsWith("/orders/status/")) {
                String status = path.substring(15);
                List<Order> orders = laundryService.getOrdersByStatus(status);
                buildOrdersJson(out, orders);
                
            } else if (path != null && path.startsWith("/orders/customer/")) {
                int customerId = Integer.parseInt(path.substring(17));
                List<Order> orders = laundryService.getOrdersByCustomer(customerId);
                buildOrdersJson(out, orders);
                
            } else if ("/pricing".equals(path)) {
                Pricing pricing = laundryService.getPricing();
                out.write("{\"success\":true,\"pricing\":{");
                out.write("\"id\":" + pricing.getId() + ",");
                out.write("\"washPrice\":" + pricing.getWashPrice() + ",");
                out.write("\"dryPrice\":" + pricing.getDryPrice() + ",");
                out.write("\"foldPrice\":" + pricing.getFoldPrice());
                out.write("}}");
                
            } else if ("/dashboard".equals(path)) {
                List<Order> pending = laundryService.getOrdersByStatus("pending");
                List<Order> ongoing = laundryService.getOrdersByStatus("ongoing");
                List<Order> completed = laundryService.getOrdersByStatus("completed");
                List<Customer> customers = laundryService.getAllCustomers();
                
                out.write("{\"success\":true,\"dashboard\":{");
                out.write("\"pendingCount\":" + pending.size() + ",");
                out.write("\"ongoingCount\":" + ongoing.size() + ",");
                out.write("\"completedCount\":" + completed.size() + ",");
                out.write("\"totalCustomers\":" + customers.size());
                out.write("}}");
                
            } else {
                out.write("{\"success\":false,\"message\":\"Invalid endpoint\"}");
            }
            
        } catch (Exception e) {
            out.write("{\"success\":false,\"message\":\"" + e.getMessage() + "\"}");
        }
    }
    
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        String path = request.getPathInfo();
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        PrintWriter out = response.getWriter();
        
        String jsonBody = readJsonBody(request);
        
        try {
            if ("/order/create".equals(path)) {
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
                
            } else if ("/pricing/update".equals(path)) {
                int id = Integer.parseInt(extractValue(jsonBody, "id"));
                double washPrice = Double.parseDouble(extractValue(jsonBody, "washPrice"));
                double dryPrice = Double.parseDouble(extractValue(jsonBody, "dryPrice"));
                double foldPrice = Double.parseDouble(extractValue(jsonBody, "foldPrice"));
                
                Pricing pricing = new Pricing();
                pricing.setId(id);
                pricing.setWashPrice(washPrice);
                pricing.setDryPrice(dryPrice);
                pricing.setFoldPrice(foldPrice);
                
                boolean updated = laundryService.updatePricing(pricing);
                out.write("{\"success\":" + updated + ",\"message\":\"" + (updated ? "Pricing updated" : "Update failed") + "\"}");
                
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
        
        String jsonBody = readJsonBody(request);
        
        try {
            if (path != null && path.startsWith("/order/status/")) {
                String[] parts = path.split("/");
                int orderId = Integer.parseInt(parts[3]);
                String status = extractValue(jsonBody, "status");
                
                Order order = laundryService.updateOrderStatus(orderId, status);
                
                out.write("{\"success\":true,\"message\":\"Status updated\",\"order\":{");
                out.write("\"id\":" + order.getId() + ",");
                out.write("\"status\":\"" + order.getStatus() + "\"");
                out.write("}}");
                
            } else if ("/order/update".equals(path)) {
                int orderId = Integer.parseInt(extractValue(jsonBody, "id"));
                String status = extractValue(jsonBody, "status");
                double weight = Double.parseDouble(extractValue(jsonBody, "weight"));
                double price = Double.parseDouble(extractValue(jsonBody, "price"));
                
                Order order = laundryService.updateOrder(orderId, status, weight, price);
                
                out.write("{\"success\":true,\"message\":\"Order updated\",\"order\":{");
                out.write("\"id\":" + order.getId() + ",");
                out.write("\"status\":\"" + order.getStatus() + "\",");
                out.write("\"weight\":" + order.getWeight() + ",");
                out.write("\"price\":" + order.getPrice());
                out.write("}}");
                
            } else {
                out.write("{\"success\":false,\"message\":\"Invalid endpoint\"}");
            }
            
        } catch (Exception e) {
            out.write("{\"success\":false,\"message\":\"" + e.getMessage() + "\"}");
        }
    }
    
    @Override
    protected void doDelete(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        String path = request.getPathInfo();
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        PrintWriter out = response.getWriter();
        
        try {
            if (path != null && path.startsWith("/order/")) {
                int orderId = Integer.parseInt(path.substring(7));
                boolean deleted = laundryService.deleteOrder(orderId);
                out.write("{\"success\":" + deleted + ",\"message\":\"" + (deleted ? "Order deleted" : "Delete failed") + "\"}");
            } else {
                out.write("{\"success\":false,\"message\":\"Invalid endpoint\"}");
            }
        } catch (Exception e) {
            out.write("{\"success\":false,\"message\":\"" + e.getMessage() + "\"}");
        }
    }
    
    private String readJsonBody(HttpServletRequest request) throws IOException {
        StringBuilder sb = new StringBuilder();
        try (BufferedReader reader = request.getReader()) {
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line);
            }
        }
        return sb.toString();
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
    
    private void buildCustomersJson(PrintWriter out, List<Customer> customers) {
        out.write("{\"success\":true,\"customers\":[");
        for (int i = 0; i < customers.size(); i++) {
            Customer c = customers.get(i);
            out.write("{");
            out.write("\"id\":" + c.getId() + ",");
            out.write("\"firstName\":\"" + c.getFirstName() + "\",");
            out.write("\"lastName\":\"" + c.getLastName() + "\",");
            out.write("\"contact\":\"" + c.getContact() + "\",");
            out.write("\"role\":\"" + c.getRole() + "\"");
            out.write("}");
            if (i < customers.size() - 1) out.write(",");
        }
        out.write("]}");
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
}