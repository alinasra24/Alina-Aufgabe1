package com.example.customer;

import java.util.*;

public class Logik {

    // Berechnung der Zeitdifferenz zwischen zwei Timestamps
    public static long hexTimeDifference(long time1, long time2) {
        return Math.abs(time1 - time2); // Berechnung der Differenz in Millisekunden
    }

    // Berechnung der Gesamtzeit pro Customer
    public static List<CustomerPost> giveCustomer(Customer[] customers) {
        // Map zur Speicherung der Gesamtnutzungsdauer pro Customer (CustomerId -> Gesamtnutzungsdauer)
        Map<String, Long> customerTotalTimes = new HashMap<>();

        // Map zur temporären Speicherung der Start-Ereignisse nach WorkloadId
        Map<String, Customer> workloadStartEvents = new HashMap<>();


        for (Customer customer : customers) {
            String customerId = customer.getCustomerId();
            String workloadId = customer.getWorkloadId();
            String eventType = customer.getEventType();


            if ("start".equals(eventType)) {
                workloadStartEvents.put(workloadId, customer);
            }

            // Wenn stop, Berechnung der Differenz zum passenden start-Ereignis
            if ("stop".equals(eventType)) {
                Customer startEvent = workloadStartEvents.get(workloadId);
                if (startEvent != null) {
                    // Zeitdifferenz zwischen start und stop
                    long timeDifference = hexTimeDifference(startEvent.getTimestamp(), customer.getTimestamp());

                    // Addiere die Zeitdifferenz zur Gesamtnutzungsdauer des Kunden
                    customerTotalTimes.put(customerId, customerTotalTimes.getOrDefault(customerId, 0L) + timeDifference);


                    workloadStartEvents.remove(workloadId);
                }
            }
        }

        // Erstelle eine Liste
        List<CustomerPost> customerPosts = new ArrayList<>();
        for (Map.Entry<String, Long> entry : customerTotalTimes.entrySet()) {
            customerPosts.add(new CustomerPost(entry.getKey(), entry.getValue()));
        }

        return customerPosts; // Rückgabe der berechneten Ergebnisse
    }
}
