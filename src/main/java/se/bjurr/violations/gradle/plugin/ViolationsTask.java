package se.bjurr.violations.gradle.plugin;

import static se.bjurr.violations.lib.ViolationsApi.violationsApi;
import static se.bjurr.violations.lib.ViolationsReporterApi.violationsReporterApi;
import static se.bjurr.violations.lib.model.SEVERITY.INFO;

import java.util.ArrayList;
import java.util.List;
import javax.script.ScriptException;
import org.gradle.api.DefaultTask;
import org.gradle.api.tasks.TaskAction;
import org.gradle.api.tasks.TaskExecutionException;
import se.bjurr.violations.lib.ViolationsReporterDetailLevel;
import se.bjurr.violations.lib.model.SEVERITY;
import se.bjurr.violations.lib.model.Violation;
import se.bjurr.violations.lib.reports.Parser;
import se.bjurr.violations.lib.util.Filtering;

public class ViolationsTask extends DefaultTask {

  private List<List<String>> violations = new ArrayList<>();
  private SEVERITY minSeverity = INFO;
  private ViolationsReporterDetailLevel detailLevel;
  private Integer maxViolations = Integer.MAX_VALUE;

  public void setMinSeverity(SEVERITY minSeverity) {
    this.minSeverity = minSeverity;
  }

  public void setViolations(List<List<String>> violations) {
    this.violations = violations;
  }

  public void setDetailLevel(ViolationsReporterDetailLevel detailLevel) {
    this.detailLevel = detailLevel;
  }

  public void setMaxViolations(Integer maxViolations) {
    this.maxViolations = maxViolations;
  }

  @TaskAction
  public void gitChangelogPluginTasks() throws TaskExecutionException, ScriptException {
    getLogger().info("");

    List<Violation> allParsedViolations = new ArrayList<>();
    for (final List<String> configuredViolation : violations) {
      final String reporter = configuredViolation.size() >= 4 ? configuredViolation.get(3) : null;

      final List<Violation> parsedViolations =
          violationsApi() //
              .findAll(Parser.valueOf(configuredViolation.get(0))) //
              .inFolder(configuredViolation.get(1)) //
              .withPattern(configuredViolation.get(2)) //
              .withReporter(reporter) //
              .violations();
      if (minSeverity != null) {
        allParsedViolations = Filtering.withAtLEastSeverity(allParsedViolations, minSeverity);
      }
      allParsedViolations.addAll(parsedViolations);
    }

    final String report =
        violationsReporterApi() //
            .withViolations(allParsedViolations) //
            .getReport(detailLevel);

    getLogger().info("\n" + report);

    if (allParsedViolations.size() > maxViolations) {
      throw new ScriptException(
          "To many violations found, max is "
              + maxViolations
              + " but found "
              + allParsedViolations.size());
    }
  }
}
