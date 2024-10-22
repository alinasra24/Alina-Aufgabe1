#!/bin/sh

# Output file location
OUTPUT_FILE=/usr/local/bin/output/curl_output.txt

# Create or clear the output file
echo "" > $OUTPUT_FILE  # Clear the file at the start

echo "Executing GET request..." >> $OUTPUT_FILE

# Wait for the list service to be available
until curl -sSf http://list:7979/getCustomers; do
    echo "Waiting for list service to be available..." >> $OUTPUT_FILE
    sleep 5
done

echo "GET request successful." >> $OUTPUT_FILE

# Executing POST request
echo "Executing POST request..." >> $OUTPUT_FILE
response_post=$(curl -X POST -L -v http://list:7979/postCustomerData -d '{}' 2>&1)

# Write POST response to output file
echo "POST Response: $response_post" >> $OUTPUT_FILE


