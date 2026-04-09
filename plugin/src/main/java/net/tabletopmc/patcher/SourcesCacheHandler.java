package net.tabletopmc.patcher;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import org.gradle.api.file.DirectoryProperty;
import org.gradle.api.model.ObjectFactory;
import org.gradle.api.provider.Property;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.Map;
import java.util.stream.Collectors;

class SourcesCacheHandler {
  private static final Gson GSON = new Gson();

  private final Property<String> name;
  private final DirectoryProperty buildDir;

  public SourcesCacheHandler(ObjectFactory objects) {
    this.name = objects.property(String.class);
    this.buildDir = objects.directoryProperty();
  }

  public Property<String> getName() {
    return this.name;
  }

  public DirectoryProperty getBuildDir() {
    return this.buildDir;
  }

  public SourcesCache load() throws IOException {
    final Path cacheFile = getCacheFile();
    if (!Files.exists(cacheFile)) {
      return SourcesCache.EMPTY;
    }

    final String data = Files.readString(cacheFile);
    final JsonObject obj = GSON.fromJson(data, JsonObject.class);

    final Map<Path, Instant> sources = obj.getAsJsonObject("sources").asMap().entrySet().stream()
      .collect(Collectors.toMap(e -> Path.of(e.getKey()), e -> GSON.fromJson(e.getValue(), Instant.class)));
    final Map<Path, Instant> targets = obj.getAsJsonObject("targets").asMap().entrySet().stream()
      .collect(Collectors.toMap(e -> Path.of(e.getKey()), e -> GSON.fromJson(e.getValue(), Instant.class)));

    return new SourcesCache(sources, targets);
  }

  public void save(Map<Path, Instant> sourcesAccessMap, Map<Path, Instant> targetAccessMap) throws IOException {
    final Path cacheDir = getCacheDir();
    final Path cacheFile = getCacheFile();

    if (!Files.deleteIfExists(cacheFile)) {
      Files.createDirectories(cacheDir);
    }

    final Map<String, Instant> stringifiedSources = sourcesAccessMap.entrySet().stream()
      .map(entry -> Map.entry(entry.getKey().toString(), entry.getValue()))
      .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    final Map<String, Instant> stringifiedTargets = targetAccessMap.entrySet().stream()
      .map(entry -> Map.entry(entry.getKey().toString(), entry.getValue()))
      .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

    final JsonObject obj = new JsonObject();
    obj.add("sources", GSON.toJsonTree(stringifiedSources));
    obj.add("targets", GSON.toJsonTree(stringifiedTargets));

    final String json = GSON.toJson(obj);
    Files.createFile(cacheFile);
    Files.writeString(cacheFile, json);
  }

  private Path getCacheDir() {
    return getBuildDir().dir("tabletop-patcher/cache/" + name.get()).get().getAsFile().toPath();
  }

  private Path getCacheFile() {
    return getCacheDir().resolve("sources.json");
  }

  static class SourcesCache {
    private static final SourcesCache EMPTY = new SourcesCache(Map.of(), Map.of()) {
      @Override
      protected boolean hasFileChanged(Path path, Path root, Map<Path, Instant> map) {
        return true;
      }
    };

    private final Map<Path, Instant> sourceTimestamps;
    private final Map<Path, Instant> targetTimestamps;

    public SourcesCache(Map<Path, Instant> sourceTimestamps, Map<Path, Instant> targetTimestamps) {
      this.sourceTimestamps = sourceTimestamps;
      this.targetTimestamps = targetTimestamps;
    }

    public boolean hasSourceFileChanged(Path path, Path root) throws IOException {
      return hasFileChanged(path, root, sourceTimestamps);
    }

    public boolean hasTargetFileChanged(Path path, Path root) throws IOException {
      return hasFileChanged(path, root, targetTimestamps);
    }

    protected boolean hasFileChanged(Path path, Path root, Map<Path, Instant> map) throws IOException {
      final Instant lastModified = map.getOrDefault(path, null);
      if (lastModified == null) {
        return true;
      }

      if (!Files.exists(root.resolve(path))) {
        return true;
      }

      return !Files.getLastModifiedTime(root.resolve(path)).toInstant().equals(lastModified);
    }
  }
}
