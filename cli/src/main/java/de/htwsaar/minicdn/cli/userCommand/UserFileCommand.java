package de.htwsaar.minicdn.cli.userCommand;

import de.htwsaar.minicdn.cli.service.DownloadService;
import de.htwsaar.minicdn.cli.service.DownloadValidator;
import java.nio.file.Path;
import java.util.concurrent.Callable;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;

@Command(
        name = "file",
        description = "Download files from the CDN",
        subcommands = {UserFileCommand.UserFileDownloadCommand.class})
public class UserFileCommand implements Runnable {
    @Override
    public void run() {
        CommandLine.usage(this, System.out);
    }

    @Command(
            name = "download",
            description = "Download a file by its remote path",
            mixinStandardHelpOptions = true)
    public static class UserFileDownloadCommand implements Callable<Integer> {

        @Parameters(index = "0", description = "Remote file path, e.g. docs/handbook.pdf")
        String remotePath;

        @Option(
                names = {"-o", "--out"},
                required = true,
                description = "Local output path, e.g. ./downloads/handbook.pdf")
        Path outputPath;

        @Option(
                names = "--router",
                required = true,
                description = "Router base URL, e.g. http://localhost:8081")
        String routerBaseUrl;

        @Option(
                names = "--region",
                required = true,
                description = "Client region for routing, e.g. eu-central")
        String region;

        @Option(
                names = "--overwrite",
                description = "Overwrite existing output file if it exists.")
        boolean overwrite;

        @Override
        public Integer call() {
            try {
                DownloadValidator.normalizeRemotePath(remotePath);
                DownloadValidator.validateOutputPath(outputPath, overwrite);
                DownloadValidator.validateRegion(region);
            } catch (IllegalArgumentException ex) {
                System.err.println("Ungültige Eingabe: " + ex.getMessage());
                return 1;
            }

            DownloadService service = new DownloadService();
            return service.downloadFile(routerBaseUrl, region, remotePath, outputPath, overwrite);
        }
    }
}
