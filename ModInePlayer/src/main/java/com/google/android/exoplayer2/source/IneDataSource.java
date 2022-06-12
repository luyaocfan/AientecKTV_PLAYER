package com.google.android.exoplayer2.source;

import android.net.Uri;

import androidx.annotation.Nullable;
import androidx.annotation.VisibleForTesting;

import com.google.android.exoplayer2.C;
import com.google.android.exoplayer2.upstream.BaseDataSource;
import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.upstream.DataSourceException;
import com.google.android.exoplayer2.upstream.DataSpec;
import com.google.android.exoplayer2.upstream.HttpDataSource;
import com.google.android.exoplayer2.upstream.HttpUtil;
import com.google.android.exoplayer2.upstream.TransferListener;
import com.google.android.exoplayer2.util.Log;
import com.google.android.exoplayer2.util.Util;
import com.google.common.base.Ascii;
import com.google.common.base.Predicate;
import com.google.common.net.HttpHeaders;

import java.io.IOException;
import java.io.InputStream;
import java.io.InterruptedIOException;
import java.io.OutputStream;
import java.lang.reflect.Method;
import java.net.HttpURLConnection;
import java.net.NoRouteToHostException;
import java.net.ProtocolException;
import java.net.URL;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.GZIPInputStream;

import static com.google.android.exoplayer2.upstream.HttpUtil.buildRangeRequestHeader;
import static com.google.android.exoplayer2.util.Assertions.checkNotNull;
import static com.google.android.exoplayer2.util.Util.castNonNull;
import static java.lang.Math.min;

public class IneDataSource extends BaseDataSource implements HttpDataSource {

    public static final class Factory implements HttpDataSource.Factory {

        private final RequestProperties defaultRequestProperties;

        @Nullable private TransferListener transferListener;
        @Nullable private Predicate<String> contentTypePredicate;
        @Nullable private String userAgent;
        private int connectTimeoutMs;
        private int readTimeoutMs;
        private boolean allowCrossProtocolRedirects;

        /** Creates an instance. */
        public Factory() {
            defaultRequestProperties = new RequestProperties();
            connectTimeoutMs = DEFAULT_CONNECT_TIMEOUT_MILLIS;
            readTimeoutMs = DEFAULT_READ_TIMEOUT_MILLIS;
        }

        /** @deprecated Use {@link #setDefaultRequestProperties(Map)} instead. */
        @Deprecated
        @Override
        public final RequestProperties getDefaultRequestProperties() {
            return defaultRequestProperties;
        }

        @Override
        public final Factory setDefaultRequestProperties(Map<String, String> defaultRequestProperties) {
            this.defaultRequestProperties.clearAndSet(defaultRequestProperties);
            return this;
        }

        /**
         * Sets the user agent that will be used.
         *
         * <p>The default is {@code null}, which causes the default user agent of the underlying
         * platform to be used.
         *
         * @param userAgent The user agent that will be used, or {@code null} to use the default user
         *     agent of the underlying platform.
         * @return This factory.
         */
        public Factory setUserAgent(@Nullable String userAgent) {
            this.userAgent = userAgent;
            return this;
        }

        /**
         * Sets the connect timeout, in milliseconds.
         *
         * <p>The default is {@link IneDataSource#DEFAULT_CONNECT_TIMEOUT_MILLIS}.
         *
         * @param connectTimeoutMs The connect timeout, in milliseconds, that will be used.
         * @return This factory.
         */
        public Factory setConnectTimeoutMs(int connectTimeoutMs) {
            this.connectTimeoutMs = connectTimeoutMs;
            return this;
        }

        /**
         * Sets the read timeout, in milliseconds.
         *
         * <p>The default is {@link IneDataSource#DEFAULT_READ_TIMEOUT_MILLIS}.
         *
         * @param readTimeoutMs The connect timeout, in milliseconds, that will be used.
         * @return This factory.
         */
        public Factory setReadTimeoutMs(int readTimeoutMs) {
            this.readTimeoutMs = readTimeoutMs;
            return this;
        }

        /**
         * Sets whether to allow cross protocol redirects.
         *
         * <p>The default is {@code false}.
         *
         * @param allowCrossProtocolRedirects Whether to allow cross protocol redirects.
         * @return This factory.
         */
        public Factory setAllowCrossProtocolRedirects(boolean allowCrossProtocolRedirects) {
            this.allowCrossProtocolRedirects = allowCrossProtocolRedirects;
            return this;
        }

        /**
         * Sets a content type {@link Predicate}. If a content type is rejected by the predicate then a
         * {@link InvalidContentTypeException} is thrown from {@link
         * IneDataSource#open(DataSpec)}.
         *
         * <p>The default is {@code null}.
         *
         * @param contentTypePredicate The content type {@link Predicate}, or {@code null} to clear a
         *     predicate that was previously set.
         * @return This factory.
         */
        public Factory setContentTypePredicate(@Nullable Predicate<String> contentTypePredicate) {
            this.contentTypePredicate = contentTypePredicate;
            return this;
        }

        /**
         * Sets the {@link TransferListener} that will be used.
         *
         * <p>The default is {@code null}.
         *
         * <p>See {@link DataSource#addTransferListener(TransferListener)}.
         *
         * @param transferListener The listener that will be used.
         * @return This factory.
         */
        public Factory setTransferListener(@Nullable TransferListener transferListener) {
            this.transferListener = transferListener;
            return this;
        }

        @Override
        public IneDataSource createDataSource() {
            IneDataSource dataSource =
                    new IneDataSource(
                            userAgent,
                            connectTimeoutMs,
                            readTimeoutMs,
                            allowCrossProtocolRedirects,
                            defaultRequestProperties,
                            contentTypePredicate);
            if (transferListener != null) {
                dataSource.addTransferListener(transferListener);
            }
            return dataSource;
        }
    }

    /** The default connection timeout, in milliseconds. */
    public static final int DEFAULT_CONNECT_TIMEOUT_MILLIS = 8 * 1000;
    /**
     * The default read timeout, in milliseconds.
     */
    public static final int DEFAULT_READ_TIMEOUT_MILLIS = 8 * 1000;

    private static final String TAG = "IneDataSource";
    private static final int MAX_REDIRECTS = 20; // Same limit as okhttp.
    private static final int HTTP_STATUS_TEMPORARY_REDIRECT = 307;
    private static final int HTTP_STATUS_PERMANENT_REDIRECT = 308;
    private static final long MAX_BYTES_TO_DRAIN = 2048;

    private final boolean allowCrossProtocolRedirects;
    private final int connectTimeoutMillis;
    private final int readTimeoutMillis;
    @Nullable private final String userAgent;
    @Nullable private final RequestProperties defaultRequestProperties;
    private final RequestProperties requestProperties;

    @Nullable private Predicate<String> contentTypePredicate;
    @Nullable private DataSpec dataSpec;
    @Nullable private HttpURLConnection connection;
    @Nullable private HttpURLConnection cacheConnection;
    @Nullable private InputStream inputStream;
    private boolean opened;
    private int responseCode, cacheResponseCode;
    private HashMap<String, List<String>> cacheResponseHeaders = null;

    private long bytesToRead;
    private long bytesRead;
    private long cacheBytesToRead;
    private int cacheBPS;
    public static final int CacheStatus_NoCache = 0;
    public static final int CacheStatus_Caching = 1;
    public static final int CacheStatus_Cached = 2;
    public static final int CacheStatus_CacheError = 3;
    public int CacheStatus = CacheStatus_NoCache;
    public static final int ChunkSize = 1024*64;
    private boolean bExitCacheLoop = false;
    private byte[] buffered = null;
    private int preBufferFillBytes = 0;
    private int bufferFillBytes = 0;

    public static String[] VODServerBaseUrl = new String[] {""};
    public static int VODServerIndex = 0;
    public static final String VODReplaceSign = "http://vodserver.ine.com/";
    public Uri uri;
    /** @deprecated Use {@link Factory} instead. */
    @SuppressWarnings("deprecation")
    @Deprecated
    public IneDataSource() {
        this(/* userAgent= */ null, DEFAULT_CONNECT_TIMEOUT_MILLIS, DEFAULT_READ_TIMEOUT_MILLIS);
    }

    /** @deprecated Use {@link Factory} instead. */
    @SuppressWarnings("deprecation")
    @Deprecated
    public IneDataSource(@Nullable String userAgent) {
        this(userAgent, DEFAULT_CONNECT_TIMEOUT_MILLIS, DEFAULT_READ_TIMEOUT_MILLIS);
    }

    /** @deprecated Use {@link Factory} instead. */
    @SuppressWarnings("deprecation")
    @Deprecated
    public IneDataSource(
            @Nullable String userAgent, int connectTimeoutMillis, int readTimeoutMillis) {
        this(
                userAgent,
                connectTimeoutMillis,
                readTimeoutMillis,
                /* allowCrossProtocolRedirects= */ false,
                /* defaultRequestProperties= */ null);
    }

    /** @deprecated Use {@link Factory} instead. */
    @Deprecated
    public IneDataSource(
            @Nullable String userAgent,
            int connectTimeoutMillis,
            int readTimeoutMillis,
            boolean allowCrossProtocolRedirects,
            @Nullable RequestProperties defaultRequestProperties) {
        this(
                userAgent,
                connectTimeoutMillis,
                readTimeoutMillis,
                allowCrossProtocolRedirects,
                defaultRequestProperties,
                /* contentTypePredicate= */ null);
    }

    private IneDataSource(
            @Nullable String userAgent,
            int connectTimeoutMillis,
            int readTimeoutMillis,
            boolean allowCrossProtocolRedirects,
            @Nullable RequestProperties defaultRequestProperties,
            @Nullable Predicate<String> contentTypePredicate) {
        super(/* isNetwork= */ true);
        this.userAgent = userAgent;
        this.connectTimeoutMillis = connectTimeoutMillis;
        this.readTimeoutMillis = readTimeoutMillis;
        this.allowCrossProtocolRedirects = allowCrossProtocolRedirects;
        this.defaultRequestProperties = defaultRequestProperties;
        this.contentTypePredicate = contentTypePredicate;
        this.requestProperties = new RequestProperties();
    }

    /**
     * @deprecated Use {@link Factory#setContentTypePredicate(Predicate)}
     *     instead.
     */
    @Deprecated
    public void setContentTypePredicate(@Nullable Predicate<String> contentTypePredicate) {
        this.contentTypePredicate = contentTypePredicate;
    }

    @Override
    @Nullable
    public Uri getUri() {
        return uri;
    }

    @Override
    public int getResponseCode() {
        if(connection == null)
            return cacheResponseCode <= 0 ? -1 : cacheResponseCode;
        return responseCode <= 0 ? -1 : responseCode;

    }

    @Override
    public Map<String, List<String>> getResponseHeaders() {
        if(connection == null)
            return cacheResponseHeaders == null ? Collections.emptyMap() : cacheResponseHeaders;
        return connection.getHeaderFields();
    }

    @Override
    public void setRequestProperty(String name, String value) {
        checkNotNull(name);
        checkNotNull(value);
        requestProperties.set(name, value);
    }

    @Override
    public void clearRequestProperty(String name) {
        checkNotNull(name);
        requestProperties.remove(name);
    }

    @Override
    public void clearAllRequestProperties() {
        requestProperties.clear();
    }

    /**
     * Opens the source to read the specified data.
     */
    @Override
    public long open(DataSpec dataSpec) throws HttpDataSourceException {
        if(CacheStatus != CacheStatus_NoCache)
            StopCache();

        this.dataSpec = dataSpec;
        DataSpec tmpDataSpec;
        if(bufferFillBytes > 0 && dataSpec.position < bufferFillBytes) {
//            if(dataSpec.length != C.LENGTH_UNSET) {
//                if (dataSpec.position + dataSpec.length <= bufferFillBytes) {
//                    transferInitializing(dataSpec);
//                    transferStarted(dataSpec);
//                    bytesToRead = bufferFillBytes;
//                    bytesRead = 0;
//                    return dataSpec.length;
//                } else {
//                    long length = dataSpec.length - (bufferFillBytes - dataSpec.position);
//                    tmpDataSpec = dataSpec.buildUpon().setPosition(bufferFillBytes).setLength(length).build();
//                }
//            }
//            else
                tmpDataSpec = dataSpec.buildUpon().setPosition(bufferFillBytes).build();
        }
        else
            tmpDataSpec = dataSpec;
        bytesRead = 0;
        bytesToRead = 0;
        transferInitializing(dataSpec);
        Log.d("IneDataSource", "open:"+dataSpec.uri.toString()+" Pos:"+dataSpec.position+" Len:"+dataSpec.length+ " cached:"+bufferFillBytes);
        try {

            connection = makeConnection(tmpDataSpec);
        } catch (IOException e) {
            @Nullable String message = e.getMessage();
            if (message != null
                    && Ascii.toLowerCase(message).matches("cleartext http traffic.*not permitted.*")) {
                throw new CleartextNotPermittedException(e, dataSpec);
            }
            throw new HttpDataSourceException(
                    "Unable to connect", e, dataSpec, HttpDataSourceException.TYPE_OPEN);
        }

        HttpURLConnection connection = this.connection;
        String responseMessage;
        try {
            responseCode = connection.getResponseCode();
            responseMessage = connection.getResponseMessage();
        } catch (IOException e) {
            closeConnectionQuietly();
            throw new HttpDataSourceException(
                    "Unable to connect", e, dataSpec, HttpDataSourceException.TYPE_OPEN);
        }

        // Check for a valid response code.
        if (responseCode < 200 || responseCode > 299) {
            Map<String, List<String>> headers = connection.getHeaderFields();
            if (responseCode == 416) {
                long documentSize =
                        HttpUtil.getDocumentSize(connection.getHeaderField(HttpHeaders.CONTENT_RANGE));
                if (tmpDataSpec.position == documentSize) {
                    opened = true;
                    transferStarted(dataSpec);
                    return dataSpec.length != C.LENGTH_UNSET ? dataSpec.length : 0;
                }
            }

            @Nullable InputStream errorStream = connection.getErrorStream();
            byte[] errorResponseBody;
            try {
                errorResponseBody =
                        errorStream != null ? Util.toByteArray(errorStream) : Util.EMPTY_BYTE_ARRAY;
            } catch (IOException e) {
                errorResponseBody = Util.EMPTY_BYTE_ARRAY;
            }
            closeConnectionQuietly();
            InvalidResponseCodeException exception =
                    new InvalidResponseCodeException(
                            responseCode, responseMessage, headers, dataSpec, errorResponseBody);
            if (responseCode == 416) {
                exception.initCause(new DataSourceException(DataSourceException.POSITION_OUT_OF_RANGE));
            }
            throw exception;
        }

        // Check for a valid content type.
        String contentType = connection.getContentType();
        if (contentTypePredicate != null && !contentTypePredicate.apply(contentType)) {
            closeConnectionQuietly();
            throw new InvalidContentTypeException(contentType, dataSpec);
        }

        // If we requested a range starting from a non-zero position and received a 200 rather than a
        // 206, then the server does not support partial requests. We'll need to manually skip to the
        // requested position.
        long bytesToSkip = responseCode == 200 && tmpDataSpec.position != 0 ? tmpDataSpec.position : 0;

        // Determine the length of the data to be read, after skipping.
        boolean isCompressed = isCompressed(connection);
        if (!isCompressed) {
            if (dataSpec.length != C.LENGTH_UNSET) {
                bytesToRead = dataSpec.length;
            } else {
                long contentLength =
                        HttpUtil.getContentLength(
                                connection.getHeaderField(HttpHeaders.CONTENT_LENGTH),
                                connection.getHeaderField(HttpHeaders.CONTENT_RANGE));
                bytesToRead = contentLength != C.LENGTH_UNSET ? (contentLength - bytesToSkip)
                        : C.LENGTH_UNSET;
            }
        } else {
            // Gzip is enabled. If the server opts to use gzip then the content length in the response
            // will be that of the compressed data, which isn't what we want. Always use the dataSpec
            // length in this case.
            bytesToRead = dataSpec.length;
        }

        try {
            inputStream = connection.getInputStream();
            if (isCompressed) {
                inputStream = new GZIPInputStream(inputStream);
            }
        } catch (IOException e) {
            closeConnectionQuietly();
            throw new HttpDataSourceException(e, dataSpec, HttpDataSourceException.TYPE_OPEN);
        }

        opened = true;
        transferStarted(dataSpec);
        if(bytesToSkip > 0) {
            try {
                if (!skipFully(bytesToSkip)) {
                    throw new DataSourceException(DataSourceException.POSITION_OUT_OF_RANGE);
                }
            } catch (IOException e) {
                closeConnectionQuietly();
                throw new HttpDataSourceException(e, dataSpec, HttpDataSourceException.TYPE_OPEN);
            }
        }

        return bytesToRead;
    }
    public void changeCacheSpeed(int KBS) {
        cacheBPS = KBS * 1024;
    }
    public long getMediaLength() {
        return bytesToRead;
    }
    public long getPlayerReadedBytes() {
        return bytesRead;
    }
    public int getCacheDownloadPosition() {
        return preBufferFillBytes;
    }
    public void startCacheBuffer(int size, int KBS){
        if(CacheStatus != CacheStatus_NoCache)
            return;
        final int bufferSize = size;
        cacheBPS = KBS * 1024;
        CacheStatus = CacheStatus_NoCache;
        buffered = new byte[size];
        preBufferFillBytes = 0;
        bufferFillBytes = 0;
        Thread t1=new Thread(()->{
            try {
                cacheBuffer(bufferSize);
            } catch (Exception e) {
                CacheStatus = CacheStatus_CacheError;
                e.printStackTrace();
            }
        });
        t1.start();
    }
    public void StopCache() {
        bExitCacheLoop = true;
        bufferFillBytes = preBufferFillBytes;
    }
    public void freeCacheBuffer() {
        bExitCacheLoop = true;
        buffered = null;
    }
    public void cacheBuffer(int bufferSize) throws Exception {
        DataSpec dataSpec = new DataSpec(uri ,0, C.LENGTH_UNSET);
        cacheBytesToRead = 0;
        InputStream inputStream;


        Log.d("IneDataSource", "cacheopen:"+dataSpec.uri.toString());
        try {
            cacheConnection = makeConnection(dataSpec);
        } catch (IOException e) {
            @Nullable String message = e.getMessage();
            if (message != null
                    && Ascii.toLowerCase(message).matches("cleartext http traffic.*not permitted.*")) {
                throw new CleartextNotPermittedException(e, dataSpec);
            }
            throw new HttpDataSourceException(
                    "Unable to connect", e, dataSpec, HttpDataSourceException.TYPE_OPEN);
        }
        HttpURLConnection connection = cacheConnection;
        String responseMessage;
        try {
            cacheResponseCode = connection.getResponseCode();
            responseMessage = connection.getResponseMessage();
        } catch (IOException e) {
            cacheCloseConnectionQuietly();
            throw new HttpDataSourceException(
                    "Unable to connect", e, dataSpec, HttpDataSourceException.TYPE_OPEN);
        }
        Map<String, List<String>> headers = connection.getHeaderFields();
        cacheResponseHeaders = new HashMap<>(headers);
        // Check for a valid response code.
        if (cacheResponseCode < 200 || cacheResponseCode > 299) {
            boolean opened = false;

            if (cacheResponseCode == 416) {
                long documentSize =
                        HttpUtil.getDocumentSize(connection.getHeaderField(HttpHeaders.CONTENT_RANGE));
                if (dataSpec.position == documentSize) {
                    opened = true;
                }
            }
            if(!opened) {
                @Nullable InputStream errorStream = connection.getErrorStream();
                byte[] errorResponseBody;
                try {
                    errorResponseBody =
                            errorStream != null ? Util.toByteArray(errorStream) : Util.EMPTY_BYTE_ARRAY;
                } catch (IOException e) {
                    errorResponseBody = Util.EMPTY_BYTE_ARRAY;
                }
                cacheCloseConnectionQuietly();
                InvalidResponseCodeException exception =
                        new InvalidResponseCodeException(
                                cacheResponseCode, responseMessage, headers, dataSpec, errorResponseBody);
                if (cacheResponseCode == 416) {
                    exception.initCause(new DataSourceException(DataSourceException.POSITION_OUT_OF_RANGE));
                }
                throw exception;
            }
        }

        // Check for a valid content type.
        String contentType = connection.getContentType();
        if (contentTypePredicate != null && !contentTypePredicate.apply(contentType)) {
            cacheCloseConnectionQuietly();
            throw new InvalidContentTypeException(contentType, dataSpec);
        }

        // If we requested a range starting from a non-zero position and received a 200 rather than a
        // 206, then the server does not support partial requests. We'll need to manually skip to the
        // requested position.
        long bytesToSkip = cacheResponseCode == 200 && dataSpec.position != 0 ? dataSpec.position : 0;

        // Determine the length of the data to be read, after skipping.
        boolean isCompressed = isCompressed(connection);
        if (!isCompressed) {
            if (dataSpec.length != C.LENGTH_UNSET) {
                cacheBytesToRead = dataSpec.length;
            } else {
                long contentLength =
                        HttpUtil.getContentLength(
                                connection.getHeaderField(HttpHeaders.CONTENT_LENGTH),
                                connection.getHeaderField(HttpHeaders.CONTENT_RANGE));
                cacheBytesToRead = contentLength != C.LENGTH_UNSET ? (contentLength - bytesToSkip)
                        : C.LENGTH_UNSET;
            }
        } else {
            // Gzip is enabled. If the server opts to use gzip then the content length in the response
            // will be that of the compressed data, which isn't what we want. Always use the dataSpec
            // length in this case.
            cacheBytesToRead = dataSpec.length;
        }

        try {
            inputStream = connection.getInputStream();
            if (isCompressed) {
                inputStream = new GZIPInputStream(inputStream);
            }
        } catch (IOException e) {
            cacheCloseConnectionQuietly();
            throw new HttpDataSourceException(e, dataSpec, HttpDataSourceException.TYPE_OPEN);
        }
        CacheStatus = CacheStatus_Caching;
        long startTime = System.currentTimeMillis();
        int length = (cacheBytesToRead == C.LENGTH_UNSET)? bufferSize : (int) min(cacheBytesToRead, bufferSize);
        while(length>0 && !bExitCacheLoop){
            int readSize =  (int)min(length, ChunkSize);
            int read = inputStream.read(buffered, preBufferFillBytes, readSize);
            if (read == -1) {
                break;
            }
            length -= read;
            preBufferFillBytes += read;
            while(true) {
                long pastTime = System.currentTimeMillis() - startTime;
                if(pastTime>500) {
                    int BPS = (int) ((long) preBufferFillBytes * 1000 / pastTime);
                    if (BPS > cacheBPS) {
                        Thread.sleep(10);
                        continue;
                    }
                }
                break;
            }
        }
        CacheStatus = CacheStatus_Cached;
        cacheCloseConnectionQuietly();
        //return bytesToRead;
    }
    @Override
    public int read(byte[] buffer, int offset, int readLength) throws HttpDataSourceException {
        try {
            return readInternal(buffer, offset, readLength);
        } catch (IOException e) {
            throw new HttpDataSourceException(
                    e, castNonNull(dataSpec), HttpDataSourceException.TYPE_READ);
        }
    }

    @Override
    public void close() throws HttpDataSourceException {
        try {
            @Nullable InputStream inputStream = this.inputStream;
            Log.d("IneDataSource", "close:"+dataSpec.uri.toString());
            if (inputStream != null) {
                long bytesRemaining =
                        bytesToRead == C.LENGTH_UNSET ? C.LENGTH_UNSET : bytesToRead - bytesRead;
                maybeTerminateInputStream(connection, bytesRemaining);
                try {
                    inputStream.close();
                } catch (IOException e) {
                    throw new HttpDataSourceException(
                            e, castNonNull(dataSpec), HttpDataSourceException.TYPE_CLOSE);
                }
            }
        } finally {
            inputStream = null;
            closeConnectionQuietly();
            if (opened) {
                opened = false;
                transferEnded();
            }
        }
    }

    /**
     * Establishes a connection, following redirects to do so where permitted.
     */
    private HttpURLConnection makeConnection(DataSpec dataSpec) throws IOException {
        URL url = new URL(dataSpec.uri.toString());
        @DataSpec.HttpMethod int httpMethod = dataSpec.httpMethod;
        @Nullable byte[] httpBody = dataSpec.httpBody;
        long position = dataSpec.position;
        long length = dataSpec.length;
        boolean allowGzip = dataSpec.isFlagSet(DataSpec.FLAG_ALLOW_GZIP);

        if (!allowCrossProtocolRedirects) {
            // HttpURLConnection disallows cross-protocol redirects, but otherwise performs redirection
            // automatically. This is the behavior we want, so use it.
            return makeConnection(
                    url,
                    httpMethod,
                    httpBody,
                    position,
                    length,
                    allowGzip,
                    /* followRedirects= */ true,
                    dataSpec.httpRequestHeaders);
        }

        // We need to handle redirects ourselves to allow cross-protocol redirects.
        int redirectCount = 0;
        while (redirectCount++ <= MAX_REDIRECTS) {
            HttpURLConnection connection =
                    makeConnection(
                            url,
                            httpMethod,
                            httpBody,
                            position,
                            length,
                            allowGzip,
                            /* followRedirects= */ false,
                            dataSpec.httpRequestHeaders);
            int responseCode = connection.getResponseCode();
            String location = connection.getHeaderField("Location");
            if ((httpMethod == DataSpec.HTTP_METHOD_GET || httpMethod == DataSpec.HTTP_METHOD_HEAD)
                    && (responseCode == HttpURLConnection.HTTP_MULT_CHOICE
                    || responseCode == HttpURLConnection.HTTP_MOVED_PERM
                    || responseCode == HttpURLConnection.HTTP_MOVED_TEMP
                    || responseCode == HttpURLConnection.HTTP_SEE_OTHER
                    || responseCode == HTTP_STATUS_TEMPORARY_REDIRECT
                    || responseCode == HTTP_STATUS_PERMANENT_REDIRECT)) {
                connection.disconnect();
                url = handleRedirect(url, location);
            } else if (httpMethod == DataSpec.HTTP_METHOD_POST
                    && (responseCode == HttpURLConnection.HTTP_MULT_CHOICE
                    || responseCode == HttpURLConnection.HTTP_MOVED_PERM
                    || responseCode == HttpURLConnection.HTTP_MOVED_TEMP
                    || responseCode == HttpURLConnection.HTTP_SEE_OTHER)) {
                // POST request follows the redirect and is transformed into a GET request.
                connection.disconnect();
                httpMethod = DataSpec.HTTP_METHOD_GET;
                httpBody = null;
                url = handleRedirect(url, location);
            } else {
                return connection;
            }
        }

        // If we get here we've been redirected more times than are permitted.
        throw new NoRouteToHostException("Too many redirects: " + redirectCount);
    }

    /**
     * Configures a connection and opens it.
     *
     * @param url The url to connect to.
     * @param httpMethod The http method.
     * @param httpBody The body data, or {@code null} if not required.
     * @param position The byte offset of the requested data.
     * @param length The length of the requested data, or {@link C#LENGTH_UNSET}.
     * @param allowGzip Whether to allow the use of gzip.
     * @param followRedirects Whether to follow redirects.
     * @param requestParameters parameters (HTTP headers) to include in request.
     */
    private HttpURLConnection makeConnection(
            URL url,
            @DataSpec.HttpMethod int httpMethod,
            @Nullable byte[] httpBody,
            long position,
            long length,
            boolean allowGzip,
            boolean followRedirects,
            Map<String, String> requestParameters)
            throws IOException {

        URL targetUrl = url;
        String urlString = url.toString();
        if(urlString.startsWith(VODReplaceSign)){
            urlString = VODServerBaseUrl[VODServerIndex] + urlString.substring(VODReplaceSign.length());
            targetUrl = new URL(urlString);
        }

        HttpURLConnection connection = openConnection(targetUrl);
        connection.setConnectTimeout(connectTimeoutMillis);
        connection.setReadTimeout(readTimeoutMillis);

        Map<String, String> requestHeaders = new HashMap<>();
        if (defaultRequestProperties != null) {
            requestHeaders.putAll(defaultRequestProperties.getSnapshot());
        }
        requestHeaders.putAll(requestProperties.getSnapshot());
        requestHeaders.putAll(requestParameters);

        for (Map.Entry<String, String> property : requestHeaders.entrySet()) {
            connection.setRequestProperty(property.getKey(), property.getValue());
        }

        @Nullable String rangeHeader = buildRangeRequestHeader(position, length);
        if (rangeHeader != null) {
            connection.setRequestProperty(HttpHeaders.RANGE, rangeHeader);
        }
        if (userAgent != null) {
            connection.setRequestProperty(HttpHeaders.USER_AGENT, userAgent);
        }
        connection.setRequestProperty(HttpHeaders.ACCEPT_ENCODING, allowGzip ? "gzip" : "identity");
        connection.setInstanceFollowRedirects(followRedirects);
        connection.setDoOutput(httpBody != null);
        connection.setRequestMethod(DataSpec.getStringForHttpMethod(httpMethod));

        if (httpBody != null) {
            connection.setFixedLengthStreamingMode(httpBody.length);
            connection.connect();
            OutputStream os = connection.getOutputStream();
            os.write(httpBody);
            os.close();
        } else {
            connection.connect();
        }
        uri = Uri.parse(connection.getURL().toString());
        return connection;
    }

    /** Creates an {@link HttpURLConnection} that is connected with the {@code url}. */
    @VisibleForTesting
    /* package */ HttpURLConnection openConnection(URL url) throws IOException {
        return (HttpURLConnection) url.openConnection();
    }

    /**
     * Handles a redirect.
     *
     * @param originalUrl The original URL.
     * @param location The Location header in the response. May be {@code null}.
     * @return The next URL.
     * @throws IOException If redirection isn't possible.
     */
    private static URL handleRedirect(URL originalUrl, @Nullable String location) throws IOException {
        if (location == null) {
            throw new ProtocolException("Null location redirect");
        }
        // Form the new url.
        URL url = new URL(originalUrl, location);
        // Check that the protocol of the new url is supported.
        String protocol = url.getProtocol();
        if (!"https".equals(protocol) && !"http".equals(protocol)) {
            throw new ProtocolException("Unsupported protocol redirect: " + protocol);
        }
        // Currently this method is only called if allowCrossProtocolRedirects is true, and so the code
        // below isn't required. If we ever decide to handle redirects ourselves when cross-protocol
        // redirects are disabled, we'll need to uncomment this block of code.
        // if (!allowCrossProtocolRedirects && !protocol.equals(originalUrl.getProtocol())) {
        //   throw new ProtocolException("Disallowed cross-protocol redirect ("
        //       + originalUrl.getProtocol() + " to " + protocol + ")");
        // }
        return url;
    }

    /**
     * Attempts to skip the specified number of bytes in full.
     *
     * @param bytesToSkip The number of bytes to skip.
     * @throws InterruptedIOException If the thread is interrupted during the operation.
     * @throws IOException If an error occurs reading from the source.
     * @return Whether the bytes were skipped in full. If {@code false} then the data ended before the
     *     specified number of bytes were skipped. Always {@code true} if {@code bytesToSkip == 0}.
     */
    private boolean skipFully(long bytesToSkip) throws IOException {
        if (bytesToSkip == 0) {
            return true;
        }
        byte[] skipBuffer = new byte[4096];
        while (bytesToSkip > 0) {
            int readLength = (int) min(bytesToSkip, skipBuffer.length);
            int read = castNonNull(inputStream).read(skipBuffer, 0, readLength);
            if (Thread.currentThread().isInterrupted()) {
                throw new InterruptedIOException();
            }
            if (read == -1) {
                return false;
            }
            bytesToSkip -= read;
            bytesTransferred(read);
        }
        return true;
    }


    //================================================================
    /**
     * Reads up to {@code length} bytes of data and stores them into {@code buffer}, starting at
     * index {@code offset}.
     * <p>
     * This method blocks until at least one byte of data can be read, the end of the opened range is
     * detected, or an exception is thrown.
     *
     * @param buffer The buffer into which the read data should be stored.
     * @param offset The start offset into {@code buffer} at which data should be written.
     * @param readLength The maximum number of bytes to read.
     * @return The number of bytes read, or {@link C#RESULT_END_OF_INPUT} if the end of the opened
     *     range is reached.
     * @throws IOException If an error occurs reading from the source.
     */
    private int readInternal(byte[] buffer, int offset, int readLength) throws IOException {
        if (readLength == 0) {
            return 0;
        }

        if (bytesToRead != C.LENGTH_UNSET) {
            long bytesRemaining = bytesToRead - bytesRead;
            if (bytesRemaining == 0) {
                return C.RESULT_END_OF_INPUT;
            }
            readLength = (int) min(readLength, bytesRemaining);
        }
        int read = 0;
        if(buffered!=null) {
            //int StreamOffset = (int)dataSpec.position + (int)bytesRead;
            long StreamOffset = dataSpec.position + bytesRead;
            long inBufferSize = (long)bufferFillBytes - StreamOffset;
            if(inBufferSize>0) {
                int bufferedSize = (int)min(inBufferSize, (long)readLength);
                System.arraycopy(buffered, (int)StreamOffset, buffer, offset, bufferedSize);
                read += bufferedSize;
                offset += bufferedSize;
                readLength -= bufferedSize;
                //castNonNull(inputStream).skip(bufferedSize);
            }
        }
        if(readLength > 0) {
            int read1 = castNonNull(inputStream).read(buffer, offset, readLength);
            if (read1 == -1) {
                return C.RESULT_END_OF_INPUT;
            }
            read += read1;
        }
        bytesRead += read;
        bytesTransferred(read);
        return read;
    }

    /**
     * On platform API levels 19 and 20, okhttp's implementation of {@link InputStream#close} can
     * block for a long time if the stream has a lot of data remaining. Call this method before
     * closing the input stream to make a best effort to cause the input stream to encounter an
     * unexpected end of input, working around this issue. On other platform API levels, the method
     * does nothing.
     *
     * @param connection The connection whose {@link InputStream} should be terminated.
     * @param bytesRemaining The number of bytes remaining to be read from the input stream if its
     *     length is known. {@link C#LENGTH_UNSET} otherwise.
     */
    private static void maybeTerminateInputStream(
            @Nullable HttpURLConnection connection, long bytesRemaining) {
        if (connection == null || Util.SDK_INT < 19 || Util.SDK_INT > 20) {
            return;
        }

        try {
            InputStream inputStream = connection.getInputStream();
            if (bytesRemaining == C.LENGTH_UNSET) {
                // If the input stream has already ended, do nothing. The socket may be re-used.
                if (inputStream.read() == -1) {
                    return;
                }
            } else if (bytesRemaining <= MAX_BYTES_TO_DRAIN) {
                // There isn't much data left. Prefer to allow it to drain, which may allow the socket to be
                // re-used.
                return;
            }
            String className = inputStream.getClass().getName();
            if ("com.android.okhttp.internal.http.HttpTransport$ChunkedInputStream".equals(className)
                    || "com.android.okhttp.internal.http.HttpTransport$FixedLengthInputStream"
                    .equals(className)) {
                Class<?> superclass = inputStream.getClass().getSuperclass();
                Method unexpectedEndOfInput =
                        checkNotNull(superclass).getDeclaredMethod("unexpectedEndOfInput");
                unexpectedEndOfInput.setAccessible(true);
                unexpectedEndOfInput.invoke(inputStream);
            }
        } catch (Exception e) {
            // If an IOException then the connection didn't ever have an input stream, or it was closed
            // already. If another type of exception then something went wrong, most likely the device
            // isn't using okhttp.
        }
    }

    /**
     * Closes the current connection quietly, if there is one.
     */
    private void closeConnectionQuietly() {
        if (connection != null) {
            try {
                connection.disconnect();
            } catch (Exception e) {
                Log.e(TAG, "Unexpected error while disconnecting", e);
            }
            connection = null;
        }
    }
    private void cacheCloseConnectionQuietly() {
        if (cacheConnection != null) {
            try {
                cacheConnection.disconnect();
            } catch (Exception e) {
                Log.e(TAG, "Unexpected error while disconnecting cache", e);
            }
            cacheConnection = null;
        }
    }

    private static boolean isCompressed(HttpURLConnection connection) {
        String contentEncoding = connection.getHeaderField("Content-Encoding");
        return "gzip".equalsIgnoreCase(contentEncoding);
    }
}
