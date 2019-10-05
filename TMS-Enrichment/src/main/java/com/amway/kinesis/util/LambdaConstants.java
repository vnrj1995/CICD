/*
 * Put your copyright text here
 */
 package com.amway.kinesis.util;

/**
 * The Class LambdaConstants. All the constants being used in Lambda function
 * are declared in this class.
 * 
 * @author rajeshdhiman
 */
public class LambdaConstants {

	/** The Constant S3_BUCKET_NAME. */
	public static final String S3_BUCKET_NAME = "S3_BUCKET_NAME";

	/** The Constant ERROR_PAYLOAD_PREFIX. */
	public static final String ERROR_PAYLOAD_PREFIX = "ERROR_PAYLOAD_PREFIX";

	/** The Constant _UNDERSCORE. */
	public static final String _UNDERSCORE = "_";

	/** The Constant JSON_EXTENSION. */
	public static final String JSON_EXTENSION = ".json";

	/** The Constant ERROR. */
	public static final String ERROR = "ERROR";

	/** The Constant SNS_ARN. */
	public static final String SNS_ARN = "SNS_ARN";

	/** The Constant SNS_ARN. */
	public static final String API_HOST = "API_HOST";
	
	public static final String COGNITO_HOST = "COGNITO_HOST";

	/** The Constant DESTINATION_STREAM_NAME. */
	public static final String DESTINATION_STREAM_NAME = "DESTINATION_STREAM_NAME";

	/** The Constant DESTINATION_STREAM_NAME. */
	public static final String DESTINATION_STREAM_REGION = "DESTINATION_STREAM_REGION";

	/** The Constant DESTINATION_CONNECTION_TIMEOUT. */
	public static final String DESTINATION_CONNECTION_TIMEOUT = "DESTINATION_CONNECTION_TIMEOUT";

	/** The Constant DESTINATION_SOCKET_TIMEOUT. */
	public static final String DESTINATION_SOCKET_TIMEOUT = "DESTINATION_SOCKET_TIMEOUT";

	/** The Constant MAX_CONNECTIONS. */
	public static final String MAX_CONNECTIONS = "MAX_CONNECTIONS";
	
	/** The Constant TIMEZONE. */
	public static final String TIMEZONE = "TIMEZONE";

	/** The Constant ERROR_LAMBDA_FUNCTION. */
	public static final String ERROR_NOTIFICATION_SUBJECT = "ERROR|Lambda function JSON parsing|%s";

	/** The Constant ERROR_NO_RECORD_FOUND_NOTIFICATION_SUBJECT. */
	public static final String ERROR_NO_RECORD_FOUND_NOTIFICATION_SUBJECT = "ERROR|Response from API received as No Record Found|%s";

	/** The Constant ERROR_CARRIERCODE_API_NOTIFICATION_SUBJECT. */
	public static final String ERROR_CARRIERCODE_API_NOTIFICATION_SUBJECT = "ERROR|Response from Carrier Code API received as Error|%s";

	/** The Constant ERROR_SERVICELEVEL_NULL_SUBJECT. */
	public static final String ERROR_SERVICELEVEL_NULL_SUBJECT = "ERROR|Servicelevel element received NULL in Payload|%s";

	/** The Constant ERROR_HTTP_RESPONSE_EXCEPTION_NOTIFICATION_SUBJECT. */
	public static final String ERROR_HTTP_RESPONSE_EXCEPTION_NOTIFICATION_SUBJECT = "ERROR|HTTP Response from API received as Exception|%s";

	/** The Constant ERROR_POPULATING_CARRIERCODE_NOTIFICATION_SUBJECT. */
	public static final String ERROR_POPULATING_CARRIERCODE_NOTIFICATION_SUBJECT = "ERROR|An Error occurred while populating carrier code|%s";

	/**
	 * The Constant ERROR_PUSHING_RECORDS_KINESISSTREAM_NOTIFICATION_SUBJECT.
	 */
	public static final String ERROR_PUSHING_RECORDS_KINESISSTREAM_NOTIFICATION_SUBJECT = "ERROR|An Error occurred while put records to Kinesis|%s";

	/** The Constant ERROR_PROCESSING_RECORDS_NOTIFICATION_SUBJECT. */
	public static final String ERROR_PROCESSING_RECORDS_NOTIFICATION_SUBJECT = "ERROR|An Error occured while processing records|%s";

	/** The Constant ERROR_MSG_INIT_JSON. */
	public static final String ERROR_MSG_INIT_JSON = "An Error occurred while initializing JSONObject. Seems JSON is not in proper format.";

	public static final String UTF_8 = "UTF-8";

	/** The Constant FORWARD_SLASH. */
	public static final String FORWARD_SLASH = "/";

	/** The Constant SERVICE_LEVEL API Request Parameter. */
	public static final String SERVICE_LEVEL = "serviceLevel";

	/** The Constant COUNTRY_REQ API Request Parameter. */
	public static final String COUNTRY_REQ = "country";

	/** The Constant STATE_REQ API Request Parameter. */
	public static final String STATE_REQ = "state";

	/** The Constant CITY API Request Parameter. */
	public static final String CITY = "city";

	/** The Constant SUBURB API Request Parameter. */
	public static final String SUBURB = "suburb";

	/** The Constant POST_CODE API Request Parameter. */
	public static final String POST_CODE = "postCode";

	/** The Constant CONSIGNMENT_ID */
	public static final String CONSIGNMENT_ID = "consignmentId";

	/** The Constant MESSAGE */
	public static final String MESSAGE = "message";

	/** The Constant DELIVERY_ADDRESS Json element. */
	public static final String DELIVERY_ADDRESS = "deliveryAddress";

	/** The Constant ENTITY_ID Json element. */
	public static final String ENTITY_ID = "entityId";

	/** The Constant EVENT_INFO Json element. */
	public static final String EVENT_INFO = "eventInfo";

	/** The Constant ENTITY Json element. */
	public static final String ENTITY = "entity";

	/** The Constant CARRIER Json element. */
	public static final String CARRIER = "carrier";

	/** The Constant CARRIER Json element. */
	public static final String DELIVERY_CARRIER_CODE = "deliveryCarrierCode";


	/** The Constant EVENT_TYPE */
	public static final String DATE_FORMAT = "yyyy-MM-dd'T'HH:mm:ss.SSSXXX";

	/** The Constant EVENT_TYPE */
	public static final String EVENT_TYPE = "ORDER_READY_FOR_FULFILLMENT_TMS_ENRICHED";

	/** The Constant ENTITY_TYPE */
	public static final String ENTITY_TYPE = "consignmentEnriched";

	/** The Constant SOURCE_APPLICATION */
    public static final String SOURCE_APPLICATION = "tmsEnrich";

	/** The Constant SOURCE_HOST */
    public static final String SOURCE_HOST = "tmsEnrich";

	/** The Constant SOURCE_TIMESTAMP */
	public static final String SOURCE_TIMESTAMP = "sourceTimestamp";

	/** The Constant EVENT_TYPE_JSON */
	public static final String EVENT_TYPE_JSON = "eventType";

	/** The Constant SOURCE_APPLICATION_JSON */
	public static final String SOURCE_APPLICATION_JSON = "sourceApplication";

	/** The Constant SOURCE_HOST_JSON */
	public static final String SOURCE_HOST_JSON = "sourceHost";

	/** The Constant SOURCE_EVENT_ID_JSON */
	public static final String SOURCE_EVENT_ID_JSON = "sourceEventId";

	/** The Constant ENTITY_TYPE_JSON */
	public static final String ENTITY_TYPE_JSON = "entityType";
	
	/** The Constant ORIGIN_SOURCE_APPLICATION */
	public static final String ORIGIN_SOURCE_APPLICATION = "originSourceApplication";
	
	/** The Constant ORIGIN_SOURCE_TIMESTAMP */
	public static final String ORIGIN_SOURCE_TIMESTAMP = "originSourceTimestamp";
	
	/** The Constant ORIGIN_SOURCE_EVENTID */
	public static final String ORIGIN_SOURCE_EVENTID = "originSourceEventId";
	
	/** The Constant ORIGIN_ENTITY_TYPE */
	public static final String ORIGIN_ENTITY_TYPE = "originEntityType";
	
	/** The Constant CONTEXT */
	public static final String CONTEXT = "context";

	/** The Constant STATE Json element. */
	public static final String STATE = "state";

	/** The Constant REQUESTED_DELIVERY_SERVICE_LEVEL Json element. */
	public static final String REQUESTED_DELIVERY_SERVICE_LEVEL = "requestedDeliveryServiceLevel";

	/** The Constant ISO_COUNTRY_CODE Json element. */
	public static final String ISO_COUNTRY_CODE = "isoCountryCode";

	/** The Constant CITY_NAME Json element. */
	public static final String CITY_NAME = "cityName";

	/** The Constant POSTAL_CODE Json element. */
	public static final String POSTAL_CODE = "postalCode";

	/** The Constant LINE_3. */
	public static final String LINE_3 = "line3";

	/** The Constant APPLICATION_JSON. */
	public static final String APPLICATION_JSON = "application/json";
	
	/** The Constant APPLICATION_JSON. */
	public static final String TEXT_PLAIN = "text/plain";

	/** The Constant UTF8. */
	public static final String UTF8 = "UTF8";

	/** Post Request Success Response Code */
	public static final int RESPONSE_CODE = 200;

	/** The Constant AWS_LAMBDA_FUNCTION_NAME. */
	public static final String AWS_LAMBDA_FUNCTION_NAME = "AWS_LAMBDA_FUNCTION_NAME";
	
	/** The Constant ORDER_RECON */
	public static final String CONSIGNMENT_RECON = "ConsignmentRecon";

	/** The Constant EVENT_RECONCILIATION */
	public static final String EVENT_RECONCILIATION = "EventReconciliation";
	
	/** The Constant PROCESSED */
	public static final String PROCESSED = "Processed";
	
	/** The Constant PROCESSED */
	public static final String FAILED = "Failed";
	
	/** The Constant AU_COUNTRY_CODE */
	public static final String VNM_COUNTRY_CODE = "VNM";
	
	/** The Constant AFFILIATE_CODE */
	public static final String AFFILIATE_CODE ="affiliateCode";
	
	/** The Constant RECON_KINESIS_STREAM */
	public static final String RECON_KINESIS_STREAM = "RECON_KINESIS_STREAM";
	
	/** The Constant EVENT_GENERATION_TIME */
	public static final String EVENT_GENERATION_TIME = "eventGenerationTime";
	
	/** The Constant EVENT_ID */
	public static final String EVENT_ID = "eventId";
	
	/** The Constant EVENT_NAME */
	public static final String EVENT_NAME = "eventName";
	
	/** The Constant PROCESSING_STATUS */
	public static final String PROCESSING_STATUS = "processingStatus";
	
	/** The Constant PROCESSING_STATUS_UPDATE_TIME */
	public static final String PROCESSING_STATUS_UPDATE_TIME = "processingStatusUpdateTime";
	
	/** The Constant EVENT_STATUS_DATA */
	public static final String EVENT_STATUS_DATA = "eventStatusData";
	
	/** The Constant EVENT_STATUS_DATA */
	public static final String GENERATE_RECON_DATA = "GENERATE_RECON_DATA";
	
	/** The Constant TRUE */
	public static final String TRUE = "True";
	
	/** The Constant GMT */
	public static final String GMT = "GMT";
	
	/** The Constant VNM_AFFILIATE_CODE */
	public static final String VNM_AFFILIATE_CODE = "VNM_AFFILIATE_CODE";
	
	/** The Constant GRANT_TYPE */
	public static final String GRANT_TYPE = "grant_type";
	
	/** The Constant CLIENT_ID */
	public static final String CLIENT_ID = "client_id";
	
	/** The Constant CLIENT_SECRET */
	public static final String CLIENT_SECRET = "client_secret";
	
	/** The Constant SCOPE */
	public static final String SCOPE = "scope";
	
	/** The Constant CLIENT_CREDENTIALS */
	public static final String CLIENT_CREDENTIALS = "client_credentials";
	
	/** The Constant CLIENT_ID_VAR */
	public static final String CLIENT_ID_VAR = "CLIENT_ID_VAR";
	
	/** The Constant CLIENT_SECRET_VAR */
	public static final String CLIENT_SECRET_VAR = "CLIENT_SECRET_VAR";
	
	/** The Constant CLIENT_SCOPE */
	public static final String CLIENT_SCOPE = "CLIENT_SCOPE";
	
	/** The Constant ACCESS_TOKEN */
	public static final String ACCESS_TOKEN = "access_token";
	
	/** The Constant TOKEN_TYPE */
	public static final String TOKEN_TYPE = "token_type";


}
