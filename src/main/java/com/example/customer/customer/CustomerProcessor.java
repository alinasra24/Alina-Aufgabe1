package com.example.customer;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import java.util.List;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.*;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.*;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.client.RestTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.*;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.client.RestTemplate;
import org.springframework.beans.factory.annotation.Autowired;

@Controller
public class CustomerProcessor {

    @Autowired
    private RestTemplate restTemplate;

    private final ObjectMapper objectMapper;
    private List<Customer> customers = new ArrayList<>();

    @Value("${dataset.service.url}")
    private String datasetServiceUrl;

    public CustomerProcessor(RestTemplate restTemplate, ObjectMapper objectMapper) {
        this.restTemplate = restTemplate;
        this.objectMapper = objectMapper;
    }

    @GetMapping("/getCustomers")
    public ResponseEntity<List<Customer>> getCustomers() {
        try {
            customers.clear();
            // Daten von der externen Quelle abrufen
            String response = restTemplate.getForObject(datasetServiceUrl, String.class);

            // JSON-String in JsonNode umwandeln
            JsonNode jsonNode = objectMapper.readTree(response);

            // Events durchlaufen und Customer-Objekte füllen
            for (JsonNode eventNode : jsonNode.get("events")) {
                Customer customer = new Customer();
                customer.setCustomerId(eventNode.get("customerId").asText());
                customer.setWorkloadId(eventNode.get("workloadId").asText());
                customer.setTimestamp(eventNode.get("timestamp").asLong());
                customer.setEventType(eventNode.get("eventType").asText());
                customers.add(customer);
            }

            // Sortiere die Kundenliste nach timestamp (aufsteigend)
            customers.sort(Comparator.comparingLong(Customer::getTimestamp));

            return ResponseEntity.ok(customers);
        } catch (Exception e) {
            return ResponseEntity.status(500).body(null);
        }
    }


    @PostMapping("/postCustomerData")
    public ResponseEntity<Map<String, List<CustomerPost>>> postCustomerData() {
        // Überprüfen, ob die Kundenliste leer ist
        if (customers == null || customers.isEmpty()) {
            return ResponseEntity.badRequest().body(Collections.singletonMap("result", Collections.singletonList(new CustomerPost("Error", 0))));
        }

        // Map für den Verbrauch pro Kunde
        Map<String, Long> aggregatedConsumption = new HashMap<>();

        // Liste zur Rückgabe
        List<CustomerPost> results = new ArrayList<>();

        // Maps zur Speicherung von Start- und Stop-Zeitpunkten
        Map<String, Long> startTimestamps = new HashMap<>();

        // Events pro workloadId durchlaufen und Verbrauch berechnen
        for (Customer customer : customers) {
            if ("start".equalsIgnoreCase(customer.getEventType())) {
                // Speichere den Start-Zeitstempel
                startTimestamps.put(customer.getWorkloadId(), customer.getTimestamp());
            } else if ("stop".equalsIgnoreCase(customer.getEventType()) && startTimestamps.containsKey(customer.getWorkloadId())) {
                // Berechne den Verbrauch zwischen Start und Stop
                long startTimestamp = startTimestamps.get(customer.getWorkloadId());
                long stopTimestamp = customer.getTimestamp();

                long consumptionValue = calculateConsumption(startTimestamp, stopTimestamp);
                aggregatedConsumption.merge(customer.getCustomerId(), consumptionValue, Long::sum);
            }
        }

        // Ergebnisliste erstellen
        for (Map.Entry<String, Long> entry : aggregatedConsumption.entrySet()) {
            results.add(new CustomerPost(entry.getKey(), entry.getValue()));
        }

        // Rückgabe der berechneten Ergebnisse in der "result"-Liste
        Map<String, List<CustomerPost>> response = new HashMap<>();
        response.put("result", results);
        return ResponseEntity.ok(response);
    }


    // Verbrauchsberechnungsmethode, die den Zeitunterschied zwischen Start und Stop berechnet
    private long calculateConsumption(Long startTimestamp, Long stopTimestamp) {
        // Unix-Timestamps in LocalDateTime konvertieren
        LocalDateTime startDateTime = Instant.ofEpochMilli(startTimestamp)
                .atZone(ZoneId.systemDefault()).toLocalDateTime();
        LocalDateTime stopDateTime = Instant.ofEpochMilli(stopTimestamp)
                .atZone(ZoneId.systemDefault()).toLocalDateTime();

        // Differenz zwischen Start- und Stop-Zeit berechnen (z.B. in Sekunden)
        return ChronoUnit.SECONDS.between(startDateTime, stopDateTime);
    }
}









