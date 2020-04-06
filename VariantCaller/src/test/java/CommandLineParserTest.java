import com.epam.bioinf.variantcaller.cmdline.CommandLineParser;

import com.epam.bioinf.variantcaller.cmdline.ParsedArguments;
import org.junit.jupiter.api.Test;
import joptsimple.OptionException;

import static com.epam.bioinf.variantcaller.helpers.TestHelper.testFilePath;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

import static java.io.File.pathSeparatorChar;
import static org.junit.jupiter.api.Assertions.*;

public class CommandLineParserTest {

  @Test
  public void parserMustBeBuiltWithValidParameters() {
    CommandLineParser.parse(getArgs("--fasta", "--bed", "--sam"));
    CommandLineParser.parse(getArgs("--fasta", "--sam"));
    CommandLineParser.parse(getArgs("--fasta", "--sam", "--region"));
  }

  @Test
  public void parserMustBeFailWithExcessParameters() {
    String[] invalidTestArgs = getArgs("--fasta", "--bed", "--sam", "--region");
    assertThrows(IllegalArgumentException.class, () ->
        CommandLineParser.parse(invalidTestArgs)
    );
  }

  @Test
  public void parserMustFailWithInvalidParameters() {
    String[] invalidTestArgs = getArgs("-p", "-ap", "-hp");
    assertThrows(OptionException.class, () ->
        CommandLineParser.parse(invalidTestArgs)
    );
  }

  @Test
  public void parserMustReturnCorrectParsedArgumentsWithValidArguments() {
    String[] correctTestArgs = getArgs("--fasta", "--bed", "--sam");
    ParsedArguments result = CommandLineParser.parse(correctTestArgs);
    assertEquals(result.getFastaPath(), Paths.get(testFilePath("test1.fasta")));
    assertEquals(result.getBedPaths().get(0), Paths.get(testFilePath("test1.bed")));
    assertEquals(result.getSamPaths().get(0), Paths.get(testFilePath("test1.sam")));
  }

  @Test
  public void parserMustReturnCorrectParsedArgumentsIfMultipleArgumentsProvided() {
    String[] correctTestArgs = getMultipleArgs();
    ParsedArguments result = CommandLineParser.parse(correctTestArgs);
    Path expectedFastaPath = Paths.get(testFilePath("test1.fasta"));
    List<Path> expectedBedPaths = List.of(
        Paths.get(testFilePath("test1.bed")),
        Paths.get(testFilePath("test2.bed"))
    );
    List<Path> expectedSamPaths = List.of(
        Paths.get(testFilePath("test1.sam")),
        Paths.get(testFilePath("test2.sam"))
    );

    Path parsedFastaPath = result.getFastaPath();
    List<Path> parsedBedPaths = result.getBedPaths();
    List<Path> parsedSamPaths = result.getSamPaths();

    assertEquals(
        expectedFastaPath,
        parsedFastaPath,
        "Expected fasta path is not equal to parsed one"
    );
    assertEquals(
        Set.copyOf(expectedBedPaths),
        Set.copyOf(parsedBedPaths),
        "Unexpected BED file paths"
    );
    assertEquals(
        Set.copyOf(expectedSamPaths),
        Set.copyOf(parsedSamPaths),
        "Unexpected SAM file paths"
    );
  }

  private String[] getMultipleArgs() {
    return new String[] {
        "--fasta", testFilePath("test1.fasta"),
        "--bed", testFilePath("test1.bed") + pathSeparatorChar + testFilePath("test2.bed"),
        "--sam", testFilePath("test1.sam") + pathSeparatorChar + testFilePath("test2.sam")
    };
  }

  private String[] getArgs(String... keys) {
    List<String> output = new ArrayList<>();
    Arrays.asList(keys).forEach(key -> {
      output.add(key);
      output.add(evaluateKey(key));
    });
    return output.toArray(String[]::new);
  }

  private String evaluateKey(String key) {
    switch (key) {
        case ("--fasta"):
          return testFilePath("test1.fasta");
        case ("--sam"):
          return testFilePath("test1.sam");
        case ("--bed"):
          return testFilePath("test1.bed");
        case ("--region"):
          return "chr1 10 20";
        default:
          return null;
    }
  }
}
