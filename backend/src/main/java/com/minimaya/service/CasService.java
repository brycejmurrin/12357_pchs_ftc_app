package com.minimaya.service;

import com.minimaya.domain.model.CasObject;
import com.minimaya.domain.repository.CasObjectRepository;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import io.minio.GetPresignedObjectUrlArgs;
import io.minio.http.Method;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayInputStream;
import java.security.MessageDigest;
import java.util.HexFormat;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
@Slf4j
public class CasService {

    private final MinioClient minioClient;
    private final CasObjectRepository casObjectRepository;

    @Value("${minimaya.minio.bucket}")
    private String bucket;

    /**
     * Store a raw geometry blob. The byte[] is treated as completely opaque.
     * Returns the SHA-256 hash (the CAS key).
     */
    @Transactional
    public String store(byte[] data, String mimeType) throws Exception {
        final String sha256 = sha256Hex(data);

        if (casObjectRepository.existsBySha256(sha256)) {
            log.debug("CAS hit — skipping upload for {}", sha256);
            return sha256;
        }

        final String objectKey = "cas/" + sha256;
        minioClient.putObject(
            PutObjectArgs.builder()
                .bucket(bucket)
                .object(objectKey)
                .stream(new ByteArrayInputStream(data), data.length, -1)
                .contentType(mimeType)
                .build()
        );

        casObjectRepository.save(
            CasObject.builder()
                .sha256(sha256)
                .sizeBytes(data.length)
                .mimeType(mimeType)
                .storageUrl(bucket + "/" + objectKey)
                .build()
        );

        log.info("CAS stored {} bytes under key {}", data.length, sha256);
        return sha256;
    }

    public String presignedDownloadUrl(String sha256, int expiryMinutes) throws Exception {
        return minioClient.getPresignedObjectUrl(
            GetPresignedObjectUrlArgs.builder()
                .method(Method.GET)
                .bucket(bucket)
                .object("cas/" + sha256)
                .expiry(expiryMinutes, TimeUnit.MINUTES)
                .build()
        );
    }

    private static String sha256Hex(byte[] data) throws Exception {
        return HexFormat.of().formatHex(
            MessageDigest.getInstance("SHA-256").digest(data)
        );
    }
}
