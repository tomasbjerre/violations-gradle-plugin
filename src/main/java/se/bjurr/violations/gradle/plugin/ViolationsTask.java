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
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.logging.Level;
import javax.script.ScriptException;
import org.gradle.api.DefaultTask;
import org.gradle.api.logging.LogLevel;
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

  public List<List<String>> violations = new ArrayList<>();
  public SEVERITY minSeverity = INFO;
  public ViolationsReporterDetailLevel detailLevel = VERBOSE;
  public Integer maxViolations = Integer.MAX_VALUE;
  public boolean printViolations;
  public String diffFrom;
  public String diffTo;
  public SEVERITY diffMinSeverity = INFO;
  public File gitRepo = new File(".");
  public boolean diffPrintViolations;
  public Integer diffMaxViolations = Integer.MAX_VALUE;
  public ViolationsReporterDetailLevel diffDetailLevel = VERBOSE;
  public int maxReporterColumnWidth;
  public int maxRuleColumnWidth;
  public int maxSeverityColumnWidth;
  public int maxLineColumnWidth;
  public int maxMessageColumnWidth = 30;
  public File codeClimateFile;
  public File violationsFile;
  public File diffViolationsFile;
  public ViolationsLogger violationsLogger;

  @TaskAction
  public void violationsPluginTasks() throws Exception {
    this.violationsLogger =
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
        };

    final Set<Violation> allParsedViolations = new TreeSet<>();
    final Set<Violation> allParsedViolationsInDiff = new TreeSet<>();
    for (final List<String> configuredViolation : this.violations) {
      final Set<Violation> parsedViolations = this.getAllParsedViolations(configuredViolation);

      allParsedViolations.addAll(this.getFiltered(parsedViolations, this.minSeverity));

      if (this.shouldCheckDiff()) {
        allParsedViolationsInDiff.addAll(this.getAllViolationsInDiff(parsedViolations));
      } else {
        this.getLogger().info("No references specified, will not report violations in diff");
      }
    }

    if (this.codeClimateFile != null) {
      this.createJsonFile(fromViolations(allParsedViolations), this.codeClimateFile);
    }
    if (this.violationsFile != null) {
      this.createJsonFile(allParsedViolations, this.violationsFile);
    }
    if (this.shouldCheckDiff() && this.diffViolationsFile != null) {
      this.createJsonFile(allParsedViolationsInDiff, this.diffViolationsFile);
    }
    this.checkGlobalViolations(allParsedViolations);

    if (this.shouldCheckDiff()) {
      this.checkDiffViolations(allParsedViolationsInDiff);
    }
  }

  private void createJsonFile(final Object object, final File file) throws IOException {
    final String codeClimateReport = new GsonBuilder().setPrettyPrinting().create().toJson(object);
    Files.write(
        file.toPath(),
        codeClimateReport.getBytes(StandardCharsets.UTF_8),
        TRUNCATE_EXISTING,
        CREATE,
        WRITE);
  }

  private void checkGlobalViolations(final Set<Violation> violations) throws ScriptException {
    final boolean tooManyViolations = violations.size() > this.maxViolations;
    if (!tooManyViolations && !this.printViolations) {
      return;
    }

    final String report =
        violationsReporterApi() //
            .withViolations(violations) //
            .withMaxLineColumnWidth(this.maxLineColumnWidth) //
            .withMaxMessageColumnWidth(this.maxMessageColumnWidth) //
            .withMaxReporterColumnWidth(this.maxReporterColumnWidth) //
            .withMaxRuleColumnWidth(this.maxRuleColumnWidth) //
            .withMaxSeverityColumnWidth(this.maxSeverityColumnWidth) //
            .getReport(this.detailLevel);

    if (tooManyViolations) {
      this.getLogger().error("\nViolations:\n\n" + report);
      throw new ScriptException(
          "Too many violations found, max is "
              + this.maxViolations
              + " but found "
              + violations.size()
              + ". You can adjust this with the 'maxViolations' configuration parameter.");
    } else {
      if (this.printViolations) {
        this.getLogger().lifecycle("\nViolations in repo\n\n" + report);
      }
    }
  }

  private void checkDiffViolations(final Set<Violation> violations) throws ScriptException {
    final boolean tooManyViolations = violations.size() > this.diffMaxViolations;
    if (!tooManyViolations && !this.diffPrintViolations) {
      return;
    }

    final String report =
        violationsReporterApi() //
            .withViolations(violations) //
            .withMaxLineColumnWidth(this.maxLineColumnWidth) //
            .withMaxMessageColumnWidth(this.maxMessageColumnWidth) //
            .withMaxReporterColumnWidth(this.maxReporterColumnWidth) //
            .withMaxRuleColumnWidth(this.maxRuleColumnWidth) //
            .withMaxSeverityColumnWidth(this.maxSeverityColumnWidth) //
            .getReport(this.diffDetailLevel);

    if (tooManyViolations) {
      this.getLogger().error("\nViolations:\n\n" + report);
      throw new ScriptException(
          "Too many violations found in diff, max is "
              + this.diffMaxViolations
              + " but found "
              + violations.size()
              + ". You can adjust this with the 'maxViolations' configuration parameter.");
    } else {
      if (this.diffPrintViolations) {
        this.getLogger().lifecycle("\nViolations in diff\n\n" + report);
      }
    }
  }

  private Set<Violation> getAllViolationsInDiff(final Set<Violation> unfilteredViolations)
      throws Exception {
    final Set<Violation> candidates = this.getFiltered(unfilteredViolations, this.diffMinSeverity);
    return new ViolationsGit(this.violationsLogger, candidates) //
        .getViolationsInChangeset(this.gitRepo, this.diffFrom, this.diffTo);
  }

  private Set<Violation> getFiltered(final Set<Violation> unfiltered, final SEVERITY filter) {
    if (filter != null) {
      return Filtering.withAtLEastSeverity(unfiltered, filter);
    }
    return unfiltered;
  }

  private boolean shouldCheckDiff() {
    return this.isDefined(this.diffFrom) && this.isDefined(this.diffTo);
  }

  private Set<Violation> getAllParsedViolations(final List<String> configuredViolation) {
    final String reporter = configuredViolation.size() >= 4 ? configuredViolation.get(3) : null;

    final String scanFolder = configuredViolation.get(1);
    final Parser parser = Parser.valueOf(configuredViolation.get(0));
    final String pattern = configuredViolation.get(2);
    final Set<Violation> parsedViolations =
        violationsApi() //
            .withViolationsLogger(this.violationsLogger) //
            .findAll(parser) //
            .inFolder(scanFolder) //
            .withPattern(pattern) //
            .withReporter(reporter) //
            .violations();
    return parsedViolations;
  }

  private boolean isDefined(final String str) {
    return str != null && !str.isEmpty();
  }
}
