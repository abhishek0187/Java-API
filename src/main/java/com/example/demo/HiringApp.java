package com.example.demo;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.http.*;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDate;
import java.time.Period;
import java.util.HashMap;
import java.util.Map;

@SpringBootApplication
public class HiringApp implements CommandLineRunner {

    private final RestTemplate restTemplate = new RestTemplate();

    public static void main(String[] args) {
        SpringApplication.run(HiringApp.class, args);
    }

    @Override
    public void run(String... args) {
        try {
            // 1. Call generateWebhook API
            String generateUrl = "https://bfhldevapigw.healthrx.co.in/hiring/generateWebhook/JAVA";

            Map<String, String> details = new HashMap<>();
            details.put("name", "Abhishek Raj");
            details.put("regNo", "22BCT0187");
            details.put("email", "abhishek.raj2022@vitstudent.ac.in");

            ResponseEntity<Map> response = restTemplate.postForEntity(generateUrl, details, Map.class);

            if (response.getStatusCode() != HttpStatus.OK) {
                System.out.println("❌ Failed to generate webhook: " + response.getStatusCode());
                return;
            }

            Map<String, Object> body = response.getBody();
            if (body == null) {
                System.out.println("❌ Empty response from generateWebhook");
                return;
            }

            String webhookUrl = (String) body.get("webhook");
            String accessToken = (String) body.get("accessToken");

            System.out.println("✅ Webhook URL: " + webhookUrl);
            System.out.println("✅ AccessToken: " + accessToken);

            String sqlQuery =
                    "SELECT p.AMOUNT AS SALARY, " +
                    "CONCAT(e.FIRST_NAME, ' ', e.LAST_NAME) AS NAME, " +
                    "TIMESTAMPDIFF(YEAR, e.DOB, CURDATE()) AS AGE, " +
                    "d.DEPARTMENT_NAME " +
                    "FROM PAYMENTS p " +
                    "JOIN EMPLOYEE e ON p.EMP_ID = e.EMP_ID " +
                    "JOIN DEPARTMENT d ON e.DEPARTMENT = d.DEPARTMENT_ID " +
                    "WHERE DAY(p.PAYMENT_TIME) <> 1 " +
                    "ORDER BY p.AMOUNT DESC " +
                    "LIMIT 1;";

            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", accessToken);
            headers.setContentType(MediaType.APPLICATION_JSON);

            Map<String, String> finalBody = new HashMap<>();
            finalBody.put("finalQuery", sqlQuery);

            HttpEntity<Map<String, String>> request = new HttpEntity<>(finalBody, headers);

            ResponseEntity<String> finalResponse =
                    restTemplate.postForEntity(webhookUrl, request, String.class);

            if (finalResponse.getStatusCode() == HttpStatus.OK) {
                System.out.println("✅ Successfully submitted solution!");
                System.out.println("Response: " + finalResponse.getBody());
            } else {
                System.out.println("❌ Failed to submit solution: " + finalResponse.getStatusCode());
                System.out.println("Response: " + finalResponse.getBody());
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
