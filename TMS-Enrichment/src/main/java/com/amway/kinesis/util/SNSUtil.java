package com.amway.kinesis.util;

import com.amazonaws.services.lambda.runtime.LambdaLogger;
import com.amazonaws.services.sns.AmazonSNS;
import com.amazonaws.services.sns.AmazonSNSClient;
import com.amazonaws.services.sns.model.PublishRequest;
import com.amazonaws.services.sns.model.PublishResult;

/**
 * The Class SNSUtil.
 * 
 * @author rajeshdhiman
 */
public class SNSUtil {

	/** The Constant ERR_MSG_LAMBDA_FUNC_DISABLED. */
	private static final String ERR_MSG_LAMBDA_FUNC_DISABLED = "Unable to connect with database. Lambda function has been disabled.";

	/** The sns ARN. */
	private static final String snsTopicARN = System.getenv(LambdaConstants.SNS_ARN);

	/**
	 * Push notification by order number.
	 *
	 * @param snsTopicARN
	 *            the sns topic ARN
	 * @param orderNumber
	 *            the order number
	 * @param logger
	 *            the logger
	 * @param message
	 *            the message
	 * @param subject
	 *            the subject
	 */
	public static void pushNotificationByConsignmentID(String consignmentId, String entityID, LambdaLogger logger,
			String message, String subject) {

		/*
		 * Creating SNS Client to publish.
		 */
		AmazonSNS snsClient = createSNSClient();

		/*
		 * Creating PublishRequest with ARN, message, subject attributes.
		 */
		PublishRequest request = new PublishRequest(snsTopicARN, message, subject);

		PublishResult result = snsClient.publish(request);
		logger.log(String.format("SNS Notification sent for ConsignmentID : %s Entity Id : %s with messageId : %s",
				consignmentId, entityID, result.getMessageId()));
	}

	public static void pushNotificationIncorrectJSON(LambdaLogger logger, String message, String subject) {

		/*
		 * Creating SNS Client to publish.
		 */
		AmazonSNS snsClient = createSNSClient();

		/*
		 * Creating PublishRequest with ARN, message, subject attributes.
		 */
		PublishRequest request = new PublishRequest(snsTopicARN, message, subject);

		PublishResult result = snsClient.publish(request);
		logger.log(
				String.format("SNS Notification sent for Incorrect JSON with messageId : %s", result.getMessageId()));
	}

	public static void pushNotificationExceptionOccurred(LambdaLogger logger, String message, String subject) {

		/*
		 * Creating SNS Client to publish.
		 */
		AmazonSNS snsClient = createSNSClient();

		/*
		 * Creating PublishRequest with ARN, message, subject attributes.
		 */
		PublishRequest request = new PublishRequest(snsTopicARN, message, subject);

		PublishResult result = snsClient.publish(request);
		logger.log(String.format("SNS Notification sent for Error occured with messageId : %s", result.getMessageId()));
	}

	/**
	 * Creates the SNS client.
	 *
	 * @return the amazon SNS client
	 * @throws IllegalArgumentException
	 *             the illegal argument exception
	 */
	@SuppressWarnings("deprecation")
	private static AmazonSNS createSNSClient() throws IllegalArgumentException {
		return AmazonSNSClient.builder().build();
	}

	/**
	 * Push notification JSON payload not found.
	 *
	 * @param snsTopicARN
	 *            the sns topic ARN
	 * @param logger
	 *            the logger
	 */
	public static void pushNotificationJSONPayloadNotFound(LambdaLogger logger) {

		AmazonSNS snsClient = createSNSClient();

		/*
		 * Creating PublishRequest with ARN, message, subject attributes.
		 */
		PublishRequest request = new PublishRequest(snsTopicARN, "JSON payload could not retrieved from Kinesis event.",
				String.format("ERROR | JSON Payload not found | %s", System.getenv("AWS_LAMBDA_FUNCTION_NAME")));

		PublishResult result = snsClient.publish(request);
		logger.log(String.format("SNS Notification sent for jsonPayload un-availablity with messageId : %s",
				result.getMessageId()));
	}

	/**
	 * Push notification S3 upload.
	 *
	 * @param snsTopicARN
	 *            the sns topic ARN
	 * @param logger
	 *            the logger
	 */
	public static void pushNotificationS3Upload(LambdaLogger logger, String message) {

		AmazonSNS snsClient = createSNSClient();

		/*
		 * Creating PublishRequest with ARN, message, subject attributes.
		 */
		PublishRequest request = new PublishRequest(snsTopicARN, message,
				String.format("ERROR | S3 Upload | %s", System.getenv("AWS_LAMBDA_FUNCTION_NAME")));

		PublishResult result = snsClient.publish(request);
		logger.log(String.format("SNS Notification sent for S3 bucket connectivity issue with messageId : %s",
				result.getMessageId()));
	}

}
