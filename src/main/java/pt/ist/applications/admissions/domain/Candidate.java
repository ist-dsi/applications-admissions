package pt.ist.applications.admissions.domain;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.Collator;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

import org.joda.time.DateTime;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import pt.ist.applications.admissions.util.Utils;
import pt.ist.drive.sdk.ClientFactory;
import pt.ist.fenixframework.Atomic;

public class Candidate extends Candidate_Base {

    Candidate(final Contest contest, final String name) {
        super();
        setCandidateNumber(contest.getCandidateSet().size() + 1);
        setName(name);
        setContest(contest);
        generateHash();
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
        return getContest().verifyHashForView(hash);
    }

    @Atomic
    public void undispose() {
        setEditHash(null);
    }

    @Atomic
    public void generateHash() {
        setEditHash(UUID.randomUUID().toString());
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
    public void submitApplication() {
        final DateTime now = new DateTime();
        setSealDate(now);
        setSeal(calculateDigest());
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
            for (int i = 0; i < byteData.length; i++) {
                sb.append(Integer.toString((byteData[i] & 0xff) + 0x100, 16).substring(1));
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

}
