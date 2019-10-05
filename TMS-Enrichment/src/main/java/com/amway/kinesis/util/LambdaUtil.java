/*
 * Put your copyright text here
 */
 package com.amway.kinesis.util;

import com.amazonaws.services.lambda.AWSLambda;
import com.amazonaws.services.lambda.AWSLambdaClientBuilder;
import com.amazonaws.services.lambda.model.EventSourceMappingConfiguration;
import com.amazonaws.services.lambda.model.UpdateEventSourceMappingRequest;
import com.amazonaws.services.lambda.runtime.LambdaLogger;

/**
 * The Class LambdaUtil.
 */
public class LambdaUtil {

	/**
	 * Disable lambda function.
	 *
	 * @param logger
	 *            the logger
	 * @param consumerName
	 *            the consumer name
	 */
	public static void disableLambdaFunction(LambdaLogger logger) {
		try {
            logger.log("@disableLambdaFunction");
			AWSLambda client = AWSLambdaClientBuilder.standard().build();
			String UUID = "";
			String lambdaFuncName = System.getenv("AWS_LAMBDA_FUNCTION_NAME");
			for (EventSourceMappingConfiguration config : client.listEventSourceMappings().getEventSourceMappings()) {
				if (config.getFunctionArn().endsWith(lambdaFuncName)) {
					UUID = config.getUUID();
					break;
				}
			}

			logger.log(String.format("UUID for %s : %s ", lambdaFuncName, UUID));
			UpdateEventSourceMappingRequest request = new UpdateEventSourceMappingRequest().withUUID(UUID)
					.withFunctionName(lambdaFuncName).withEnabled(false);
			client.updateEventSourceMapping(request);
            logger.log(String.format("Lambda function has been disabled."));
		} catch (Exception exception) {
			logger.log(String.format("Exception occured while disabling Lambda function. Error: %s",
					exception.getMessage()));
		}
	}
}
