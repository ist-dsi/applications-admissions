package pt.ist.applications.admissions.domain;

import com.google.common.base.Strings;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.fenixedu.bennu.ApplicationsAdmissionsConfiguration;
import org.fenixedu.messaging.core.domain.Message;
import org.fenixedu.messaging.core.template.DeclareMessageTemplate;
import org.fenixedu.messaging.core.template.TemplateParameter;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.springframework.context.MessageSource;
import pt.ist.applications.admissions.util.Utils;
import pt.ist.drive.sdk.ClientFactory;
import pt.ist.fenixframework.Atomic;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.Collator;
import java.util.*;

@DeclareMessageTemplate(bundle = "resources.ApplicationsAdmissionsResources", id = "message.applications.admissions.candidacy",
        description = "message.applications.admissions.candidacy", subject = "message.applications.admissions.candidacy.subject",
        text = "message.applications.admissions.candidacy.messageBody", parameters = {
                @TemplateParameter(id = "contestName", description = "template.parameter.contestName"),
                @TemplateParameter(id = "url", description = "template.parameter.url"),
                @TemplateParameter(id = "endDate", description = "template.parameter.endDate") })
@DeclareMessageTemplate(bundle = "resources.ApplicationsAdmissionsResources", id = "message.applications.admissions.submition",
        description = "message.applications.admissions.submition", subject = "message.applications.admissions.submition.subject",
        text = "message.applications.admissions.submition.messageBody", parameters = {
                @TemplateParameter(id = "contestName", description = "template.parameter.contestName"),
                @TemplateParameter(id = "candidateFiles", description = "template.parameter.files") })
public class Candidate extends Candidate_Base {

    Candidate(final Contest contest, final String name, final String email, final String contestPath,
              final MessageSource messageSource) {
        super();
        setCandidateNumber(contest.getCandidateSet().size() + 1);
        setName(name);
        setEmail(email);
        setContest(contest);
        generateHash(contestPath, messageSource);
        final String directory =
                ClientFactory.configurationDriveClient().createDirectory(contest.getDirectory(), getDirectoryName());
        setDirectory(directory);
        setDirectoryForCandidateDocuments(ClientFactory.configurationDriveClient().createDirectory(directory,
                "CandidateDocuments"));
        setDirectoryForLettersOfRecomendation(ClientFactory.configurationDriveClient().createDirectory(directory,
                "LettersOfRecommendation"));
    }

    private String getDirectoryName() {
        final StringBuilder builder = new StringBuilder();
        builder.append(getCandidateNumber());
        builder.append('_');
        builder.append(getName());
        return builder.toString();
    }

    public boolean verifyHash(final String hash) {
        return verifyHashForEdit(hash) || verifyHashForView(hash);
    }

    public boolean verifyHashForEdit(final String hash) {
        return Utils.match(getEditHash(), hash) && getContest().isInPeriod();
    }

    public boolean verifyHashForView(final String hash) {
        return Utils.match(getEditHash(), hash) || getContest().verifyHashForView(hash);
    }

    @Atomic
    public void undispose() {
        setEditHash(null);
    }

    @Atomic
    public void generateHash(final String contestPath, final MessageSource messageSource) {
        setEditHash(UUID.randomUUID().toString());
        sendRegistrationEmail(contestPath, messageSource);
    }

    @Atomic
    public void delete() {
        ClientFactory.configurationDriveClient().deleteDirectory(getDirectory());
        setContest(null);
        deleteDomainObject();
    }

    public void deleteItem(final String item) {
        // Make sure the item 'belongs' to this candidate before we delete it. 
        if (ClientFactory.configurationDriveClient().dirContainsItem(getDirectoryForCandidateDocuments(), item)) {
            ClientFactory.configurationDriveClient().deleteFile(item);
        }
    }

    @Atomic
    public void submitApplication(final MessageSource messageSource) {
        final DateTime now = new DateTime();
        setSealDate(now);
        setSeal(calculateDigest());
        sendApplicationSubmitionEmail(messageSource);
    }

    public String calculateDigest() {
        final String dateTimeFormat = "yyyy-MM-dd HH:mm:ss";

        final StringBuilder input = new StringBuilder();

        input.append(getSealDate().toString(dateTimeFormat));
        input.append(getCandidateNumber());
        input.append(getContest().getContestName());

        final JsonArray ja = ClientFactory.configurationDriveClient().listDirectory(getDirectoryForCandidateDocuments());
        for (final JsonObject jo : sortBy(ja, "name", "modified", "size")) {
            input.append(jo.get("name").getAsString());
            input.append(jo.get("size").getAsString());
            input.append(new DateTime(jo.get("modified").getAsLong()).toString(dateTimeFormat));
        }

        try {
            final MessageDigest md = MessageDigest.getInstance("SHA-256");
            md.update(input.toString().getBytes());
            final byte byteData[] = md.digest();

            final StringBuilder sb = new StringBuilder();
            for (byte element : byteData) {
                sb.append(Integer.toString((element & 0xff) + 0x100, 16).substring(1));
            }
            return sb.toString();
        } catch (final NoSuchAlgorithmException e) {
            throw new Error(e);
        }
    }

    private List<JsonObject> sortBy(final JsonArray ja, final String... keys) {
        final List<JsonObject> result = new ArrayList<>();
        for (final JsonElement je : ja) {
            final JsonObject jo = je.getAsJsonObject();
            result.add(jo);
        }
        Collections.sort(result, new Comparator<JsonObject>() {
            @Override
            public int compare(final JsonObject o1, final JsonObject o2) {
                for (final String key : keys) {
                    final String v1 = o1.get(key).getAsString();
                    final String v2 = o2.get(key).getAsString();
                    final int i = Collator.getInstance().compare(v1, v2);
                    if (i != 0) {
                        return i;
                    }
                }
                return o1.hashCode() - o2.hashCode();
            }
        });
        return result;
    }

    public void sendRegistrationEmail(final String contestPath, final MessageSource messageSource) {
        if (!Strings.isNullOrEmpty(getEmail())) {
            final StringBuilder url = new StringBuilder();
            String candidacyBasePath = ApplicationsAdmissionsConfiguration.getConfiguration().candidacyBasePath();
            url.append(contestPath).append(candidacyBasePath).append(getExternalId()).append("?hash=")
                    .append(getEditHash());
            final DateTimeFormatter datePattern = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm (ZZZ)");

            Message.fromSystem()
                    .singleTos(ApplicationsAdmissionsConfiguration.getConfiguration().emailTo())
                    .singleBcc(getEmail())
                    .template("message.applications.admissions.candidacy")
                    .parameter("contestName", getContest().getContestName()).parameter("url", url.toString())
                    .parameter("endDate", datePattern.print(getContest().getEndDate())).and().send();
        }
    }

    private void sendApplicationSubmitionEmail(final MessageSource messageSource) {
        if (!Strings.isNullOrEmpty(getEmail())) {

            final StringBuilder files = new StringBuilder();
            final JsonArray ja = ClientFactory.configurationDriveClient().listDirectory(getDirectoryForCandidateDocuments());

            for (final JsonObject jo : sortBy(ja, "name", "created", "modified", "size")) {
                files.append("\n").append(jo.get("name").getAsString()).append(" | ");
                files.append(jo.get("size").getAsString()).append(" | ");
                files.append(new DateTime(jo.get("created").getAsLong()).toString("yyyy-MM-dd HH:mm")).append(" | ");
                files.append(new DateTime(jo.get("modified").getAsLong()).toString("yyyy-MM-dd HH:mm"));
            }

            Message.fromSystem()
                    .singleTos(ApplicationsAdmissionsConfiguration.getConfiguration().emailTo())
                    .singleBcc(getEmail())
                    .template("message.applications.admissions.submition")
                    .parameter("contestName", getContest().getContestName()).parameter("candidateFiles", files.toString()).and()
                    .send();
        }
    }

}
