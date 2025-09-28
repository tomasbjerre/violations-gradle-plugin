package se.bjurr.violations.gradle.plugin;

import static java.nio.file.StandardOpenOption.CREATE;
import static java.nio.file.StandardOpenOption.TRUNCATE_EXISTING;
import static java.nio.file.StandardOpenOption.WRITE;
import static se.bjurr.violations.git.ViolationsReporterApi.violationsReporterApi;
import static se.bjurr.violations.git.ViolationsReporterDetailLevel.VERBOSE;
import static se.bjurr.violations.lib.ViolationsApi.violationsApi;
import static se.bjurr.violations.lib.model.SEVERITY.INFO;
import static se.bjurr.violations.lib.model.codeclimate.CodeClimateTransformer.fromViolations;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.logging.Level;
import java.util.stream.Collectors;
import javax.script.ScriptException;
import org.gradle.api.DefaultTask;
import org.gradle.api.logging.LogLevel;
import org.gradle.api.provider.ListProperty;
import org.gradle.api.provider.Property;
import org.gradle.api.tasks.TaskAction;
import se.bjurr.violations.git.ViolationsGit;
import se.bjurr.violations.git.ViolationsReporterDetailLevel;
import se.bjurr.violations.lib.ViolationsLogger;
import se.bjurr.violations.lib.model.SEVERITY;
import se.bjurr.violations.lib.model.Violation;
import se.bjurr.violations.lib.reports.Parser;
import se.bjurr.violations.lib.util.Filtering;
import se.bjurr.violations.violationslib.com.google.gson.GsonBuilder;

public class ViolationsTask extends DefaultTask {

  public ListProperty<ViolationConfig> violations =
      this.getProject()
          .getObjects()
          .listProperty(ViolationConfig.class)
          .convention(new ArrayList<>());
  public Property<SEVERITY> minSeverity =
      this.getProject().getObjects().property(SEVERITY.class).convention(INFO);
  public Property<ViolationsReporterDetailLevel> detailLevel =
      this.getProject()
          .getObjects()
          .property(ViolationsReporterDetailLevel.class)
          .convention(VERBOSE);
  public Property<Integer> maxViolations =
      this.getProject().getObjects().property(Integer.class).convention(Integer.MAX_VALUE);
  public Property<Boolean> printViolations =
      this.getProject().getObjects().property(Boolean.class).convention(false);
  public Property<String> diffFrom =
      this.getProject().getObjects().property(String.class).convention("");
  public Property<String> diffTo =
      this.getProject().getObjects().property(String.class).convention("");
  public Property<SEVERITY> diffMinSeverity =
      this.getProject().getObjects().property(SEVERITY.class).convention(INFO);
  public Property<File> gitRepo =
      this.getProject().getObjects().property(File.class).convention(new File("."));
  public Property<Boolean> diffPrintViolations =
      this.getProject().getObjects().property(Boolean.class).convention(false);
  public Property<Integer> diffMaxViolations =
      this.getProject().getObjects().property(Integer.class).convention(Integer.MAX_VALUE);
  public Property<ViolationsReporterDetailLevel> diffDetailLevel =
      this.getProject()
          .getObjects()
          .property(ViolationsReporterDetailLevel.class)
          .convention(VERBOSE);
  public Property<Integer> maxReporterColumnWidth =
      this.getProject().getObjects().property(Integer.class).convention(0);
  public Property<Integer> maxRuleColumnWidth =
      this.getProject().getObjects().property(Integer.class).convention(0);
  public Property<Integer> maxSeverityColumnWidth =
      this.getProject().getObjects().property(Integer.class).convention(0);
  public Property<Integer> maxLineColumnWidth =
      this.getProject().getObjects().property(Integer.class).convention(0);
  public Property<Integer> maxMessageColumnWidth =
      this.getProject().getObjects().property(Integer.class).convention(30);
  public Property<File> codeClimateFile = this.getProject().getObjects().property(File.class);
  public Property<File> violationsFile = this.getProject().getObjects().property(File.class);
  public Property<ViolationsLogger> violationsLogger =
      this.getProject()
          .getObjects()
          .property(ViolationsLogger.class)
          .convention(
              new ViolationsLogger() {
                private LogLevel toGradleLogLevel(final Level level) {
                  LogLevel gradleLevel = LogLevel.INFO;
                  if (level == Level.FINE) {
                    gradleLevel = LogLevel.DEBUG;
                  } else if (level == Level.SEVERE) {
                    gradleLevel = LogLevel.ERROR;
                  } else if (level == Level.WARNING) {
                    gradleLevel = LogLevel.WARN;
                  }
                  return gradleLevel;
                }

                @Override
                public void log(final Level level, final String string) {
                  ViolationsTask.this.getLogger().log(this.toGradleLogLevel(level), string);
                }

                @Override
                public void log(final Level level, final String string, final Throwable t) {
                  ViolationsTask.this.getLogger().log(this.toGradleLogLevel(level), string, t);
                }
              });

  /**
   * Added to make it backwards compatible. Better to use the setter with {@link ViolationConfig} to
   * make it more type safe.
   */
  public void setViolations(final List<List<String>> violations) {
    final List<ViolationConfig> violationConfigs =
        violations.stream()
            .map(
                configuredViolation -> {
                  final String reporter =
                      configuredViolation.size() >= 4 ? configuredViolation.get(3) : null;

                  final String scanFolder = configuredViolation.get(1);
                  final Parser parser = Parser.valueOf(configuredViolation.get(0));
                  final String pattern = configuredViolation.get(2);

                  return new ViolationConfig()
                      .setFolder(scanFolder)
                      .setParser(parser)
                      .setPattern(pattern)
                      .setReporter(reporter);
                })
            .collect(Collectors.toList());

    this.violations.set(violationConfigs);
  }

  public ViolationConfig violationConfig() {
    final ViolationConfig vc = new ViolationConfig();
    this.violations.add(vc);
    return vc;
  }

  @TaskAction
  public void violationsPluginTasks() throws Exception {
    final Set<Violation> allParsedViolations = new TreeSet<>();
    final Set<Violation> allParsedViolationsInDiff = new TreeSet<>();
    for (final ViolationConfig configuredViolation : this.violations.get()) {
      final Set<Violation> parsedViolations = this.getAllParsedViolations(configuredViolation);

      allParsedViolations.addAll(this.getFiltered(parsedViolations, this.minSeverity.get()));

      if (this.shouldCheckDiff()) {
        allParsedViolationsInDiff.addAll(this.getAllViolationsInDiff(parsedViolations));
      } else {
        this.getLogger().info("No references specified, will not report violations in diff");
      }
    }

    if (this.codeClimateFile.isPresent()) {
      this.createJsonFile(fromViolations(allParsedViolations), this.codeClimateFile.get());
    }
    if (this.violationsFile.isPresent()) {
      this.createJsonFile(allParsedViolations, this.violationsFile.get());
    }
    this.checkGlobalViolations(allParsedViolations);

    if (this.shouldCheckDiff()) {
      this.checkDiffViolations(allParsedViolationsInDiff);
    }
  }

  private void createJsonFile(final Object object, final File file) throws IOException {
    final String codeClimateReport = new GsonBuilder().setPrettyPrinting().create().toJson(object);
    final Path path = file.toPath();
    path.toFile().getParentFile().mkdirs();
    Files.write(
        path, codeClimateReport.getBytes(StandardCharsets.UTF_8), TRUNCATE_EXISTING, CREATE, WRITE);
  }

  private void checkGlobalViolations(final Set<Violation> violations) throws ScriptException {
    final boolean tooManyViolations = violations.size() > this.maxViolations.get();
    if (!tooManyViolations && !this.printViolations.get()) {
      return;
    }

    final String report =
        violationsReporterApi() //
            .withViolations(violations) //
            .withMaxLineColumnWidth(this.maxLineColumnWidth.get()) //
            .withMaxMessageColumnWidth(this.maxMessageColumnWidth.get()) //
            .withMaxReporterColumnWidth(this.maxReporterColumnWidth.get()) //
            .withMaxRuleColumnWidth(this.maxRuleColumnWidth.get()) //
            .withMaxSeverityColumnWidth(this.maxSeverityColumnWidth.get()) //
            .getReport(this.detailLevel.get());

    if (tooManyViolations) {
      this.getLogger().error("\nViolations:\n\n" + report);
      throw new ScriptException(
          "Too many violations found, max is "
              + this.maxViolations.get()
              + " but found "
              + violations.size()
              + ". You can adjust this with the 'maxViolations' configuration parameter.");
    } else {
      if (this.printViolations.get()) {
        this.getLogger().lifecycle("\nViolations in repo\n\n" + report);
      }
    }
  }

  private void checkDiffViolations(final Set<Violation> violations) throws ScriptException {
    final boolean tooManyViolations = violations.size() > this.diffMaxViolations.get();
    if (!tooManyViolations && !this.diffPrintViolations.get()) {
      return;
    }

    final String report =
        violationsReporterApi() //
            .withViolations(violations) //
            .withMaxLineColumnWidth(this.maxLineColumnWidth.get()) //
            .withMaxMessageColumnWidth(this.maxMessageColumnWidth.get()) //
            .withMaxReporterColumnWidth(this.maxReporterColumnWidth.get()) //
            .withMaxRuleColumnWidth(this.maxRuleColumnWidth.get()) //
            .withMaxSeverityColumnWidth(this.maxSeverityColumnWidth.get()) //
            .getReport(this.diffDetailLevel.get());

    if (tooManyViolations) {
      this.getLogger().error("\nViolations:\n\n" + report);
      throw new ScriptException(
          "Too many violations found in diff, max is "
              + this.diffMaxViolations
              + " but found "
              + violations.size()
              + ". You can adjust this with the 'maxViolations' configuration parameter.");
    } else {
      if (this.diffPrintViolations.get()) {
        this.getLogger().lifecycle("\nViolations in diff\n\n" + report);
      }
    }
  }

  private Set<Violation> getAllViolationsInDiff(final Set<Violation> unfilteredViolations)
      throws Exception {
    final Set<Violation> candidates =
        this.getFiltered(unfilteredViolations, this.diffMinSeverity.get());
    return new ViolationsGit(this.violationsLogger.get(), candidates) //
        .getViolationsInChangeset(this.gitRepo.get(), this.diffFrom.get(), this.diffTo.get());
  }

  private Set<Violation> getFiltered(final Set<Violation> unfiltered, final SEVERITY filter) {
    if (filter != null) {
      return Filtering.withAtLEastSeverity(unfiltered, filter);
    }
    return unfiltered;
  }

  private boolean shouldCheckDiff() {
    return this.isDefined(this.diffFrom.get()) && this.isDefined(this.diffTo.get());
  }

  private Set<Violation> getAllParsedViolations(final ViolationConfig configuredViolation) {
    final Set<Violation> parsedViolations =
        violationsApi() //
            .withViolationsLogger(this.violationsLogger.get()) //
            .findAll(configuredViolation.getParser()) //
            .inFolder(configuredViolation.getFolder()) //
            .withPattern(configuredViolation.getPattern()) //
            .withReporter(configuredViolation.getReporter()) //
            .violations();
    return parsedViolations;
  }

  private boolean isDefined(final String str) {
    return str != null && !str.isEmpty();
  }
}
