### usage
```shell
mvn clean package  -Dcheckstyle.skip -DskipTests

aws lambda create-function --function-name s3_udf \
--runtime java11 --role arn:aws:iam::xxxxxx:role/lambda-ex \
--handler com.aws.analytics.S3UDFHandler --timeout 900 \
--zip-file fileb://./target/athena-udfs-2022.24.1.jar


aws lambda update-function-code --function-name s3_udf \
--zip-file fileb://./target/athena-udfs-2022.24.1.jar


 USING EXTERNAL FUNCTION s3union(col1 varchar)
 RETURNS varchar
 LAMBDA 's3_udf'
 select s3union(a) from (select 'hello' as a)

```