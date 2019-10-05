/**
 * Copyright (C) 2018 Amway-confidential- All rights reserved.
 *
 * This file is part of Amway project.
 */
package com.amway.kinesis.consumer;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;
import java.util.UUID;

import org.apache.commons.lang.StringUtils;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.ParseException;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpResponseException;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.json.JSONException;
import org.json.JSONObject;
import org.joda.time.LocalDateTime;

import com.amazonaws.ClientConfiguration;
import com.amazonaws.Protocol;
import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.auth.DefaultAWSCredentialsProviderChain;
import com.amazonaws.kinesis.agg.AggRecord;
import com.amazonaws.kinesis.agg.RecordAggregator;
import com.amazonaws.services.kinesis.AmazonKinesis;
import com.amazonaws.services.kinesis.AmazonKinesisClient;
import com.amazonaws.services.kinesis.clientlibrary.types.UserRecord;
import com.amazonaws.services.kinesis.model.PutRecordRequest;
import com.amazonaws.services.kinesis.model.PutRecordResult;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.LambdaLogger;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.KinesisEvent;
import com.amazonaws.services.lambda.runtime.events.KinesisEvent.KinesisEventRecord;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.amway.kinesis.util.LambdaConstants;
import com.amway.kinesis.util.LambdaUtil;
import com.amway.kinesis.util.PayloadGenerator;
import com.amway.kinesis.util.PropertyDecriptor;
import com.amway.kinesis.util.SNSUtil;
import com.google.common.net.HttpHeaders;

/**
 * The Class KinesisLambdaReceiver for Hybris Consignment TMS enrichment. This
 * is an AWS Kinesis stream event handler which will receive data from
 * Consignment Stream and enrich the TMS data and re-push into TMS encriched
 * stream.
 */
public class KinesisLambdaReceiver implements RequestHandler<KinesisEvent, Void> {

	/** The payload prefix. */
	String payloadPrefix = System.getenv(LambdaConstants.ERROR_PAYLOAD_PREFIX);

	/** The s3 bucket name. */
	String s3BucketName = System.getenv(LambdaConstants.S3_BUCKET_NAME);

	/** The api Host. */
	String apiHost = System.getenv(LambdaConstants.API_HOST);
	
	static String cognitoHost = System.getenv(LambdaConstants.COGNITO_HOST);

	/** The reconKinesisStream. */
	String reconKinesisStream = System.getenv(LambdaConstants.RECON_KINESIS_STREAM);

	/** The Destination Stream Region. */

	//static String destinationStreamRegion = System.getenv("AWS_DEFAULT_REGION");
	static String destinationStreamRegion = "ap-southeast-1";

	/** The Destination Stream Name. */
	String destinationStreamName = System.getenv(LambdaConstants.DESTINATION_STREAM_NAME);

	/** The Destination Connection Timeout. */
	static String destinationConnectionTimeout = System.getenv(LambdaConstants.DESTINATION_CONNECTION_TIMEOUT);

	/** The Destination Socket Timeout. */
	static String destinationSocketTimeout = System.getenv(LambdaConstants.DESTINATION_SOCKET_TIMEOUT);

	/** The maxConnections. */
	static String maxConnections = System.getenv(LambdaConstants.MAX_CONNECTIONS);

	/** The generateReconData. */
	static String generateReconData = System.getenv(LambdaConstants.GENERATE_RECON_DATA);
	
	/** The affliateCode. */
	static String AFFILIATE_CODE = System.getenv(LambdaConstants.VNM_AFFILIATE_CODE);
	
	static String clientId= System.getenv(LambdaConstants.CLIENT_ID_VAR);	
	
	static String clientSecret= System.getenv(LambdaConstants.CLIENT_SECRET_VAR);
	
	static String clientScope= System.getenv(LambdaConstants.CLIENT_SCOPE);
	/** KinesisForwarder for put records to kinesis stream */
	private static AmazonKinesis kinesisForwarder;

	private RecordAggregator aggregator;

	/* AWS Kinesis Configuration */
	static AWSCredentialsProvider provider = new DefaultAWSCredentialsProviderChain();

	SimpleDateFormat sdf = new SimpleDateFormat(LambdaConstants.DATE_FORMAT);

	/* AWS Connection & Client Configuration */
	static {
		if (provider != null) {
			ClientConfiguration kinesisConfig = new ClientConfiguration()
					.withMaxConnections(Integer.parseInt(maxConnections)).withProtocol(Protocol.HTTPS)
					.withConnectionTimeout(Integer.parseInt(destinationConnectionTimeout))
					.withSocketTimeout(Integer.parseInt(destinationSocketTimeout));
			kinesisForwarder = AmazonKinesisClient.builder().withCredentials(provider)
					.withClientConfiguration(kinesisConfig).withRegion(destinationStreamRegion).build();
		}

	}
	
	static CloseableHttpClient httpClient = HttpClientBuilder.create().build();
	 /*Get Token from Cognito for accessing the Metapack API */
	 static HttpPost requestGetToken = new HttpPost(cognitoHost);

	
	static ArrayList<NameValuePair> postParameters = new ArrayList<NameValuePair>();
	static{
	postParameters.add(new BasicNameValuePair(LambdaConstants.GRANT_TYPE, LambdaConstants.CLIENT_CREDENTIALS));
    postParameters.add(new BasicNameValuePair(LambdaConstants.CLIENT_ID, clientId));
    postParameters.add(new BasicNameValuePair(LambdaConstants.CLIENT_SECRET, clientSecret));
    postParameters.add(new BasicNameValuePair(LambdaConstants.SCOPE, clientScope));
    try {
		requestGetToken.setEntity(new UrlEncodedFormEntity(postParameters,  LambdaConstants.UTF8));
	} catch (UnsupportedEncodingException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	}
	}
    static JSONObject jsonResponseGetToken = null;
	static HttpResponse responseGetToken =null;
    static String jsonGetToken = null;
	static {
    /* API Call to get the token */
	try{
     responseGetToken = httpClient.execute(requestGetToken);
     jsonGetToken = EntityUtils.toString(responseGetToken.getEntity());
		jsonResponseGetToken = new JSONObject(jsonGetToken);
	}catch (UnsupportedEncodingException e) {
		e.printStackTrace();
	} catch (ClientProtocolException e) {
		e.printStackTrace();
	} catch (IOException e) {
		e.printStackTrace();
	}
	catch (JSONException e1) {
			e1.printStackTrace();
	}
   
   
	}
	

	@Override
	public Void handleRequest(KinesisEvent event, Context context) throws RuntimeException {
		LambdaLogger logger = context.getLogger();
		Map<String,String> consignment_Event_list = new HashMap<>();
		if (event != null) {
			int numberOfRecords = event.getRecords().size();			
			logger.log(String.format("Received %d raw Event Records , AWS RequestID: %s", numberOfRecords,
					context.getAwsRequestId()));

			try {

				if (kinesisForwarder != null) {
					consignment_Event_list = populateCarrierCode(event, context);
					if (generateReconData.equals(LambdaConstants.TRUE)) {
						generate_event_statusdata(logger, consignment_Event_list, context);
					}
				} else {

					ClientConfiguration kinesisConfig = new ClientConfiguration()
							.withMaxConnections(Integer.parseInt(maxConnections)).withProtocol(Protocol.HTTPS)
							.withConnectionTimeout(Integer.parseInt(destinationConnectionTimeout))
							.withSocketTimeout(Integer.parseInt(destinationSocketTimeout));

					kinesisForwarder = AmazonKinesisClient.builder().withCredentials(provider)
							.withClientConfiguration(kinesisConfig).withRegion(destinationStreamRegion).build();

					if (kinesisForwarder != null) {
						consignment_Event_list = populateCarrierCode(event, context);
						if (generateReconData.equals(LambdaConstants.TRUE)) {
							generate_event_statusdata(logger, consignment_Event_list, context);
						}
					} else {

						String errorMessage = String.format(
								"Kinesis forwarder object could not initialized. Function name: %s%s",
								System.getenv(LambdaConstants.AWS_LAMBDA_FUNCTION_NAME), " has been disabled.");
						logger.log(errorMessage);
						LambdaUtil.disableLambdaFunction(logger);
						throw new Exception(errorMessage);
					}
				}

			} catch (Exception e) {

				String errorMessage = String.format(
						"Exception occurred while processing records.Error: %s, AWS RequestID: %s", e.getMessage(),
						context.getAwsRequestId());
				logger.log(errorMessage);
				SNSUtil.pushNotificationExceptionOccurred(logger, errorMessage,
						String.format(LambdaConstants.ERROR_PROCESSING_RECORDS_NOTIFICATION_SUBJECT,
								System.getenv(LambdaConstants.AWS_LAMBDA_FUNCTION_NAME)));
				throw new RuntimeException(e);
			}

		} else {
			logger.log(String.format("EventObject not found. AWS RequestID: %s", context.getAwsRequestId()));
		}

		return null;
	}

	/**
	 * Method to Check if the input aggregated record is complete and pushed
	 * into the "ApacVnmDevHybrisConsignmentTmsEnriched" stream.
	 * 
	 * @param logger
	 *            The LambdaLogger from the input Context
	 * 
	 * @param aggRecord
	 *            The aggregated record to transmit or null if the record isn't
	 *            full yet.
	 */
	private void checkAndForwardRecords(LambdaLogger logger, AggRecord aggRecord, Context context)

	{
		logger.log(String.format("Received %d aggregated record for Kinesis Stream to push,AWS RequestID: %s",
				aggRecord.getNumUserRecords(), context.getAwsRequestId()));

		PutRecordRequest request = aggRecord.toPutRecordRequest(destinationStreamName);
		PutRecordResult result = null;

		try {
			result = kinesisForwarder.putRecord(request);
			if (result == null || result.getShardId() == null)
				throw new RuntimeException(String.format(
						"Could not publish the records into tms enriched kinesis stream,AWS RequestID: %s",
						context.getAwsRequestId()));
			else {
				logger.log(String.format("Successfully published record : shard %s,AWS RequestID: %s",
						result.getShardId(), context.getAwsRequestId()));
			}

		} catch (Exception e) {
			String errorMessage = String.format("Failed to push: shard %s,AWS RequestID: %s", result.getShardId(),
					context.getAwsRequestId());
			logger.log(errorMessage);
			SNSUtil.pushNotificationExceptionOccurred(logger, errorMessage,
					String.format(LambdaConstants.ERROR_PUSHING_RECORDS_KINESISSTREAM_NOTIFICATION_SUBJECT,
							System.getenv(LambdaConstants.AWS_LAMBDA_FUNCTION_NAME)));
		}

	}

	/**
	 * Upload payload to S3 Bucket.
	 *
	 * @param logger
	 *            the logger
	 * @param consignmentId
	 *            the order number
	 * @param payload
	 *            the payload
	 * @param payloadPrefix
	 *            the payload prefix
	 * @param s3BucketName
	 *            the s3 bucket name
	 * @return the string
	 */
	public String uploadErrorPayloadToS3(LambdaLogger logger, String consignmentId, String payload,
			String payloadPrefix, String s3BucketName) {

		String payloadFileName = String.format(((StringUtils.isNotBlank(consignmentId) ? consignmentId : "ErrorPayload"))
				.concat(LambdaConstants._UNDERSCORE).concat("%s"), System.currentTimeMillis());

		logger.log(String.format("Uploading error payload into S3 Bucket. PayloadFileName: %s", payloadFileName));

		AmazonS3 s3Client = AmazonS3Client.builder().build();

		if (s3Client.doesBucketExistV2(s3BucketName)) {

			LocalDateTime localDateTime = new LocalDateTime();

			String err_file_path = String.format(s3Client.getBucketLocation(s3BucketName)
					.concat(LambdaConstants.FORWARD_SLASH).concat(s3BucketName).concat(LambdaConstants.FORWARD_SLASH)
					.concat(payloadFileName).concat(LambdaConstants.JSON_EXTENSION));

			try {

				logger.log(String.format("s3BucketName : %s, PayloadPrefix : %s", s3BucketName, payloadPrefix));

            	PutObjectRequest request = new PutObjectRequest(s3BucketName,
						String.format("%s/%s/%s/%s/%s%s", payloadPrefix, String.format("year=%d", localDateTime.getYear()),
								String.format("month=%d", localDateTime.getMonthOfYear()),String.format("day=%d", localDateTime.getDayOfMonth()),
								payloadFileName, LambdaConstants.JSON_EXTENSION),
						PayloadGenerator.createPayloadFile(payloadFileName, payload, LambdaConstants.JSON_EXTENSION));

				s3Client.putObject(request);
			} catch (IOException e) {
				String message = String.format(
						"Not able to create payload file for S3 Bucket. Error File Name : %s, Error : %s",
						payloadFileName, e.getMessage());
				logger.log(message);
				SNSUtil.pushNotificationS3Upload(logger, message);
			} catch (RuntimeException e) {
				String message = String.format("Not able to put payload on S3 Bucket. Error File Name : %s, Error : %s",
						payloadFileName, e.getMessage());
				logger.log(message);
				SNSUtil.pushNotificationS3Upload(logger, message);
			}

			return err_file_path;
		} else {
			logger.log(String.format("S3 Bucket not found."));
		}

		return LambdaConstants.ERROR;
	}

	/**
	 * Gets the API records and Push into destination stream.
	 *
	 * @param event
	 *            the event
	 * @param logger
	 *            the logger
	 * @return the lambda records
	 * @throws Exception
	 *             the exception
	 */
	private Map<String,String> populateCarrierCode(KinesisEvent event, Context context) throws Exception {
		LambdaLogger logger = context.getLogger();
		logger.log(String.format("Populate Carrier Code - START: %s, AWS RequestID: %s",
				LocalDateTime.now(), context.getAwsRequestId()));

		aggregator = new RecordAggregator();

		String entityID = null;
		String consignmentId = null;
		Map<String,String> reconList= new HashMap<>();
		CloseableHttpClient httpClientServ = HttpClientBuilder.create().build();
//		/*Get Token from Cognito for accessing the Metapack API */

	    String accessToken = jsonResponseGetToken.getString(LambdaConstants.ACCESS_TOKEN);
	    String tokenType = jsonResponseGetToken.getString(LambdaConstants.TOKEN_TYPE);
		logger.log(jsonResponseGetToken.toString());
		for (KinesisEventRecord record : event.getRecords()) {
			List<UserRecord> userRecordList = deaggregate(record);
			for (UserRecord userRecord : userRecordList) {

				String jsonPayload = new String(userRecord.getData().array());

				HttpResponse response = null;

				if (StringUtils.isEmpty(jsonPayload)) {
					SNSUtil.pushNotificationJSONPayloadNotFound(logger);
					continue;
				}
				
				
				try {
					logger.log(jsonPayload.toString());
					/* Getting post request parameters from payload */
					JSONObject jsonObject = new JSONObject(jsonPayload);
					JSONObject bodyParams = jsonObject.getJSONObject(LambdaConstants.ENTITY);

					entityID = jsonObject.getJSONObject(LambdaConstants.EVENT_INFO)
							.getString(LambdaConstants.ENTITY_ID);
					consignmentId = bodyParams.getString(LambdaConstants.CONSIGNMENT_ID);

					logger.log(String.format("EntityID: %s, ConsignmentId : %s,AWS RequestID: %s ", entityID,
							consignmentId, context.getAwsRequestId()));

					String state = null, country = null,serviceLevel = null;
					JSONObject deliveryAddress = bodyParams.getJSONObject(LambdaConstants.DELIVERY_ADDRESS);

					if (!deliveryAddress.isNull(LambdaConstants.STATE)
							&& StringUtils.isNotBlank(deliveryAddress.getString(LambdaConstants.STATE))) {
						state = deliveryAddress.getString(LambdaConstants.STATE);
					}
					if (!deliveryAddress.isNull(LambdaConstants.ISO_COUNTRY_CODE)
							&& StringUtils.isNotBlank(deliveryAddress.getString(LambdaConstants.ISO_COUNTRY_CODE))) {
						country = deliveryAddress.getString(LambdaConstants.ISO_COUNTRY_CODE);
					}
					if (!bodyParams.isNull(LambdaConstants.REQUESTED_DELIVERY_SERVICE_LEVEL) && StringUtils
							.isNotBlank(bodyParams.getString(LambdaConstants.REQUESTED_DELIVERY_SERVICE_LEVEL))) {
						serviceLevel = bodyParams.getString(LambdaConstants.REQUESTED_DELIVERY_SERVICE_LEVEL);
					}
					logger.log(String.format(
							"Post Request for Carrier and push to destination kinesis stream- START: %s,EntityID: %s, ConsignmentId : %s, AWS RequestID: %s",
							LocalDateTime.now(), entityID, consignmentId, context.getAwsRequestId()));
					/*
					 * HTTP Post Request to get Carrier code from the
					 * Payload
					 */
					HttpPost request = new HttpPost(apiHost);

					/* API Post Request Parameters */
					JSONObject requestJSON = new JSONObject();
					requestJSON.put(LambdaConstants.STATE_REQ, state);
					requestJSON.put(LambdaConstants.COUNTRY_REQ, country);
					requestJSON.put(LambdaConstants.SERVICE_LEVEL, serviceLevel);

					logger.log(String.format(
							"Requested Json Parameters for Carrier Code API POST Request: %s%n,EntityID: %s, ConsignmentId : %s,AWS RequestID: %s",
							requestJSON.toString(), entityID, consignmentId, context.getAwsRequestId()));

					StringEntity params = new StringEntity(requestJSON.toString(), LambdaConstants.UTF8);
					request.addHeader(HttpHeaders.CONTENT_TYPE, LambdaConstants.APPLICATION_JSON);
					request.addHeader(HttpHeaders.AUTHORIZATION,tokenType+" "+accessToken);
					request.setEntity(params);

					/* API Call to get the response */
					response = httpClientServ.execute(request);

					logger.log(String.format(
							"Post Request for Carrier code and push to destination kinesis stream- END: %s,EntityID: %s, ConsignmentId : %s, AWS RequestID: %s",
							LocalDateTime.now(), entityID, consignmentId, context.getAwsRequestId()));

					int code = response.getStatusLine().getStatusCode();
					logger.log(String.format(
							"Response Code received from Carrier code API: %d,EntityID: %s, ConsignmentId : %s, AWS RequestID: %s",
							code, entityID, consignmentId, context.getAwsRequestId()));

					/*
					 * If Post Response Success then it will process to push the
					 * new payload to destination stream
					 */
					if (code == LambdaConstants.RESPONSE_CODE && serviceLevel != null && !"null".equals(serviceLevel)) {

						String json = EntityUtils.toString(response.getEntity());

						logger.log(String.format(
								"Response Json received from Carrier code API: %s,EntityID: %s, ConsignmentId : %s,AWS RequestID: %s",
								json, entityID, consignmentId, context.getAwsRequestId()));

						JSONObject jsonResponse = new JSONObject(json);

						if (!StringUtils.isEmpty(json) && !jsonResponse.has(LambdaConstants.MESSAGE)) {

							String carrier = jsonResponse.getString(LambdaConstants.CARRIER);

							JSONObject append = jsonObject.getJSONObject(LambdaConstants.ENTITY);
							append.put(LambdaConstants.DELIVERY_CARRIER_CODE,
									jsonResponse.getString(LambdaConstants.CARRIER));

							JSONObject contextEventInfo = new JSONObject();
							contextEventInfo.put(LambdaConstants.ORIGIN_SOURCE_APPLICATION,
									jsonObject.getJSONObject(LambdaConstants.EVENT_INFO)
											.getString(LambdaConstants.SOURCE_APPLICATION_JSON));
							contextEventInfo.put(LambdaConstants.ORIGIN_SOURCE_TIMESTAMP,
									jsonObject.getJSONObject(LambdaConstants.EVENT_INFO)
											.getString(LambdaConstants.SOURCE_TIMESTAMP));
							contextEventInfo.put(LambdaConstants.ORIGIN_SOURCE_EVENTID,
									jsonObject.getJSONObject(LambdaConstants.EVENT_INFO)
											.getString(LambdaConstants.SOURCE_EVENT_ID_JSON));
							
							String hybrisSourceTimestamp = jsonObject.getJSONObject(LambdaConstants.EVENT_INFO)
									.getString(LambdaConstants.SOURCE_TIMESTAMP);  
						    ZonedDateTime zDateTime = ZonedDateTime.parse(hybrisSourceTimestamp, DateTimeFormatter.ISO_ZONED_DATE_TIME);            
						    ZoneId zone = zDateTime.getZone();
						    String timeZone=LambdaConstants.GMT.concat(zone.getId());

							Timestamp time = new Timestamp(System.currentTimeMillis());
							sdf.setTimeZone(TimeZone.getTimeZone(timeZone.trim()));
							String sourceTimestamp = sdf.format(time);

							String sourceEventID = UUID.randomUUID().toString();
							/*
							 * Updating Event Info to final TMS Enriched Payload
							 */
							JSONObject appendEventinfo = jsonObject.getJSONObject(LambdaConstants.EVENT_INFO);
							appendEventinfo.put(LambdaConstants.SOURCE_APPLICATION_JSON,
									LambdaConstants.SOURCE_APPLICATION);
							appendEventinfo.put(LambdaConstants.SOURCE_HOST_JSON, LambdaConstants.SOURCE_HOST);
							appendEventinfo.put(LambdaConstants.SOURCE_EVENT_ID_JSON, sourceEventID);
							appendEventinfo.put(LambdaConstants.SOURCE_TIMESTAMP, sourceTimestamp);
							appendEventinfo.put(LambdaConstants.CONTEXT, contextEventInfo);

							logger.log(String.format(
									"EntityId: %s,ConsignmentId: %s, Carrier :  %s,AWS RequestID: %s",
									entityID, consignmentId, carrier, context.getAwsRequestId()));

							logger.log(String.format(
									"Final Payload of TMS Enriched: %s,EntityId: %s,ConsignmentId: %s,AWS RequestID: %s",
									jsonObject.toString(), entityID, consignmentId, context.getAwsRequestId()));

							/* Adding Payload to the Recon List */
							reconList.put(jsonObject.toString(),null);

							/*
							 * New Payload pushing to
							 * "ApacVnmDevHybrisConsignmentTmsEnriched" Stream
							 */

							ByteBuffer data = ByteBuffer
									.wrap((jsonObject.toString() + "\n").getBytes(LambdaConstants.UTF_8));
							userRecord.setData(data);
							aggregator.addUserRecord(userRecord);
							logger.log(String.format(
									"Final Payload added to aggregator list for TMS Enriched Stream,EntityId: %s,ConsignmentId: %s,AWS RequestID: %s",
									entityID, consignmentId, context.getAwsRequestId()));
							
						} else {
							
							String noRecord = String.format(
									"Response from Carrier Code API received as %s,EntityId: %s,ConsignmentId: %s,AWS RequestID: %s",
									jsonResponse.getString(LambdaConstants.MESSAGE), entityID, consignmentId,
									context.getAwsRequestId());
							/* Adding Payload to the Recon List */
							reconList.put(jsonPayload,noRecord);
							logger.log(noRecord);
							
							uploadErrorPayloadToS3(logger, entityID, jsonPayload, payloadPrefix, s3BucketName);
							SNSUtil.pushNotificationByConsignmentID(consignmentId, entityID, logger, noRecord,
									String.format(LambdaConstants.ERROR_NO_RECORD_FOUND_NOTIFICATION_SUBJECT,
											System.getenv(LambdaConstants.AWS_LAMBDA_FUNCTION_NAME)));
						}

			} 
					else {
						String errorMessage = null;
						if ((serviceLevel == null || "null".equals(serviceLevel))
								&& code == LambdaConstants.RESPONSE_CODE) {
							errorMessage = String.format(
									"Service Level element received NULL in the Consignment Stream ,EntityId: %s,ConsignmentId: %s,AWS RequestID: %s",
									entityID, consignmentId, context.getAwsRequestId());
							/* Adding Payload to the Recon List */
							reconList.put(jsonPayload,errorMessage);							
							logger.log(errorMessage);
							uploadErrorPayloadToS3(logger, entityID, jsonPayload, payloadPrefix, s3BucketName);
							SNSUtil.pushNotificationByConsignmentID(consignmentId, entityID, logger, errorMessage,
									String.format(LambdaConstants.ERROR_SERVICELEVEL_NULL_SUBJECT,
											System.getenv(LambdaConstants.AWS_LAMBDA_FUNCTION_NAME)));
						} else {
							errorMessage = String.format(
									"Response from API request received as Failure Reponse : %s,Response Status Code: %d%n ,Response Message: %s,EntityId: %s,ConsignmentId: %s,AWS RequestID: %s",
									response.toString(), response.getStatusLine().getStatusCode(),
									response.getStatusLine().getReasonPhrase(), entityID, consignmentId,
									context.getAwsRequestId());
							/* Adding Payload to the Recon List */
							reconList.put(jsonPayload,errorMessage);
							logger.log(errorMessage);
							uploadErrorPayloadToS3(logger, entityID, jsonPayload, payloadPrefix, s3BucketName);
							SNSUtil.pushNotificationByConsignmentID(consignmentId, entityID, logger, errorMessage,
									String.format(LambdaConstants.ERROR_CARRIERCODE_API_NOTIFICATION_SUBJECT,
											System.getenv(LambdaConstants.AWS_LAMBDA_FUNCTION_NAME)));
						}
					}

				} catch (JSONException e) {
					String errorMessage = String.format(
							"Exception occurred while in JSONObject. ConsignmentId : %s, EntityId : %s,AWS RequestID: %s ,ErrorMessage: %s",
							String.valueOf(consignmentId), String.valueOf(entityID), context.getAwsRequestId(),
							e.getMessage());
					/* Adding Payload to the Recon List */
					reconList.put(jsonPayload,errorMessage);
					logger.log(errorMessage);
					uploadErrorPayloadToS3(logger, entityID, jsonPayload, payloadPrefix, s3BucketName);
					SNSUtil.pushNotificationIncorrectJSON(logger, errorMessage,
							String.format(LambdaConstants.ERROR_NOTIFICATION_SUBJECT,
									System.getenv(LambdaConstants.AWS_LAMBDA_FUNCTION_NAME)));

					continue;
				}
				catch (HttpResponseException e) {

					String errorMessage = String.format(
							"Response from Carrier Code API Exception Status Code: %d%n ,Response Exception Message: %s,ErrorMessage: %s,EntityId: %s,ConsignmentId: %s,AWS RequestID: %s ",
							response.getStatusLine().getStatusCode(), response.getStatusLine().getReasonPhrase(),
							e.getMessage(), entityID, consignmentId, context.getAwsRequestId());
					/* Adding Payload to the Recon List */
					reconList.put(jsonPayload,errorMessage);
					/* Post API request Exception */
					logger.log(errorMessage);
					uploadErrorPayloadToS3(logger, entityID, jsonPayload, payloadPrefix, s3BucketName);
					SNSUtil.pushNotificationByConsignmentID(consignmentId, entityID, logger, errorMessage,
							String.format(LambdaConstants.ERROR_HTTP_RESPONSE_EXCEPTION_NOTIFICATION_SUBJECT,
									System.getenv(LambdaConstants.AWS_LAMBDA_FUNCTION_NAME)));
					continue;
				} 
					catch (Exception e) {

					String errorMessage = String.format(
							"Exception occurred while traversing kinesis events. ConsignmentId : %s ,EntityID : %s ,AWS RequestID: %s,ErrorMessage: %s",
							String.valueOf(consignmentId), String.valueOf(entityID), context.getAwsRequestId(),
							e.getMessage());					
					/* Adding Payload to the Recon List */
					reconList.put(jsonPayload,errorMessage);
					logger.log(errorMessage);
					uploadErrorPayloadToS3(logger, entityID, jsonPayload, payloadPrefix, s3BucketName);
					SNSUtil.pushNotificationByConsignmentID(consignmentId, entityID, logger, errorMessage,
							String.format(LambdaConstants.ERROR_POPULATING_CARRIERCODE_NOTIFICATION_SUBJECT,
									System.getenv(LambdaConstants.AWS_LAMBDA_FUNCTION_NAME)));
					continue;
				}

			}
		}
		/* Releasing the HTTP Post Request connection */
		if (httpClient != null) {

			httpClient.close();
		}
		if (aggregator.getNumUserRecords() > 0) {
			checkAndForwardRecords(logger, aggregator.clearAndGet(), context);
		} else {
			logger.log(String.format("No record found for pushing into TMS Enriched Stream, AWSRequestID: %s",
					context.getAwsRequestId()));
		}
		logger.log(String.format("Populate Carrier Code - END: %s, AWS RequestID: %s", LocalDateTime.now(),
				context.getAwsRequestId()));
		return reconList;
	}

	/**
	 * Method to deaggregate a single Kinesis record into one or more Kinesis
	 * user records.
	 * 
	 * @param inputRecord
	 *            The single KinesisEventRecord to deaggregate
	 * @return A list of Kinesis UserRecord objects obtained by deaggregating
	 *         the input KinesisEventRecord
	 */
	public static List<UserRecord> deaggregate(KinesisEventRecord inputRecord) {
		return UserRecord.deaggregate(Arrays.asList(inputRecord.getKinesis()));
	}

	/**
	 * Method to generate reconcilation event infomation user records.
	 * 
	 * @param country_code
	 *            Generating the event info
	 * @return eventinfo for the particular record
	 * 
	 */
	public JSONObject generate_recon_eventinfo(LambdaLogger logger, String country_code, String timeZone,
			Context context) {

		Timestamp time = new Timestamp(System.currentTimeMillis());
		sdf.setTimeZone(TimeZone.getTimeZone(timeZone));
		String sourceTimestamp = sdf.format(time);

		JSONObject eventinfo = new JSONObject();
		try {
			eventinfo.put(LambdaConstants.SOURCE_APPLICATION_JSON, LambdaConstants.SOURCE_APPLICATION);
			eventinfo.put(LambdaConstants.SOURCE_TIMESTAMP, sourceTimestamp);
			eventinfo.put(LambdaConstants.SOURCE_HOST_JSON, JSONObject.NULL);
			eventinfo.put(LambdaConstants.SOURCE_EVENT_ID_JSON, UUID.randomUUID().toString());
			eventinfo.put(LambdaConstants.ENTITY_TYPE_JSON, LambdaConstants.EVENT_RECONCILIATION);
			eventinfo.put(LambdaConstants.ENTITY_ID, JSONObject.NULL);
			eventinfo.put(LambdaConstants.EVENT_TYPE_JSON, LambdaConstants.CONSIGNMENT_RECON);
			if (AFFILIATE_CODE != null && AFFILIATE_CODE != "") {
				eventinfo.put(LambdaConstants.AFFILIATE_CODE, AFFILIATE_CODE);
			} else {
				eventinfo.put(LambdaConstants.AFFILIATE_CODE, JSONObject.NULL);
			}
			eventinfo.put(LambdaConstants.ISO_COUNTRY_CODE, country_code);
			eventinfo.put(LambdaConstants.CONTEXT, JSONObject.NULL);
		} catch (JSONException jsonException) {
			logger.log(String.format(
					"generate_recon_eventinfo:Exception occurred while in JSONObject. Exception : %s,AWS RequestID: %s",
					jsonException.getMessage(), context.getAwsRequestId()));
		}
		return eventinfo;
	}

	/**
	 * Method to generate reconcilation event status data.
	 * 
	 * @param consignment_events
	 *            It will have consignment_events to generate reconcilation
	 *            event information
	 * 
	 */
	public void generate_event_statusdata(LambdaLogger logger, Map<String,String> consignment_events, Context context) {

		aggregator = new RecordAggregator();

		List<JSONObject> eventStatusDataList = new ArrayList<>();
		String timeZone = null;
		try {

			for (Map.Entry<String, String> entry : consignment_events.entrySet()) {

				JSONObject jsonObject = new JSONObject(entry.getKey());
				JSONObject eventStatusData = new JSONObject();
				JSONObject eventInfo = jsonObject.getJSONObject(LambdaConstants.EVENT_INFO);
				String hybrisSourceTimestamp = eventInfo.getString(LambdaConstants.SOURCE_TIMESTAMP);
				ZonedDateTime zDateTime = ZonedDateTime.parse(hybrisSourceTimestamp,
						DateTimeFormatter.ISO_ZONED_DATE_TIME);
				ZoneId zone = zDateTime.getZone();
				timeZone = LambdaConstants.GMT.concat(zone.getId());

				Timestamp time = new Timestamp(System.currentTimeMillis());
				sdf.setTimeZone(TimeZone.getTimeZone(timeZone));
				String sourceTimestamp = sdf.format(time);

				if (sourceTimestamp != null && sourceTimestamp != "") {
					eventStatusData.put(LambdaConstants.PROCESSING_STATUS_UPDATE_TIME, sourceTimestamp);
				} else {
					eventStatusData.put(LambdaConstants.PROCESSING_STATUS_UPDATE_TIME, JSONObject.NULL);
				}

				if (eventInfo.getString(LambdaConstants.EVENT_TYPE_JSON) != null
						&& eventInfo.getString(LambdaConstants.EVENT_TYPE_JSON) != "") {
					eventStatusData.put(LambdaConstants.EVENT_NAME,
							eventInfo.getString(LambdaConstants.EVENT_TYPE_JSON));
				} else {
					eventStatusData.put(LambdaConstants.EVENT_NAME, JSONObject.NULL);
				}
				if (eventInfo.has(LambdaConstants.CONTEXT)) {
					eventStatusData.put(LambdaConstants.PROCESSING_STATUS, LambdaConstants.PROCESSED);
					eventStatusData.put(LambdaConstants.MESSAGE, JSONObject.NULL);
					if (eventInfo.getJSONObject(LambdaConstants.CONTEXT)
							.getString(LambdaConstants.ORIGIN_SOURCE_EVENTID) != null
							&& eventInfo.getJSONObject(LambdaConstants.CONTEXT)
									.getString(LambdaConstants.ORIGIN_SOURCE_EVENTID) != "") {
						eventStatusData.put(LambdaConstants.EVENT_ID, eventInfo.getJSONObject(LambdaConstants.CONTEXT)
								.getString(LambdaConstants.ORIGIN_SOURCE_EVENTID));
					} else {
						eventStatusData.put(LambdaConstants.EVENT_ID, JSONObject.NULL);
					}
					if (eventInfo.getJSONObject(LambdaConstants.CONTEXT)
							.getString(LambdaConstants.ORIGIN_SOURCE_TIMESTAMP) != null
							&& eventInfo.getJSONObject(LambdaConstants.CONTEXT)
									.getString(LambdaConstants.ORIGIN_SOURCE_TIMESTAMP) != "") {
						eventStatusData.put(LambdaConstants.EVENT_GENERATION_TIME,
								eventInfo.getJSONObject(LambdaConstants.CONTEXT)
										.getString(LambdaConstants.ORIGIN_SOURCE_TIMESTAMP));
					} else {
						eventStatusData.put(LambdaConstants.EVENT_GENERATION_TIME, JSONObject.NULL);
					}
				} else {
					eventStatusData.put(LambdaConstants.PROCESSING_STATUS, LambdaConstants.FAILED);
					eventStatusData.put(LambdaConstants.MESSAGE, entry.getValue());
					if (eventInfo.getString(LambdaConstants.SOURCE_EVENT_ID_JSON) != null
							&& eventInfo.getString(LambdaConstants.SOURCE_EVENT_ID_JSON) != "") {
						eventStatusData.put(LambdaConstants.EVENT_ID,
								eventInfo.getString(LambdaConstants.SOURCE_EVENT_ID_JSON));
					} else {
						eventStatusData.put(LambdaConstants.EVENT_ID, JSONObject.NULL);
					}
					if (eventInfo.getString(LambdaConstants.SOURCE_TIMESTAMP) != null
							&& eventInfo.getString(LambdaConstants.SOURCE_TIMESTAMP) != "") {
						eventStatusData.put(LambdaConstants.EVENT_GENERATION_TIME,
								eventInfo.getString(LambdaConstants.SOURCE_TIMESTAMP));
					} else {
						eventStatusData.put(LambdaConstants.EVENT_GENERATION_TIME, JSONObject.NULL);
					}
				}

				if (eventInfo.getString(LambdaConstants.ENTITY_ID) != null
						&& eventInfo.getString(LambdaConstants.ENTITY_ID) != "") {
					eventStatusData.put(LambdaConstants.ENTITY_ID, eventInfo.getString(LambdaConstants.ENTITY_ID));
				} else {
					eventStatusData.put(LambdaConstants.ENTITY_ID, JSONObject.NULL);
				}

				eventStatusData.put(LambdaConstants.CONTEXT, JSONObject.NULL);

				if (eventInfo.getString(LambdaConstants.ISO_COUNTRY_CODE).toLowerCase().trim()
						.equals(LambdaConstants.VNM_COUNTRY_CODE.toLowerCase().trim())) {
					eventStatusDataList.add(eventStatusData);
				} else {
					logger.log(String.format(
							"generate_event_statusdata:Iso country code not correct: %s,AWS RequestID: %s",
							eventInfo.getString("isoCountryCode"), context.getAwsRequestId()));
				}
			}
			if (eventStatusDataList.size() > 0) {
				JSONObject eventReconPayload = new JSONObject();

				eventReconPayload.put(LambdaConstants.EVENT_INFO,
						generate_recon_eventinfo(logger, LambdaConstants.VNM_COUNTRY_CODE, timeZone, context));
				eventReconPayload.put(LambdaConstants.EVENT_STATUS_DATA, eventStatusDataList);
				publish_recon_status(logger, eventReconPayload, context);
			}

		} catch (JSONException jsonException) {
			String errorMessage = String.format(
					"generate_event_statusdata:Exception occurred while in JSONObject. Exception : %s,AWS RequestID: %s",
					jsonException.getMessage(), context.getAwsRequestId());
			logger.log(errorMessage);
		} catch (RuntimeException exception) {
			String errorMessage = String.format(
					"Exception occurred in generate_event_statusdata. Exception : %s,AWS RequestID: %s",
					exception.getMessage(), context.getAwsRequestId());
			logger.log(errorMessage);
		} catch (Exception exception) {
			String errorMessage = String.format(
					"Exception occurred in generate_event_statusdata. Exception : %s,AWS RequestID: %s",
					exception.getMessage(), context.getAwsRequestId());
			logger.log(errorMessage);

		}

	}

	/**
	 * Method to publish reconcilation event for the consignment events.
	 * 
	 * @param eventReconPayload
	 *            eventReconPayload for publish into kinesis stream
	 * @return eventinfo for the particular record
	 * 
	 */
	public void publish_recon_status(LambdaLogger logger, JSONObject eventReconPayload, Context context)
			throws RuntimeException {

		PutRecordResult publish_status = null;
		PutRecordRequest putRecordRequest = new PutRecordRequest();
		try {
			logger.log(String.format("Recon Payload for Recon Kinesis Stream : %s,AWS RequestID: %s ",
					eventReconPayload.toString(), context.getAwsRequestId()));
			ByteBuffer data = ByteBuffer.wrap((eventReconPayload.toString() + "\n").getBytes(LambdaConstants.UTF_8));

			putRecordRequest.setStreamName(reconKinesisStream);
			putRecordRequest.setData(data);
			putRecordRequest.setPartitionKey(String.format(eventReconPayload.getJSONObject(LambdaConstants.EVENT_INFO)
					.getString(LambdaConstants.SOURCE_EVENT_ID_JSON)));

			publish_status = kinesisForwarder.putRecord(putRecordRequest);
			if (publish_status == null || publish_status.getShardId() == null) {

				throw new RuntimeException(String.format(
						"publish_recon_status:Could not publish the records into Recon kinesis stream,AWS RequestID: %s",
						context.getAwsRequestId()));
			} else {
				logger.log(
						String.format("publish_recon_status:Successfully published record : shard %s,AWS RequestID: %s",
								publish_status.getShardId(), context.getAwsRequestId()));
			}
		} catch (Exception e) {
			String errorMessage = String.format(
					"publish_recon_status:Exception occurred while publishing to recon kinesis stream records .Error: %s, AWS RequestID: %s",
					e.getMessage(), context.getAwsRequestId());
			logger.log(errorMessage);
		}
	}

}
