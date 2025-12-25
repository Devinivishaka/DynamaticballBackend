package com.protonestiot.dynamaticball.Config;

import com.azure.storage.blob.BlobServiceClient;
import com.azure.storage.blob.BlobServiceClientBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AzureBlobConfig {

    @Value("${azure.storage.endpoint}")
    private String storageEndpoint;

    @Value("${azure.storage.account-name}")
    private String accountName;

    @Value("${azure.storage.account-key}")
    private String accountKey;

    @Bean
    public BlobServiceClient blobServiceClient() {
        String connectionString = String.format(
                "DefaultEndpointsProtocol=https;AccountName=%s;AccountKey=%s;BlobEndpoint=%s",
                accountName, accountKey, storageEndpoint
        );

        return new BlobServiceClientBuilder()
                .connectionString(connectionString)
                .buildClient();
    }
}
