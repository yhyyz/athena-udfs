### usage
```shell
mvn clean package  -Dcheckstyle.skip -DskipTests

# 创建函数, 注意替换role和环境变量中指定的s3文件
aws lambda create-function --function-name s3_udf \
--runtime java11 --role arn:aws:iam::xxxxx:role/lambda-ex \
--handler com.aws.analytics.S3UDFHandler --timeout 900 \
--environment "Variables={REGION=ap-southeast-1,BUCKET=app-util,KEY=test.json}" \
--zip-file fileb://./target/athena-udfs-2022.24.1.jar

# 发布 
aws lambda  publish-version --function-name s3_udf

# 更新环境变量
aws lambda update-function-configuration --function-name s3_udf \
 --environment "Variables={REGION=ap-southeast-1,BUCKET=app-util,KEY=test.json}"

# 更新代码
aws lambda update-function-code --function-name s3_udf \
--zip-file fileb://./target/athena-udfs-2022.24.1.jar

# athena中使用
USING EXTERNAL FUNCTION s3union(col1 varchar)
RETURNS varchar
LAMBDA 's3_udf'
select s3union(a) from (select 'hello' as a)


```

