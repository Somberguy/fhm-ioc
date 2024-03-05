package org.fhm.substrate.util;

import java.io.*;

/**
 * @since 2023/10/14 16:27
 * @author Somberguy
 */
public class IOUtil {

    private static final int EOF = -1;
    private static final byte[] buffer = new byte[512];

    /**
     * The file to byte array
     *
     * @param filePath file path
     * @return byte array
     */
    public static byte[] file2Bytes(String filePath) {
        File file = new File(filePath);
        if (!file.exists()) {
            throw IOCExceptionUtil.generateNormalException(filePath + " is not exist");
        }
        try (
                FileInputStream fis = new FileInputStream(file);
                BufferedInputStream bos = new BufferedInputStream(fis);
                ByteArrayOutputStream baos = new ByteArrayOutputStream()
        ) {
            int in;
            while (EOF != (in = bos.read(buffer))) {
                baos.write(buffer, 0, in);
            }
            return baos.toByteArray();
        } catch (IOException e) {
            throw IOCExceptionUtil.generateNormalException(e.getMessage(), e);
        }
    }

    /**
     * @param is InputStream
     * @return InputStream to byte array
     */
    public static byte[] inStreamToByte(InputStream is) {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        byte[] bytes = new byte[1024];
        int i;
        try {
            while ((i = is.read(bytes)) != EOF) {
                bos.write(bytes, 0, i);
            }
        } catch (Exception e) {
            throw IOCExceptionUtil.generateNormalException(e);
        }
        return bos.toByteArray();
    }

}
