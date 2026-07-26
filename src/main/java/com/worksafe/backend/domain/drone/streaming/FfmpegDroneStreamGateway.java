package com.worksafe.backend.domain.drone.streaming;

import com.worksafe.backend.global.common.exception.BusinessException;
import com.worksafe.backend.global.common.exception.ErrorCode;
import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

@Slf4j
@Service
public class FfmpegDroneStreamGateway implements DroneStreamGateway {

    private static final Pattern SAFE_STREAM_KEY = Pattern.compile("[A-Za-z0-9._-]{1,100}");
    private static final String PLAYLIST_FILE_NAME = "index.m3u8";

    private final DroneStreamingProperties properties;
    private final Path outputRoot;
    private final Map<String, RunningStream> runningStreams = new ConcurrentHashMap<>();
    private final Map<String, String> lastErrors = new ConcurrentHashMap<>();

    public FfmpegDroneStreamGateway(DroneStreamingProperties properties) {
        this.properties = properties;
        this.outputRoot = Path.of(properties.outputPath()).toAbsolutePath().normalize();
    }

    @Override
    public synchronized DroneStreamStatus start(String streamKey) {
        String safeKey = validateStreamKey(streamKey);
        if (!properties.isEnabled()) {
            return disabledStatus(safeKey);
        }

        RunningStream current = runningStreams.get(safeKey);
        if (current != null && current.process().isAlive() && isManifestReady(current.playlistPath())) {
            return toStatus(safeKey, current, null);
        }

        stopInternal(safeKey);
        Path streamDirectory = resolveStreamDirectory(safeKey);
        Path playlistPath = streamDirectory.resolve(PLAYLIST_FILE_NAME);
        Path logPath = streamDirectory.resolve("ffmpeg.log");
        String sourceUrl = properties.resolveSourceUrl(safeKey);

        try {
            recreateDirectory(streamDirectory);
            ProcessBuilder processBuilder = new ProcessBuilder(buildCommand(sourceUrl, streamDirectory, playlistPath));
            processBuilder.redirectErrorStream(true);
            processBuilder.redirectOutput(logPath.toFile());

            Process process = processBuilder.start();
            RunningStream runningStream = new RunningStream(
                    process,
                    playlistPath,
                    logPath,
                    sourceType(sourceUrl),
                    LocalDateTime.now()
            );
            runningStreams.put(safeKey, runningStream);
            lastErrors.remove(safeKey);
            process.onExit().thenRun(() -> removeExitedProcess(safeKey, runningStream));

            waitForPlaylist(safeKey, runningStream);
            log.info(
                    "Drone HLS stream started: streamKey={}, pid={}, playlist={}",
                    safeKey,
                    process.pid(),
                    playlistPath
            );
            return toStatus(safeKey, runningStream, null);
        } catch (IOException e) {
            stopInternal(safeKey);
            lastErrors.put(safeKey, "FFmpeg 프로세스를 실행하지 못했습니다.");
            log.error("Unable to start FFmpeg. executable={}, streamKey={}", properties.ffmpegExecutable(), safeKey, e);
            throw new BusinessException(ErrorCode.FFMPEG_NOT_AVAILABLE, e);
        } catch (RuntimeException e) {
            stopInternal(safeKey);
            if (e instanceof BusinessException businessException) {
                throw businessException;
            }
            lastErrors.put(safeKey, "드론 영상 스트림을 시작하지 못했습니다.");
            log.error("Unable to start drone stream: streamKey={}", safeKey, e);
            throw new BusinessException(ErrorCode.DRONE_STREAM_START_FAILED, e);
        }
    }

    @Override
    public synchronized DroneStreamStatus stop(String streamKey) {
        String safeKey = validateStreamKey(streamKey);
        stopInternal(safeKey);
        lastErrors.remove(safeKey);
        return status(safeKey);
    }

    @Override
    public DroneStreamStatus status(String streamKey) {
        String safeKey = validateStreamKey(streamKey);
        if (!properties.isEnabled()) {
            return disabledStatus(safeKey);
        }
        RunningStream runningStream = runningStreams.get(safeKey);
        return toStatus(safeKey, runningStream, lastErrors.get(safeKey));
    }

    @Override
    public String playlistUrl(String streamKey) {
        return properties.playlistUrl(validateStreamKey(streamKey));
    }

    @PreDestroy
    public synchronized void stopAll() {
        List.copyOf(runningStreams.keySet()).forEach(this::stopInternal);
    }

    private List<String> buildCommand(String sourceUrl, Path streamDirectory, Path playlistPath) {
        List<String> command = new ArrayList<>();
        command.add(properties.ffmpegExecutable());
        command.add("-hide_banner");
        command.add("-loglevel");
        command.add("warning");
        command.add("-y");

        if (sourceUrl.startsWith("lavfi:")) {
            command.add("-re");
            command.add("-f");
            command.add("lavfi");
            command.add("-i");
            command.add(sourceUrl.substring("lavfi:".length()));
        } else {
            if (sourceUrl.startsWith("rtsp://") || sourceUrl.startsWith("rtsps://")) {
                command.add("-rtsp_transport");
                command.add("tcp");
                command.add("-rw_timeout");
                command.add("10000000");
                command.add("-fflags");
                command.add("nobuffer");
            } else {
                command.add("-re");
                command.add("-stream_loop");
                command.add("-1");
            }
            command.add("-i");
            command.add(sourceUrl);
        }

        int width = properties.targetWidth();
        int height = properties.targetHeight();
        int frameRate = properties.targetFrameRate();
        String videoFilter = "scale=" + width + ":" + height
                + ":force_original_aspect_ratio=decrease,pad=" + width + ":" + height
                + ":(ow-iw)/2:(oh-ih)/2";

        command.add("-map");
        command.add("0:v:0");
        command.add("-an");
        command.add("-vf");
        command.add(videoFilter);
        command.add("-c:v");
        command.add("libx264");
        command.add("-preset");
        command.add("veryfast");
        command.add("-tune");
        command.add("zerolatency");
        command.add("-profile:v");
        command.add("main");
        command.add("-pix_fmt");
        command.add("yuv420p");
        command.add("-r");
        command.add(String.valueOf(frameRate));
        command.add("-g");
        command.add(String.valueOf(frameRate));
        command.add("-keyint_min");
        command.add(String.valueOf(frameRate));
        command.add("-sc_threshold");
        command.add("0");
        command.add("-f");
        command.add("hls");
        command.add("-hls_time");
        command.add(String.valueOf(properties.hlsSegmentSeconds()));
        command.add("-hls_list_size");
        command.add(String.valueOf(properties.hlsPlaylistSize()));
        command.add("-hls_flags");
        command.add("delete_segments+independent_segments+omit_endlist+program_date_time");
        command.add("-hls_segment_filename");
        command.add(streamDirectory.resolve("segment_%06d.ts").toString());
        command.add(playlistPath.toString());
        return List.copyOf(command);
    }

    private void waitForPlaylist(String streamKey, RunningStream runningStream) {
        long deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(properties.startupTimeout());
        while (System.nanoTime() < deadline) {
            if (isManifestReady(runningStream.playlistPath())) {
                return;
            }
            if (!runningStream.process().isAlive()) {
                lastErrors.put(streamKey, "FFmpeg가 HLS manifest 생성 전에 종료되었습니다.");
                logFfmpegFailure(streamKey, runningStream);
                throw new BusinessException(ErrorCode.DRONE_STREAM_START_FAILED);
            }
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new BusinessException(ErrorCode.DRONE_STREAM_START_FAILED, e);
            }
        }

        lastErrors.put(streamKey, "HLS manifest 생성 시간이 초과되었습니다.");
        logFfmpegFailure(streamKey, runningStream);
        throw new BusinessException(ErrorCode.DRONE_STREAM_START_TIMEOUT);
    }

    private boolean isManifestReady(Path playlistPath) {
        try {
            return Files.isRegularFile(playlistPath)
                    && Files.size(playlistPath) > 0
                    && Files.readString(playlistPath).contains("#EXTM3U");
        } catch (IOException e) {
            return false;
        }
    }

    private void stopInternal(String streamKey) {
        RunningStream runningStream = runningStreams.remove(streamKey);
        if (runningStream == null || !runningStream.process().isAlive()) {
            return;
        }

        runningStream.process().destroy();
        try {
            if (!runningStream.process().waitFor(3, TimeUnit.SECONDS)) {
                runningStream.process().destroyForcibly();
                runningStream.process().waitFor(3, TimeUnit.SECONDS);
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            runningStream.process().destroyForcibly();
        }
        log.info("Drone HLS stream stopped: streamKey={}", streamKey);
    }

    private void removeExitedProcess(String streamKey, RunningStream exitedStream) {
        boolean unexpectedExit = runningStreams.remove(streamKey, exitedStream);
        if (unexpectedExit && exitedStream.process().exitValue() != 0) {
            lastErrors.put(streamKey, "FFmpeg 프로세스가 비정상 종료되었습니다.");
            logFfmpegFailure(streamKey, exitedStream);
        }
    }

    private void logFfmpegFailure(String streamKey, RunningStream runningStream) {
        try {
            List<String> lines = Files.exists(runningStream.logPath())
                    ? Files.readAllLines(runningStream.logPath())
                    : List.of();
            int fromIndex = Math.max(0, lines.size() - 30);
            log.error(
                    "FFmpeg stream failure: streamKey={}, log={}\n{}",
                    streamKey,
                    runningStream.logPath(),
                    String.join(System.lineSeparator(), lines.subList(fromIndex, lines.size()))
            );
        } catch (IOException e) {
            log.error("Unable to read FFmpeg log: streamKey={}, log={}", streamKey, runningStream.logPath(), e);
        }
    }

    private DroneStreamStatus toStatus(String streamKey, RunningStream runningStream, String error) {
        boolean running = runningStream != null && runningStream.process().isAlive();
        boolean manifestReady = runningStream != null && isManifestReady(runningStream.playlistPath());
        return new DroneStreamStatus(
                streamKey,
                properties.isEnabled(),
                running,
                manifestReady,
                properties.playlistUrl(streamKey),
                running ? runningStream.process().pid() : null,
                runningStream == null ? null : runningStream.sourceType(),
                runningStream == null ? null : runningStream.startedAt(),
                error
        );
    }

    private DroneStreamStatus disabledStatus(String streamKey) {
        return new DroneStreamStatus(
                streamKey,
                false,
                false,
                false,
                properties.playlistUrl(streamKey),
                null,
                null,
                null,
                "드론 스트리밍이 비활성화되어 있습니다."
        );
    }

    private Path resolveStreamDirectory(String streamKey) {
        Path streamDirectory = outputRoot.resolve(streamKey).normalize();
        if (!streamDirectory.startsWith(outputRoot)) {
            throw new BusinessException(ErrorCode.INVALID_DRONE_STREAM_KEY);
        }
        return streamDirectory;
    }

    private void recreateDirectory(Path streamDirectory) throws IOException {
        Files.createDirectories(outputRoot);
        if (Files.exists(streamDirectory)) {
            try (var paths = Files.walk(streamDirectory)) {
                paths.sorted(Comparator.reverseOrder()).forEach(path -> {
                    try {
                        Files.delete(path);
                    } catch (IOException e) {
                        throw new StreamFileException(e);
                    }
                });
            } catch (StreamFileException e) {
                throw e.getCause();
            }
        }
        Files.createDirectories(streamDirectory);
    }

    private String validateStreamKey(String streamKey) {
        if (streamKey == null || !SAFE_STREAM_KEY.matcher(streamKey).matches()) {
            throw new BusinessException(ErrorCode.INVALID_DRONE_STREAM_KEY);
        }
        return streamKey;
    }

    private String sourceType(String sourceUrl) {
        if (sourceUrl.startsWith("lavfi:")) {
            return "LAVFI";
        }
        if (sourceUrl.startsWith("rtsp://") || sourceUrl.startsWith("rtsps://")) {
            return "RTSP";
        }
        return "FILE";
    }

    private record RunningStream(
            Process process,
            Path playlistPath,
            Path logPath,
            String sourceType,
            LocalDateTime startedAt
    ) {
    }

    private static final class StreamFileException extends RuntimeException {
        private StreamFileException(IOException cause) {
            super(cause);
        }

        @Override
        public synchronized IOException getCause() {
            return (IOException) super.getCause();
        }
    }
}
