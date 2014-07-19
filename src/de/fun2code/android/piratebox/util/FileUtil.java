package de.fun2code.android.piratebox.util;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.fileupload.MultipartStream;

import sunlabs.brazil.server.Request;
import android.os.Environment;
import android.util.Log;
import de.fun2code.android.piratebox.Constants;

public class FileUtil {
	/**
	 * Waits for external storage to be writable
	 * 
	 * @param tries
	 * 				number of retries
	 * @param pauseBeforRetry
	 * 				time to wait (milis) berfore next try
	 * @return
	 * 				{@code true} on success, otherwise {@code false}
	 */
	public static boolean waitExternalStorageWritable(int tries, int pauseBeforRetry) {
	    boolean externalStorageWriteable = false;
	    int count = 0;

	    do {
	        String state = Environment.getExternalStorageState();
	        if(count > 0) {
	            try {
	                Thread.sleep(pauseBeforRetry);
	            } catch (InterruptedException e) {
	                Log.e(Constants.TAG, e.getMessage(), e);
	            }
	        }
	        if (state.equals(Environment.MEDIA_MOUNTED)) {
	            externalStorageWriteable = true;
	        } 
	        count++;
	    } while (!externalStorageWriteable && (count < tries));
	    

	    return externalStorageWriteable;
	}
	
	/**
	 * Processes a multipart upload request and stores the files
	 * into the specified output directory
	 * 
	 * @param outDir	output directory
	 * @param request	the request object
	 * @return			list of saved file names (fqn)
	 */
	public static List<String> processUpload(File outDir, Request request) {
		List<String> files = new ArrayList<String>();

		String type = (String) request.headers.get("content-type");

		try {
			// Create output directory if it does not exist
			if (!outDir.exists()) {
				outDir.mkdirs();
			}

			// Get the boundary string
			int boundaryIndex = type.indexOf("boundary=");
			byte[] boundary = (type.substring(boundaryIndex + 9)).getBytes();

			// Construct a MultiPartStream with request.in as InputStream
			MultipartStream multipartStream = new MultipartStream(request.in,
					boundary);
			boolean nextPart = multipartStream.skipPreamble();

			// Loop through all parts
			while (nextPart) {
				String headers = multipartStream.readHeaders();

				// If part is a file, save it to disk. Otherwise skip it.
				if (headers.contains("filename=\"")) {
					// Get filename
					String filename = headers.substring(headers
							.indexOf("filename=") + 10);
					filename = filename.substring(0, filename.indexOf("\""));

					// Turn file extension to lower case
					if (filename.lastIndexOf(".") > 0) {
						String ext = filename.substring(filename
								.lastIndexOf(".") + 1);
						filename = filename.substring(0,
								filename.lastIndexOf(".") + 1)
								+ ext.toLowerCase();
					}

					// If filename is not empty save content to file system
					if (filename.length() > 0) {
						/*
						 * Make sure file is not already there. If so, add
						 * number (e.g. foo-3.png)
						 */
						int fnameCount = 1;
						String ext = filename.substring(filename
								.lastIndexOf(".") + 1);
						String baseName = filename.substring(0,
								filename.lastIndexOf("."));

						while (new File(outDir + "/" + filename).exists()) {
							filename = baseName + "-" + fnameCount + "." + ext;
							fnameCount++;
						}

						files.add(filename);
						multipartStream.readBodyData(new FileOutputStream(
								outDir + "/" + filename));
					} else {
						multipartStream.discardBodyData();
					}
				} else {
					multipartStream.discardBodyData();
				}

				nextPart = multipartStream.readBoundary();
			}

		} catch (Exception e) {
			Log.e(Constants.TAG, "Error during file upload: " + e.toString());
		}

		return files;
	}
}
