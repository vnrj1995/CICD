package com.amway.api.consumer;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;

import org.junit.Before;
import org.junit.Test;

import com.amazonaws.services.kms.AWSKMS;
import com.amazonaws.services.kms.AWSKMSClientBuilder;
import com.amazonaws.services.kms.model.DecryptRequest;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.events.KinesisEvent;
import com.amazonaws.util.Base64;
import com.amway.kinesis.consumer.KinesisLambdaReceiver;
import com.amway.api.consumer.TestContext;
import com.amway.api.consumer.TestUtils;
import com.amway.kinesis.util.LambdaUtil;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;

/**
 * A simple test harness for locally invoking your Lambda function handler.
 */
public class OrderHandlerTest {

    private KinesisEvent input;
    private static final String CHARSET_UTF_8 = "UTF-8";
    byte[] encryptedKey = Base64.decode("AQICAHgKOE5e4BtZCwlvUljEIxAjKFMJQa+mhsw5BfxLqENXdwFzfIqmUX+rS7BWWlvkSA3OAAAAdzB1BgkqhkiG9w0BBwagaDBmAgEAMGEGCSqGSIb3DQEHATAeBglghkgBZQMEAS4wEQQMegtB6wqcGk8v59vDAgEQgDSNnuiUNbxq0o5e35b+yjLSDqi0UnnAm5TjGq7UQbAU6qAIFHY1fKwgl8aRVQ0bfx1+ulG5");
    AWSKMS client = AWSKMSClientBuilder.defaultClient();

	DecryptRequest request = new DecryptRequest().withCiphertextBlob(ByteBuffer.wrap(encryptedKey));

	ByteBuffer plainTextKey = client.decrypt(request).getPlaintext();
	String temp = new String(plainTextKey.array(), Charset.forName(CHARSET_UTF_8));
    
    
    KinesisLambdaReceiver handler = new KinesisLambdaReceiver();

    Context ctx = createContext();

    static String readFile(String path, Charset encoding) throws IOException {
        byte[] encoded = Files.readAllBytes(Paths.get(path));
        return new String(encoded, encoding);
    }

    @Before
    public void createInput() throws IOException {
        input = TestUtils.parse("/kinesis-event.json", KinesisEvent.class);
        //jsonData = readFile("E:\\Amway\\Kinesis\\json_order_payload.json", StandardCharsets.UTF_8);
    }

    private Context createContext() {
        TestContext ctx = new TestContext();

        // customize your context here if needed.
        ctx.setFunctionName("ApacIdnDevHybrisOrderConsumer");

        return ctx;
    }

    /*
     * @Test public void testOrderHandler() { try { PropertyDecriptor.getDecryptedKey(LambdaConstants.SNS_ARN);
     * SNSUtil.pushNotificationByOrderNumber("1", ctx.getLogger(), "Test Message",
     * "Testing SNS Integration with Lambda function"); } catch (Exception e) { e.printStackTrace(); } //
     * ctx.getLogger().log(handler.uploadErrorPayloadToS3(ctx.getLogger(), "123", jsonData, //
     * "apac-idn-dev-hybris-order/orderpayloads/", "apac-idn-dev-hybris-order")); // handler.handleRequest(input, ctx);
     * }
     */

    // @Test
    /*
     * public void testSNSNotification() { try { PropertyDecriptor.getDecryptedKey(LambdaConstants.SNS_ARN);
     * SNSUtil.pushNotificationByOrderNumber("1", ctx.getLogger(), "Test Message",
     * "Testing SNS Integration with Lambda function"); } catch (Exception e) { e.printStackTrace(); } }
     */

    @Test
    public void testOrderHandler() {

        try {
            //LambdaUtil.disableLambdaFunction(ctx.getLogger());
            handler.handleRequest(input, ctx);
        } catch (Exception e) {
            e.printStackTrace();
        }
        // ctx.getLogger().log(handler.uploadErrorPayloadToS3(ctx.getLogger(), "123", jsonData,
        // "apac-idn-dev-hybris-order/orderpayloads/", "apac-idn-dev-hybris-order"));
        // handler.handleRequest(input, ctx);

    }
}
