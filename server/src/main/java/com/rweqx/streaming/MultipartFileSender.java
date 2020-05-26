package com.rweqx.streaming;

import com.rweqx.utils.HttpUtils;
import org.apache.commons.lang3.StringUtils;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.FileTime;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

/**
 * Allows MultipartFile Sending - aka video preview/streaming in the client.
 */
public class MultipartFileSender {

    private static final Logger LOGGER = Logger.getLogger(MultipartFileSender.class.getName());

    private final String IF_NONE_MATCH = "If-None-Match";
    private final String ETAG = "ETag";
    private final String IF_MODIFIED_SINCE = "If-Modified-Since";
    private final String IF_MATCH = "If-Match";
    private final String IF_UNMODIFIED_SINCE = "If-Unmodified-Since";

    private final String RANGE_REGEX = "^bytes=\\d*-\\d*(,\\d*-\\d*)*$";
    private final String RANGE = "Range";
    private final String CONTENT_RANGE = "Content-Range";
    private final String IF_RANGE = "If-Range";
    private final String CONTENT_RANGE_BYTES_HEADER_PREFIX = "bytes */";

    private final String ACCEPT = "Accept";
    private final String INLINE = "inline";
    private final String ATTACHMENT = "attachment";

    private final String CONTENT_TYPE = "Content-Type";
    private final String CONTENT_DISPOSITION = "Content-Disposition";
    private final String ACCEPT_RANGES = "Accept-Ranges";
    private final String BYTES = "bytes";
    private final String LAST_MODIFIED = "Last-Modified";
    private final String EXPIRES = "Expires";
    private final String CONTENT_LENGTH = "Content-Length";


    private final int DEFAULT_BUFFER_SIZE = 20480; //20kb?
    private final long DEFAULT_EXPIRE_TIME = 60 * 60 * 24 * 7 * 1000L; // 1 week in ms.
    private final String MULTIPART_BOUNDARY = "MULTIPART_BYTERANGES";
    private final String MULTIPART_CONTENT_TYPE = "multipart/byteranges; boundary=" + MULTIPART_BOUNDARY;

    private Path path;
    private HttpServletRequest request;
    private HttpServletResponse response;


    private MultipartFileSender() {
    }

    public static MultipartFileSender fromFile(File file) {
        return new MultipartFileSender().setPath(file.toPath());
    }

    private MultipartFileSender setPath(Path path) {
        this.path = path;
        return this;
    }

    public MultipartFileSender setResponse(HttpServletResponse response) {
        this.response = response;
        return this;
    }

    public MultipartFileSender setRequest(HttpServletRequest request) {
        this.request = request;
        return this;
    }

    public void serveResource() throws IOException {
        if (response == null || request == null) {
            LOGGER.info("Response or request was not set");
            return;
        }

        if (!Files.exists(path)) {
            LOGGER.severe("Trying to stream file that does not exist");
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }

        long length = Files.size(path);
        String fileName = path.getFileName().toString();
        FileTime lastModified = Files.getLastModifiedTime(path);

        if (StringUtils.isEmpty(fileName) || lastModified == null) {
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            return;
        }

        long lastModifiedL = LocalDateTime.ofInstant(lastModified.toInstant(), ZoneId.of(ZoneOffset.systemDefault().getId())).toEpochSecond(ZoneOffset.UTC);


        if (doesHeaderHaveProblem(fileName, lastModifiedL)) {
            return;
        }

        Range full = new Range(0, length - 1, length);
        List<Range> ranges = new ArrayList<>();

        String rangeHeader = request.getHeader(RANGE);
        if (rangeHeader != null) {
            // Check if valid.
            if (!rangeHeader.matches(RANGE_REGEX)) {

                response.setHeader(CONTENT_RANGE, CONTENT_RANGE_BYTES_HEADER_PREFIX + length);
                LOGGER.severe("Range not fitting regex " + rangeHeader);
                response.sendError(HttpServletResponse.SC_REQUESTED_RANGE_NOT_SATISFIABLE);
                return;

            }

            String ifRange = request.getHeader(IF_RANGE);
            if (ifRange != null && !ifRange.equals(fileName)) {
                try {
                    long ifRangeTime = request.getDateHeader(IF_RANGE); // Throws IAE if invalid.
                    if (ifRangeTime != -1) {
                        ranges.add(full); // Read full.
                    }
                } catch (IllegalArgumentException e) {
                    ranges.add(full);
                }
            }

            if (ranges.isEmpty()) {
                for (String part : rangeHeader.substring(6).split(",")) {
                    long start = sublong(part, 0, part.indexOf("-"));
                    long end = sublong(part, part.indexOf("-") + 1, part.length());

                    if (start == -1) {
                        // X from end.
                        start = length - end;
                        end = length - 1;
                    } else if (end == -1 || end > length - 1) {
                        //End is over. set to end.
                        end = length - 1;
                    }
                    if (start > end) {
                        LOGGER.severe("Start > end");
                        response.setHeader(CONTENT_RANGE, CONTENT_RANGE_BYTES_HEADER_PREFIX + length); // 416
                        response.sendError(HttpServletResponse.SC_REQUESTED_RANGE_NOT_SATISFIABLE);
                        return;
                    }
                    ranges.add(new Range(start, end, length));
                }
            }
        }


        //Prepare response.
        String disposition = "inline"; //TODO what is this for?
        String contentType = "video/mp4"; // todo get proper contentype if needed.

        if (contentType == null) {
            contentType = "application/octet-stream"; // TODO but we set this to mp4?
        } else if (!contentType.startsWith("image")) {
            String accept = request.getHeader(ACCEPT);
            disposition = accept != null && HttpUtils.accepts(accept, contentType) ? INLINE : ATTACHMENT;
        }

        // Setup some response stuff;
        LOGGER.info("Content-Type - " + contentType);
        LOGGER.info("Content-Disposition - " + disposition);

        response.reset();
        response.setBufferSize(DEFAULT_BUFFER_SIZE);
        response.setHeader(CONTENT_TYPE, contentType);
        response.setHeader(CONTENT_DISPOSITION, disposition + ";filename=\"" + fileName + "\"");
        response.setHeader(ACCEPT_RANGES, BYTES);
        response.setHeader(ETAG, fileName);
        response.setDateHeader(LAST_MODIFIED, lastModifiedL);
        response.setDateHeader(EXPIRES, System.currentTimeMillis() + DEFAULT_EXPIRE_TIME); //Approximate.


        // Now do the actual writing!
        try (InputStream input = new BufferedInputStream(Files.newInputStream(path))) {
            OutputStream output = response.getOutputStream();

            if (ranges.isEmpty() || ranges.get(0) == full) {
                //Sending non-partila data...
                LOGGER.info("Returning the whole file");

                response.setContentType(contentType);
                response.setHeader(CONTENT_RANGE, "bytes " + full.start + "-" + full.end + "/" + full.total);
                response.setHeader(CONTENT_LENGTH, String.valueOf(full.length));

                copy(input, output, length, full.start, full.length);
            } else if (ranges.size() == 1) {
                // Return single part of file.
                Range r = ranges.get(0);
                response.setContentType(contentType);
                response.setHeader("Content-Range", "bytes " + r.start + "-" + r.end + "/" + r.total);
                response.setHeader("Content-Length", String.valueOf(r.length));
                response.setStatus(HttpServletResponse.SC_PARTIAL_CONTENT); // 206.
                // Copy single part range.
                copy(input, output, length, r.start, r.length);

            } else {
                response.setContentType(MULTIPART_CONTENT_TYPE);
                response.setStatus(HttpServletResponse.SC_PARTIAL_CONTENT);

                ServletOutputStream sos = (ServletOutputStream) output;

                LOGGER.info("Request was for partial data");

                for (Range r : ranges) {
                    LOGGER.info("Writing partial from " + r.start + " to " + r.end);

                    //Print the important headers inbetween each partial data.
                    sos.println();
                    sos.println("--" + MULTIPART_BOUNDARY);
                    sos.println("Content-Type:" + contentType);
                    sos.println("Content-Range: bytes " + r.start + "-" + r.end + "/" + r.total);
                    copy(input, output, length, r.start, r.length);
                }

                // End multipart boundary
                sos.println();
                sos.println("--" + MULTIPART_BOUNDARY + "--");

            }
        }
    }

    private long sublong(String value, int begin, int end) {
        String substring = value.substring(begin, end);
        return (substring.length() > 0) ? Long.parseLong(substring) : -1;
    }


    private boolean doesHeaderHaveProblem(String fileName, long lastModifiedL) throws IOException {

        // Validate Headers
        String ifNoneMatch = request.getHeader(IF_NONE_MATCH);
        long ifModifiedSince = request.getDateHeader(IF_MODIFIED_SINCE);
        String ifMatch = request.getHeader(IF_MATCH);
        long ifUnmodifiedSince = request.getDateHeader(IF_UNMODIFIED_SINCE);

        LOGGER.info("Headers content - " + ifNoneMatch + " " + ifModifiedSince + " " + ifMatch + " " + ifUnmodifiedSince);

        // Check the headers.

        if (ifNoneMatch != null && HttpUtils.matches(ifNoneMatch, fileName)) {
            LOGGER.severe("If none match header doesn't contain * or ETag.");
            response.setHeader(ETAG, fileName);
            response.sendError(HttpServletResponse.SC_NOT_MODIFIED);
            return true;
        }

        if (ifNoneMatch == null && ifModifiedSince != -1 && ifModifiedSince + 1000 > lastModifiedL) {
            LOGGER.severe("304 cuz modified since is wrong");
            response.setHeader(ETAG, fileName);
            response.sendError(HttpServletResponse.SC_NOT_MODIFIED);
            return true;
        }

        if (ifMatch != null && HttpUtils.matches(ifMatch, fileName)) {
            LOGGER.severe("304 cuz ifmatch didn't contain *, return 412");
            response.sendError(HttpServletResponse.SC_PRECONDITION_FAILED);
            return true;
        }

        if (ifUnmodifiedSince != -1 && ifUnmodifiedSince + 1000 <= lastModifiedL) {
            LOGGER.severe("if unmodified since header not greater than lastModifiedL");
            response.sendError(HttpServletResponse.SC_PRECONDITION_FAILED);
            return true;
        }
        return false;
    }

    private void copy(InputStream input, OutputStream output, long inputSize, long start, long length) throws IOException {
        byte[] buffer = new byte[DEFAULT_BUFFER_SIZE];
        int read;

        if (inputSize == length) {
            // Full range
            while ((read = input.read(buffer)) > 0) {
                output.write(buffer, 0, read);
                output.flush();
            }
        } else {
            input.skip(start);
            long toRead = length;

            while ((read = input.read(buffer)) > 0) {
                if ((toRead -= read) > 0) {
                    output.write(buffer, 0, read);
                    output.flush();
                } else {
                    output.write(buffer, 0, (int) toRead + read);
                    output.flush();
                    break;
                }
            }
        }
    }

    private class Range {
        private long start;
        private long end;
        private long length;
        private long total;

        public Range(long start, long end, long total) {
            this.start = start;
            this.end = end;
            this.length = end - start + 1;
            this.total = total;
        }
    }


}
