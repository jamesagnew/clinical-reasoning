package org.opencds.cqf.fhir.benchmark;

import static org.opencds.cqf.fhir.utility.r4.Parameters.parameters;
import static org.opencds.cqf.fhir.utility.r4.Parameters.stringPart;

import ca.uhn.fhir.context.FhirContext;
import java.util.Collection;
import java.util.concurrent.TimeUnit;
import org.hl7.fhir.r4.model.IdType;
import org.opencds.cqf.fhir.api.Repository;
import org.opencds.cqf.fhir.cr.questionnaire.r4.QuestionnaireProcessorTests;
import org.opencds.cqf.fhir.cr.questionnaire.r4.helpers.TestQuestionnaire;
import org.opencds.cqf.fhir.cr.questionnaire.r4.helpers.TestQuestionnaire.QuestionnaireResult;
import org.opencds.cqf.fhir.test.TestRepositoryFactory;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Level;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.infra.Blackhole;
import org.openjdk.jmh.results.RunResult;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

@State(Scope.Benchmark)
public class Questionnaires {
    private static final FhirContext FHIR_CONTEXT = FhirContext.forR4Cached();
    private static final Repository REPOSITORY = TestRepositoryFactory.createRepository(
            FHIR_CONTEXT, QuestionnaireProcessorTests.class, TestQuestionnaire.CLASS_PATH + "/pa-aslp");

    private QuestionnaireResult result;

    @Setup(Level.Trial)
    public void setupTrial() throws Exception {
        this.result = TestQuestionnaire.Assert.that(new IdType("Questionnaire", "ASLPA1"), "positive")
                .withRepository(REPOSITORY)
                .withParameters(parameters(
                        stringPart("Service Request Id", "SleepStudy"),
                        stringPart("Service Request Id", "SleepStudy2"),
                        stringPart("Coverage Id", "Coverage-positive")));
    }

    @Benchmark
    @Fork(warmups = 1, value = 1)
    @Measurement(iterations = 10, timeUnit = TimeUnit.SECONDS)
    @OutputTimeUnit(TimeUnit.SECONDS)
    public void test(Blackhole bh) throws Exception {
        // The Blackhole ensures that the compiler doesn't optimize
        // away this call, which does nothing with the result of the evaluation
        bh.consume(this.result.populate());
    }

    @SuppressWarnings("unused")
    public static void main(String[] args) throws RunnerException {
        Options opt = new OptionsBuilder()
                .include(Questionnaires.class.getSimpleName())
                .build();
        Collection<RunResult> runResults = new Runner(opt).run();
    }
}
