package pt.ist.applications.admissions.ui.json;

import java.util.Comparator;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import pt.ist.applications.admissions.domain.Candidate;
import pt.ist.applications.admissions.domain.Contest;
import pt.ist.applications.admissions.util.Utils;

public class ContestAdapter {

    public static JsonElement listItemView(Contest contest) {
        final JsonObject object = new JsonObject();
        object.addProperty("id", contest.getExternalId());
        object.addProperty("contestName", contest.getContestName());
        object.addProperty("beginDate", contest.getBeginDate().toString(Utils.DATE_TIME_PATTERN));
        object.addProperty("endDate", contest.getEndDate().toString(Utils.DATE_TIME_PATTERN));
        object.addProperty("isInPeriod", contest.isInPeriod());
        if (Contest.canManageContests()) {
            object.addProperty("viewHash", contest.getViewHash());
        }
        return object;
    }

    public static JsonElement detailedView(Contest contest, String hash) {
        final JsonObject result = ContestAdapter.listItemView(contest).getAsJsonObject();

        if (Contest.canManageContests() || contest.verifyHashForView(hash)) {
            final JsonArray candidates =
                    contest.getCandidateSet().stream().sorted(Comparator.comparing(Candidate::getCandidateNumber))
                            .map(CandidateAdapter::listItemView).collect(Utils.toJsonArray());
            result.add("candidates", candidates);
        }

        return result;
    }
}
