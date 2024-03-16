package com.rekognition;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.rekognition.AmazonRekognition;
import com.amazonaws.services.rekognition.AmazonRekognitionClientBuilder;
import com.amazonaws.services.rekognition.model.DetectLabelsRequest;
import com.amazonaws.services.rekognition.model.DetectLabelsResult;
import com.amazonaws.services.rekognition.model.Image;
import com.amazonaws.services.rekognition.model.Label;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.services.s3.model.S3ObjectInputStream;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.List;
import java.util.Scanner;

@SpringBootApplication
public class AwsRekognitionApplication {

    public static void main(String[] args) {
        SpringApplication.run(AwsRekognitionApplication.class, args);

        Scanner scanner = new Scanner(System.in);

        System.out.print("Enter AWS Access Key: ");
        String accessKey = scanner.nextLine();

        System.out.print("Enter AWS Secret Key: ");
        String secretKey = scanner.nextLine();

        System.out.print("Enter AWS Region: ");
        String region = scanner.nextLine();

        System.out.print("Enter AWS S3 bucketName: ");
        String bucketName = scanner.nextLine();

        System.out.print("Enter the key of the image object in S3 bucket: ");
        String objectKey = scanner.nextLine();

        try {
            BasicAWSCredentials awsCreds = new BasicAWSCredentials(accessKey, secretKey);

            // Initialize S3 client
            AmazonS3 s3 = AmazonS3ClientBuilder.standard()
                    .withRegion(region)
                    .withCredentials(new AWSStaticCredentialsProvider(awsCreds))
                    .build();

            // Download image from S3
            S3Object s3Object = s3.getObject(bucketName, objectKey);
            S3ObjectInputStream inputStream = s3Object.getObjectContent();
            ByteBuffer imageBytes = ByteBuffer.wrap(inputStream.readAllBytes());

            // Initialize Rekognition client
            AmazonRekognition rekognitionClient = AmazonRekognitionClientBuilder.standard()
                    .withRegion(region)
                    .withCredentials(new AWSStaticCredentialsProvider(awsCreds))
                    .build();

            // Detect labels in the image
            DetectLabelsRequest request = new DetectLabelsRequest()
                    .withImage(new Image().withBytes(imageBytes))
                    .withMaxLabels(10)
                    .withMinConfidence(75F);

            DetectLabelsResult result = rekognitionClient.detectLabels(request);

            // Display detected labels
            List<Label> labels = result.getLabels();
            System.out.println("Detected labels:");
            for (Label label : labels) {
                System.out.println(label.getName() + ": " + label.getConfidence());
            }

            // Cleanup
            inputStream.close();

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            scanner.close();
        }
    }
}
