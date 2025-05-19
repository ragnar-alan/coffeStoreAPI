package com.coffee.coffeestoreapi.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * Controller for handling web UI routes
 */
@Controller
public class WebController {

    /**
     * Home page
     * @return the index template
     */
    @GetMapping("/")
    public String index() {
        return "index";
    }

    /**
     * Orders management page
     * @return the orders template
     */
    @GetMapping("/admin/orders")
    public String ordersPage() {
        return "admin/orders";
    }

    /**
     * Products management page
     * @return the products template
     */
    @GetMapping("/admin/products")
    public String productsPage() {
        return "admin/products";
    }
}