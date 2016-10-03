package com.linkedpipes.plugin.transformer.unpack;

import com.linkedpipes.etl.component.api.Component;
import com.linkedpipes.etl.component.api.service.ExceptionFactory;
import com.linkedpipes.etl.component.api.service.ProgressReport;
import com.linkedpipes.etl.dataunit.system.api.files.FilesDataUnit;
import com.linkedpipes.etl.dataunit.system.api.files.WritableFilesDataUnit;
import com.linkedpipes.etl.executor.api.v1.exception.LpException;
import org.apache.commons.compress.archivers.ArchiveException;
import org.apache.commons.compress.archivers.ArchiveInputStream;
import org.apache.commons.compress.archivers.ArchiveStreamFactory;
import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.commons.compress.compressors.bzip2.BZip2CompressorInputStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.zip.GZIPInputStream;

/**
 * @author Škoda Petr
 */
public final class Unpack implements Component.Sequential {

    private static final Logger LOG = LoggerFactory.getLogger(Unpack.class);

    @Component.InputPort(id = "FilesInput")
    public FilesDataUnit input;

    @Component.OutputPort(id = "FilesOutput")
    public WritableFilesDataUnit output;

    @Component.Configuration
    public UnpackConfiguration configuration;

    @Component.Inject
    public ProgressReport progressReport;

    @Component.Inject
    public ExceptionFactory exceptionFactory;

    @Override
    public void execute() throws LpException {
        LOG.info("Used extension option: {}", configuration.getFormat());
        progressReport.start(input.size());
        for (FilesDataUnit.Entry entry : input) {
            final File outputDirectory;
            if (configuration.isUsePrefix()) {
                outputDirectory = new File(output.getRootDirectory(),
                        entry.getFileName());
            } else {
                outputDirectory = output.getRootDirectory();
            }
            outputDirectory.mkdirs();
            // Unpack.
            unpack(entry, outputDirectory);
            //
            progressReport.entryProcessed();
        }
        progressReport.done();
        //
    }

    private void unpack(FilesDataUnit.Entry inputEntry, File targetDirectory)
            throws LpException {
        final String extension = getExtension(inputEntry);
        try (final InputStream stream = new FileInputStream(
                inputEntry.toFile())) {
            switch (extension) {
                case ArchiveStreamFactory.ZIP:
                    unpackZip(stream, targetDirectory);
                    break;
                case "bz2":
                    unpackBzip2(stream, targetDirectory, inputEntry);
                    break;
                case "gz":
                    unpackGzip(stream, targetDirectory, inputEntry);
                    break;
                default:
                    throw exceptionFactory.failure(
                            "Unknown file format (" + extension + ") : " +
                                    inputEntry.getFileName());
            }
        } catch (IOException | ArchiveException ex) {
            throw exceptionFactory.failure("Extraction failure: {}",
                    inputEntry.getFileName(), ex);
        }
    }

    /**
     * Return lower case extension of file, or extension defined by configuration.
     *
     * @param entry
     * @return
     */
    private String getExtension(FilesDataUnit.Entry entry) {
        if (configuration.getFormat() == null || configuration.getFormat().isEmpty()) {
            LOG.debug("No format setting provided.");
            configuration.setFormat(UnpackVocabulary.FORMAT_DETECT);
        }
        switch (configuration.getFormat()) {
            case UnpackVocabulary.FORMAT_ZIP:
                return ArchiveStreamFactory.ZIP;
            case UnpackVocabulary.FORMAT_BZIP2:
                return "bz2";
            case UnpackVocabulary.FORMAT_GZIP:
                return "gz";
            case UnpackVocabulary.FORMAT_DETECT:
            default:
                final String fileName = entry.getFileName();
                return fileName.substring(fileName.lastIndexOf(".") + 1).toLowerCase();
        }
    }

    /**
     * Unpack ZIP archive.
     *
     * @param inputStream
     * @param targetDirectory
     */
    private static void unpackZip(InputStream inputStream, File targetDirectory) throws IOException, ArchiveException {
        try (ArchiveInputStream archiveStream = new ArchiveStreamFactory().createArchiveInputStream("zip", inputStream)) {
            ZipArchiveEntry entry;
            while ((entry = (ZipArchiveEntry) archiveStream.getNextEntry()) != null) {
                final File entryFile = new File(targetDirectory, entry.getName());
                // Create directories based on file path.
                if (entry.getName().endsWith("/")) {
                    if (!entryFile.exists()) {
                        entryFile.mkdirs();
                    }
                    continue;
                }
                if (entryFile.isDirectory() || entryFile.exists()) {
                    continue;
                }
                // Copy stream to file.
                copyToFile(archiveStream, entryFile);
            }
        }
    }

    private static void unpackBzip2(InputStream inputStream, File targetDirectory, FilesDataUnit.Entry inputEntry) throws IOException {
        try (final BZip2CompressorInputStream bzip2Stream = new BZip2CompressorInputStream(inputStream, true)) {
            final String outputFileName = inputEntry.getFileName().substring(0, inputEntry.getFileName().lastIndexOf("."));
            final File outputFile = new File(targetDirectory, outputFileName);
            outputFile.getParentFile().mkdirs();
            // Copy stream to file.
            copyToFile(bzip2Stream, outputFile);
        }
    }

    private static void unpackGzip(InputStream inputStream,
            File targetDirectory, FilesDataUnit.Entry inputEntry)
            throws IOException {
        String outputFileName = inputEntry.getFileName();
        if (outputFileName.toLowerCase().endsWith(".gz")) {
            outputFileName = outputFileName.substring(0,
                    outputFileName.length() - 3);
        }
        try (GZIPInputStream gzipStream = new GZIPInputStream(inputStream)) {
            copyToFile(gzipStream, new File(targetDirectory, outputFileName));
        }
    }

    /**
     * Write given stream to a file.
     *
     * @param stream
     * @param file
     */
    private static void copyToFile(InputStream stream, File file)
            throws IOException {
        try (FileOutputStream out = new FileOutputStream(file)) {
            final byte[] buffer = new byte[8196];
            int length;
            while ((length = stream.read(buffer)) > 0) {
                out.write(buffer, 0, length);
                out.flush();
            }
        }
    }

}
