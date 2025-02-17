package org.opencds.cqf.fhir.cr.questionnaireresponse.dstu3;

import static org.junit.jupiter.api.Assertions.fail;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.context.FhirVersionEnum;
import ca.uhn.fhir.parser.IParser;
import ca.uhn.fhir.rest.api.EncodingEnum;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import org.hl7.fhir.dstu3.model.Bundle;
import org.hl7.fhir.dstu3.model.QuestionnaireResponse;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.json.JSONException;
import org.opencds.cqf.fhir.api.Repository;
import org.opencds.cqf.fhir.cql.EvaluationSettings;
import org.opencds.cqf.fhir.cql.LibraryEngine;
import org.opencds.cqf.fhir.utility.repository.IGFileStructureRepository;
import org.opencds.cqf.fhir.utility.repository.IGLayoutMode;
import org.skyscreamer.jsonassert.JSONAssert;

public class TestQuestionnaireResponse {
    private static final FhirContext fhirContext = FhirContext.forCached(FhirVersionEnum.DSTU3);
    private static final IParser jsonParser = fhirContext.newJsonParser().setPrettyPrint(true);
    private static final EvaluationSettings evaluationSettings = EvaluationSettings.getDefault();

    private static InputStream open(String asset) {
        return TestQuestionnaireResponse.class.getResourceAsStream(asset);
    }

    public static String load(InputStream asset) throws IOException {
        return new String(asset.readAllBytes(), StandardCharsets.UTF_8);
    }

    public static String load(String asset) throws IOException {
        return load(open(asset));
    }

    public static IBaseResource parse(String asset) {
        return jsonParser.parseResource(open(asset));
    }

    public static QuestionnaireResponseProcessor buildProcessor(Repository repository) {
        return new QuestionnaireResponseProcessor(repository);
    }

    /** Fluent interface starts here **/
    public static class Assert {
        public static Extract that(String questionnaireResponseName) {
            return new Extract(questionnaireResponseName);
        }
    }

    static class Extract {
        private Repository repository;
        private QuestionnaireResponse baseResource;

        private final FhirContext fhirContext = FhirContext.forDstu3Cached();

        public Extract(String questionnaireResponseName) {
            baseResource = (QuestionnaireResponse) parse(questionnaireResponseName);
            repository = new IGFileStructureRepository(
                    this.fhirContext,
                    this.getClass()
                                    .getProtectionDomain()
                                    .getCodeSource()
                                    .getLocation()
                                    .getPath() + "org/opencds/cqf/fhir/cr/questionnaireresponse/dstu3",
                    IGLayoutMode.TYPE_PREFIX,
                    EncodingEnum.JSON);
        }

        public GeneratedBundle extract() {
            var libraryEngine = new LibraryEngine(repository, evaluationSettings);
            return new GeneratedBundle(
                    (Bundle) buildProcessor(repository).extract(baseResource, null, null, libraryEngine));
        }
    }

    static class GeneratedBundle {
        Bundle bundle;

        public GeneratedBundle(Bundle bundle) {
            this.bundle = bundle;
        }

        public void isEqualsTo(String expectedBundleAssetName) {
            try {
                JSONAssert.assertEquals(load(expectedBundleAssetName), jsonParser.encodeResourceToString(bundle), true);
            } catch (JSONException | IOException e) {
                e.printStackTrace();
                fail("Unable to compare Jsons: " + e.getMessage());
            }
        }
    }
}
