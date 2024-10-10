<?php

$url = "http://localhost:8080/api/v1/analyze";

$parameters = file_get_contents('./src/main/resources/json_test.json');

$curl_opts = [
    CURLOPT_HEADER => false,
    CURLOPT_RETURNTRANSFER => true,
    CURLOPT_CONNECTTIMEOUT => 10, // a timeout to call itself should not be too much higher :D
    CURLOPT_SSL_VERIFYPEER => true,
    CURLOPT_SSL_VERIFYHOST => 2,
    CURLOPT_POSTFIELDS => $parameters,
    CURLINFO_HEADER_OUT => true,
    CURLOPT_TIMEOUT => 120
];


while (true) {

    $ch = curl_init($url);
    curl_setopt_array($ch, $curl_opts);

    $response = curl_exec($ch);

    if ($response === false) {
        echo 'Curl error: ' . curl_error($ch) . "\n";
    } else {
        // Print response
        echo 'Response: ' . $response . "\n";
    }

// Close cURL session
    curl_close($ch);

    sleep(3);

}



