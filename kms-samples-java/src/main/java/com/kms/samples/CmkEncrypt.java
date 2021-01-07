package com.kms.samples;

import com.aliyuncs.DefaultAcsClient;
import com.aliyuncs.exceptions.ClientException;
import com.aliyuncs.http.FormatType;
import com.aliyuncs.http.HttpClientConfig;
import com.aliyuncs.http.MethodType;
import com.aliyuncs.http.ProtocolType;
import com.aliyuncs.kms.model.v20160120.EncryptRequest;
import com.aliyuncs.kms.model.v20160120.EncryptResponse;
import com.aliyuncs.profile.DefaultProfile;
import com.aliyuncs.profile.IClientProfile;

import java.io.*;
import java.nio.charset.StandardCharsets;

public class CmkEncrypt {
    private static DefaultAcsClient kmsClient;

    private static DefaultAcsClient kmsClient(String regionId, String accessKeyId, String accessKeySecret) {
        IClientProfile profile = DefaultProfile.getProfile(regionId, accessKeyId, accessKeySecret);
        HttpClientConfig clientConfig = HttpClientConfig.getDefault();
        //clientConfig.setIgnoreSSLCerts(true);
        profile.setHttpClientConfig(clientConfig);

        return new DefaultAcsClient(profile);
    }

    private static String kmsEncrypt(String keyAlias, String plaintext) throws ClientException {
        final EncryptRequest request = new EncryptRequest();
        request.setSysProtocol(ProtocolType.HTTPS);
        request.setAcceptFormat(FormatType.JSON);
        request.setSysMethod(MethodType.POST);
        request.setKeyId(keyAlias);
        request.setPlaintext(plaintext);
        EncryptResponse response = kmsClient.getAcsResponse(request);
        return response.getCiphertextBlob();
    }

    private static String readTextFile(String inFile) throws IOException {
        File file = new File(inFile);
        try (InputStream in = new FileInputStream(file)) {
            long len = file.length();
            byte[] data = new byte[(int) len];
            in.read(data);
            return new String(data, StandardCharsets.UTF_8);
        }
    }

    private static void writeTextFile(String outFile, String content) throws IOException {
        File file = new File(outFile);
        try (FileOutputStream fos = new FileOutputStream(file)) {
            fos.write(content.getBytes());
        }
    }

    public static void main(String[] args) {
        String regionId = "cn-hangzhou";
        String accessKeyId = System.getenv("AccessKeyId");
        String accessKeySecret = System.getenv("AccessKeySecret");

        kmsClient = kmsClient(regionId, accessKeyId, accessKeySecret);

        String keyAlias = "alias/Apollo/WorkKey";
        String inFile = "./certs/key.pem";
        String outFile = "./certs/key.pem.cipher";

        try {
            //Read encrypted key file in text mode
            String inContent = readTextFile(inFile);

            //Decrypt
            String cipherText = kmsEncrypt(keyAlias, inContent);

            //Write Decrypted key file in text mode
            writeTextFile(outFile, cipherText);

        } catch (ClientException e){
            System.out.println("Failed.");
            System.out.println("Error code: " + e.getErrCode());
            System.out.println("Error message: " + e.getErrMsg());
        } catch (IOException e){
            e.printStackTrace();
        }
    }
}
