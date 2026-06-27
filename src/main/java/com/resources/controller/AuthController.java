package com.resources.controller;

import com.resources.model.Customer;
import com.resources.service.LaundryService;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;

@WebServlet("/api/auth/*")
public class AuthController extends HttpServlet {
    
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
        
        // Read JSON body
        StringBuilder sb = new StringBuilder();
        try (BufferedReader reader = request.getReader()) {
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line);
            }
        }
        String jsonBody = sb.toString();
        
        try {
            if ("/login".equals(path)) {
                String contact = extractValue(jsonBody, "contact");
                String password = extractValue(jsonBody, "password");
                
                Customer customer = laundryService.login(contact, password);
                if (customer != null) {
                    out.write("{\"success\":true,\"message\":\"Login successful\",\"user\":{");
                    out.write("\"id\":" + customer.getId() + ",");
                    out.write("\"firstName\":\"" + customer.getFirstName() + "\",");
                    out.write("\"lastName\":\"" + customer.getLastName() + "\",");
                    out.write("\"contact\":\"" + customer.getContact() + "\",");
                    out.write("\"role\":\"" + customer.getRole() + "\"");
                    out.write("}}");
                } else {
                    out.write("{\"success\":false,\"message\":\"Invalid credentials\"}");
                }
                
            } else if ("/register".equals(path)) {
                String firstName = extractValue(jsonBody, "firstName");
                String lastName = extractValue(jsonBody, "lastName");
                String contact = extractValue(jsonBody, "contact");
                String password = extractValue(jsonBody, "password");
                String address = extractValue(jsonBody, "address");
                String nickname = extractValue(jsonBody, "nickname");
                
                Customer customer = new Customer();
                customer.setFirstName(firstName);
                customer.setLastName(lastName);
                customer.setContact(contact);
                customer.setPassword(password);
                customer.setAddress(address);
                customer.setNickname(nickname);
                customer.setRole("customer");
                
                boolean registered = laundryService.register(customer);
                if (registered) {
                    out.write("{\"success\":true,\"message\":\"Registration successful\"}");
                } else {
                    out.write("{\"success\":false,\"message\":\"Registration failed\"}");
                }
                
            } else {
                out.write("{\"success\":false,\"message\":\"Invalid endpoint\"}");
            }
            
        } catch (Exception e) {
            out.write("{\"success\":false,\"message\":\"" + e.getMessage() + "\"}");
        }
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