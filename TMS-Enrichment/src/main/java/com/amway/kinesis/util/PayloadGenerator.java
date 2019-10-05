package com.amway.kinesis.util;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;

public class PayloadGenerator {

	/**
	 * Creates the payload file.
	 * @param fileName the file name
	 * @param payload the payload
	 * @param fileExtension the file extension
	 * @return the file
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	public static File createPayloadFile(String fileName, String payload, String fileExtension) throws IOException {

		File file = File.createTempFile(fileName, fileExtension);
		file.deleteOnExit();

		Writer writer = new OutputStreamWriter(new FileOutputStream(file));
		writer.write(payload);
		writer.close();

		return file;
	}
}
