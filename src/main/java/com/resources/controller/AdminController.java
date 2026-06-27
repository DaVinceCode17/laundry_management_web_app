package com.resources.controller;

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

@WebServlet("/api/admin/*")
public class AdminController extends HttpServlet {
    
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
            if ("/pricing".equals(path)) {
                Pricing pricing = laundryService.getPricing();
                
                out.write("{\"success\":true,\"pricing\":{");
                out.write("\"id\":" + pricing.getId() + ",");
                out.write("\"washPrice\":" + pricing.getWashPrice() + ",");
                out.write("\"dryPrice\":" + pricing.getDryPrice() + ",");
                out.write("\"foldPrice\":" + pricing.getFoldPrice());
                out.write("}}");
                
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
        
        if ("/pricing".equals(path)) {
            StringBuilder sb = new StringBuilder();
            try (BufferedReader reader = request.getReader()) {
                String line;
                while ((line = reader.readLine()) != null) {
                    sb.append(line);
                }
            }
            String jsonBody = sb.toString();
            
            try {
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
                
            } catch (Exception e) {
                out.write("{\"success\":false,\"message\":\"" + e.getMessage() + "\"}");
            }
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