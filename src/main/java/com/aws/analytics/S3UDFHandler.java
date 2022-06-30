/*-
 * #%L
 * athena-udfs
 * %%
 * Copyright (C) 2019 Amazon Web Services
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */
package com.aws.analytics;

import com.amazonaws.SdkClientException;
import com.amazonaws.athena.connector.lambda.handlers.UserDefinedFunctionHandler;
import com.amazonaws.athena.connector.lambda.security.CachableSecretsManager;
import com.amazonaws.auth.profile.ProfileCredentialsProvider;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.GetObjectRequest;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.services.secretsmanager.AWSSecretsManagerClient;
import com.google.common.annotations.VisibleForTesting;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.stream.Collectors;

/**
mvn clean package  -Dcheckstyle.skip -DskipTests

aws lambda create-function --function-name s3_udf \
--runtime java11 --role arn:aws:iam::305996241648:role/lambda-ex \
--handler com.aws.analytics.S3UDFHandler --timeout 900 \
--zip-file fileb://./target/athena-udfs-2022.24.1.jar


aws lambda update-function-code --function-name s3_udf \
--zip-file fileb://./target/athena-udfs-2022.24.1.jar


 USING EXTERNAL FUNCTION s3union(col1 varchar)
 RETURNS varchar
 LAMBDA 's3_udf'
 select s3union(a) from (select 'hello' as a)


 */
public class S3UDFHandler
        extends UserDefinedFunctionHandler
{
    private static final String SOURCE_TYPE = "athena_common_udfs";

    private static  String dimData = "";

    private final CachableSecretsManager cachableSecretsManager;

    public S3UDFHandler()
    {
        this(new CachableSecretsManager(AWSSecretsManagerClient.builder().build()));
        readFile();
    }

    @VisibleForTesting
    S3UDFHandler(CachableSecretsManager cachableSecretsManager)
    {
        super(SOURCE_TYPE);
        this.cachableSecretsManager = cachableSecretsManager;
    }

    public static void readFile() {
        Regions clientRegion = Regions.AP_SOUTHEAST_1;
        String bucketName = "app-util";
        String key = "test.json";

        S3Object fullObject = null, objectPortion = null, headerOverrideObject = null;
        try {
            AmazonS3 s3Client = AmazonS3ClientBuilder.standard()
                    .withRegion(clientRegion)
//                    .withCredentials(new ProfileCredentialsProvider())
                    .build();

            fullObject = s3Client.getObject(new GetObjectRequest(bucketName, key));

            BufferedReader reader = new BufferedReader(new InputStreamReader(fullObject.getObjectContent()));
            String content = reader.lines().collect(Collectors.joining());
            dimData = content;
        } catch (SdkClientException e) {
            e.printStackTrace();
        }
        finally {
            if (fullObject != null) {
                try {
                    fullObject.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

    }

    public String s3union(String input)
    {
        return dimData +":" +input;

    }
//    public static void main(String[] args) throws IOException {
//        System.out.println(new S3UDFHandler().s3union("hello"));
//    }

}
