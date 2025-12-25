package com.protonestiot.dynamaticball.Service;

import com.azure.storage.blob.BlobClient;
import com.azure.storage.blob.BlobServiceClient;
import com.azure.storage.blob.sas.BlobSasPermission;
import com.azure.storage.blob.sas.BlobServiceSasSignatureValues;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;

@Service
@RequiredArgsConstructor
public class AzureBlobService {

    private final BlobServiceClient blobServiceClient;

    @Value("${azure.storage.video-container}")
    private String videoContainer;

    @Value("${azure.storage.thumbnail-container}")
    private String thumbnailContainer;

    @Value("${azure.storage.sas.expiry-minutes:120}")
    private int expiryMinutes;

    public String getVideoUrl(String blobName) {
        return generateSas(videoContainer, blobName);
    }

    public String getThumbnailUrl(String blobName) {
        return generateSas(thumbnailContainer, blobName);
    }

    private String generateSas(String container, String blobName) {

        BlobClient blobClient = blobServiceClient
                .getBlobContainerClient(container)
                .getBlobClient(blobName);

        if (!blobClient.exists()) {
            return null;
        }

        BlobSasPermission permission = new BlobSasPermission()
                .setReadPermission(true);

        BlobServiceSasSignatureValues values =
                new BlobServiceSasSignatureValues(
                        OffsetDateTime.now().plusMinutes(expiryMinutes),
                        permission
                );

        return blobClient.getBlobUrl() + "?" + blobClient.generateSas(values);
    }
}
