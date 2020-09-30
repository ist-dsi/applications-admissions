package pt.ist.applications.admissions.ui.json;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.Collator;
import java.util.*;

import org.joda.time.DateTime;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import pt.ist.applications.admissions.domain.Candidate;
import pt.ist.applications.admissions.domain.Contest;
import pt.ist.applications.admissions.util.Utils;
import pt.ist.drive.sdk.ClientFactory;

public class CandidateAdapter {

    public static JsonElement listItemView(Candidate candidate) {
        final JsonObject object = new JsonObject();
        object.addProperty("id", candidate.getExternalId());
        object.addProperty("candidateNumber", candidate.getCandidateNumber());
        object.addProperty("name", candidate.getName());
        object.addProperty("email", candidate.getEmail());

        return object;
    }

    public static JsonElement detailedView(Candidate candidate, String hash) {
        final JsonObject object = CandidateAdapter.listItemView(candidate).getAsJsonObject();

        final DateTime sealDate = candidate.getSealDate();
        if (sealDate != null) {
            object.addProperty("sealDate", sealDate.toString(Utils.DATE_TIME_PATTERN));
            object.addProperty("seal", candidate.getSeal());
        }
        if (Contest.canManageContests()) {
            object.addProperty("editHash", candidate.getEditHash());
        }

        final Contest contest = candidate.getContest();
        object.add("contest", ContestAdapter.listItemView(contest).getAsJsonObject());

        object.add("items",
                ClientFactory.configurationDriveClient().listDirectory(candidate.getDirectoryForCandidateDocuments()));
        if (Contest.canManageContests() || contest.verifyHashForView(hash)) {
            object.add("letterItems",
                    ClientFactory.configurationDriveClient().listDirectory(candidate.getDirectoryForLettersOfRecomendation()));
        }
        if (sealDate != null) {
            object.addProperty("calculatedDigest", candidate.calculateDigest());
        }

        return object;
    }

    public static JsonElement logView(Candidate candidate) {
        final JsonObject object = CandidateAdapter.listItemView(candidate).getAsJsonObject();

        final Contest contest = candidate.getContest();
        object.add("contest", ContestAdapter.listItemView(contest).getAsJsonObject());

        object.add("logs",
                ClientFactory.configurationDriveClient().logs(candidate.getDirectoryForCandidateDocuments(), Integer.MAX_VALUE));

        object.add("recommendationLogs",
                ClientFactory.configurationDriveClient().logs(candidate.getDirectoryForLettersOfRecomendation(), Integer.MAX_VALUE));

        return object;
    }
}
