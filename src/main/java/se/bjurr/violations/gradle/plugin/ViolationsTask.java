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

  private List<List<String>> violations = new ArrayList<>();
  private SEVERITY minSeverity = INFO;
  private ViolationsReporterDetailLevel detailLevel = VERBOSE;
  private Integer maxViolations = Integer.MAX_VALUE;
  private boolean printViolations;
  private String diffFrom;
  private String diffTo;
  private SEVERITY diffMinSeverity = INFO;
  private File gitRepo = new File(".");
  private boolean diffPrintViolations;
  private Integer diffMaxViolations = Integer.MAX_VALUE;
  private ViolationsReporterDetailLevel diffDetailLevel = VERBOSE;
  private int maxReporterColumnWidth;
  private int maxRuleColumnWidth;
  private int maxSeverityColumnWidth;
  private int maxLineColumnWidth;
  private int maxMessageColumnWidth = 30;
  private File codeClimateFile;
  private File violationsFile;
  private ViolationsLogger violationsLogger;

  public void setCodeClimateFile(final File codeClimateFile) {
    this.codeClimateFile = codeClimateFile;
  }

  public void setViolationsFile(final File violationsFile) {
    this.violationsFile = violationsFile;
  }

  public void setMinSeverity(final SEVERITY minSeverity) {
    this.minSeverity = minSeverity;
  }

  public void setViolations(final List<List<String>> violations) {
    this.violations = violations;
  }

  public void setDetailLevel(final ViolationsReporterDetailLevel detailLevel) {
    this.detailLevel = detailLevel;
  }

  public void setMaxViolations(final Integer maxViolations) {
    this.maxViolations = maxViolations;
  }

  public void setDiffDetailLevel(final ViolationsReporterDetailLevel diffDetailLevel) {
    this.diffDetailLevel = diffDetailLevel;
  }

  public void setDiffFrom(final String diffFrom) {
    this.diffFrom = diffFrom;
  }

  public void setDiffMaxViolations(final Integer diffMaxViolations) {
    this.diffMaxViolations = diffMaxViolations;
  }

  public void setDiffMinSeverity(final SEVERITY diffMinSeverity) {
    this.diffMinSeverity = diffMinSeverity;
  }

  public void setDiffTo(final String diffTo) {
    this.diffTo = diffTo;
  }

  public void setGitRepo(final File gitRepo) {
    this.gitRepo = gitRepo;
  }

  public void setDiffPrintViolations(final boolean diffPrintViolations) {
    this.diffPrintViolations = diffPrintViolations;
  }

  public void setPrintViolations(final boolean printViolations) {
    this.printViolations = printViolations;
  }

  public void setMaxLineColumnWidth(final int maxLineColumnWidth) {
    this.maxLineColumnWidth = maxLineColumnWidth;
  }

  public void setMaxMessageColumnWidth(final int maxMessageColumnWidth) {
    this.maxMessageColumnWidth = maxMessageColumnWidth;
  }

  public void setMaxReporterColumnWidth(final int maxReporterColumnWidth) {
    this.maxReporterColumnWidth = maxReporterColumnWidth;
  }

  public void setMaxRuleColumnWidth(final int maxRuleColumnWidth) {
    this.maxRuleColumnWidth = maxRuleColumnWidth;
  }

  public void setMaxSeverityColumnWidth(final int maxSeverityColumnWidth) {
    this.maxSeverityColumnWidth = maxSeverityColumnWidth;
  }

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
    return new ViolationsGit(candidates) //
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
