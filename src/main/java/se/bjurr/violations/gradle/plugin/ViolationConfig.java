package se.bjurr.violations.gradle.plugin;

import java.util.Objects;
import se.bjurr.violations.lib.reports.Parser;

public class ViolationConfig {
  private String reporter;
  private Parser parser;
  private String folder;
  private String pattern;

  public ViolationConfig() {}

  public String getReporter() {
    return this.reporter;
  }

  public ViolationConfig setReporter(final String reporter) {
    this.reporter = reporter;
    return this;
  }

  public Parser getParser() {
    return this.parser;
  }

  public ViolationConfig setParser(final Parser parser) {
    this.parser = parser;
    return this;
  }

  public String getFolder() {
    return this.folder;
  }

  public ViolationConfig setFolder(final String folder) {
    this.folder = folder;
    return this;
  }

  public String getPattern() {
    return this.pattern;
  }

  public ViolationConfig setPattern(final String pattern) {
    this.pattern = pattern;
    return this;
  }

  @Override
  public int hashCode() {
    return Objects.hash(this.folder, this.parser, this.pattern, this.reporter);
  }

  @Override
  public boolean equals(final Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null) {
      return false;
    }
    if (this.getClass() != obj.getClass()) {
      return false;
    }
    final ViolationConfig other = (ViolationConfig) obj;
    return Objects.equals(this.folder, other.folder)
        && this.parser == other.parser
        && Objects.equals(this.pattern, other.pattern)
        && Objects.equals(this.reporter, other.reporter);
  }

  @Override
  public String toString() {
    return "ViolationConfig [reporter="
        + this.reporter
        + ", parser="
        + this.parser
        + ", folder="
        + this.folder
        + ", pattern="
        + this.pattern
        + "]";
  }
}
