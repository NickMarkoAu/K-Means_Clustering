package com.file;

import com.univocity.parsers.csv.CsvParser;
import com.univocity.parsers.csv.CsvParserSettings;
import lombok.extern.slf4j.Slf4j;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.SequenceInputStream;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;
import java.util.zip.GZIPInputStream;

@Slf4j
public class CsvUtil {

    private final CsvParserSettings parserSettings;
    private static final int BUFFER_SIZE = 66560;


    public CsvUtil(CsvParserSettings parserSettings) {
        this.parserSettings = parserSettings;
    }

    public Reader reader(String filePath) {
        return new BufferedReader(new InputStreamReader(in(filePath)));
    }

    public Stream<String[]> readStream(String filePath) {
        try {
            final Reader reader = reader(filePath);

            // univocity parser defaults to 4096 which is okay for most cases.
            // However, columns for geometry string require a lot more.
            final CsvParserSettings settings = parserSettings.clone();
            settings.setMaxCharsPerColumn(-1);

            final CsvParser parser = new CsvParser(settings);
            return StreamSupport.stream(parser.iterate(reader).spliterator(), false).onClose(() -> {
                try {
                    log.trace("Closing {}", filePath);
                    reader.close();
                } catch (IOException e) {
                    log.warn("Could not close resource " + filePath, e);
                }
            });
        } catch (Exception e) {
            throw new RuntimeException("Could not access resource '" + filePath + "'", e);
        }
    }

    public static int columns(Stream<String[]> rowStream) {
        return rowStream.collect(Collectors.toList()).get(0).length;
    }

    private InputStream in(String path) {
        try {
            final File file = new File(path).getCanonicalFile();
            log.info("Accessing {} using base path {}", file, new File(".").getCanonicalPath());
            if (file.exists()) {
                if (file.isFile()) {
                    return new FileInputStream(file);
                } else {
                    final File[] files = file.listFiles(new FileFilter() {
                        // TODO
                        @Override
                        public boolean accept(File pathname) {
                            final String name = pathname.getName().toLowerCase();
                            return name.endsWith(".csv") || name.endsWith(".csv.gz") || name.endsWith(".csv000.gz");
                        }
                    });
                    List<InputStream> iss = Arrays.stream(files)
                            .filter(File::isFile)
                            .map(f -> {
                                try {
                                    return toInputStream(f);
                                } catch (Exception e) {
                                    throw new RuntimeException(e);
                                }
                            }).collect(Collectors.toList());

                    // make sure each file is separated with a new line so they don't run together
                    InputStream sis = null;
                    if (iss.size() > 1) {
                        for (InputStream inputStream : iss) {
                            if (sis == null) {
                                sis = ensureNewLine(inputStream);
                            } else {
                                sis = new SequenceInputStream(sis, ensureNewLine(inputStream));
                            }
                        }
                        return sis;
                    } else {
                        return iss.get(0);
                    }
                }
            } else {
                throw new RuntimeException("Could not find " + file.getAbsolutePath());
            }
        } catch (IOException e) {
            throw new RuntimeException("Error encountered while trying to access " + path, e);
        }
    }

    private SequenceInputStream ensureNewLine(InputStream inputStream) {
        return new SequenceInputStream(inputStream, new ByteArrayInputStream("\n".getBytes()));
    }

    private InputStream toInputStream(File file) throws IOException {
        try (FileInputStream fileInputStream = new FileInputStream(file)) {

            // read first couple of bytes from file
            byte[] header = new byte[2];
            if (fileInputStream.read(header, 0, 2) == 2) {
                int magic = header[0] & 0xff | ((header[1] << 8) & 0xff00);

                // test for gzip
                if (magic == GZIPInputStream.GZIP_MAGIC) {
                    // set buffer size for best performance
                    return new GZIPInputStream(new FileInputStream(file), BUFFER_SIZE);
                }
            }

            // default to normal file input stream
            return new BufferedInputStream(new FileInputStream(file), BUFFER_SIZE);

        }
    }
}
