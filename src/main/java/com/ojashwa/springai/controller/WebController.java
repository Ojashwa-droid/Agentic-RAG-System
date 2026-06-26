package com.ojashwa.springai.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.ResponseEntity;
import java.util.List;

@Controller
public class WebController {
    
    private final RestTemplate restTemplate = new RestTemplate();
    private final String API_URL = "http://localhost:8080/api/chat/tokens";
    
    @GetMapping("/tokens")
    public String tokenPage() {
        return "tokens";
    }
    
    @PostMapping("/tokens")
    public String tokenizeMessage(@RequestParam("message") String message, Model model) {
        try {
            String url = API_URL + "?message=" + message;
            ResponseEntity<TokenCountController.TokenDetail[]> response = restTemplate.getForEntity(url, TokenCountController.TokenDetail[].class);
            
            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                model.addAttribute("tokens", response.getBody());
                model.addAttribute("originalMessage", message);
                model.addAttribute("tokenCount", response.getBody().length);
            } else {
                model.addAttribute("error", "Failed to tokenize message");
            }
        } catch (Exception e) {
            model.addAttribute("error", "Error: " + e.getMessage());
        }
        
        return "tokens";
    }
}
