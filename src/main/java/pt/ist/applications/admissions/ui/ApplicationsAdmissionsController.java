/**
 * Copyright © 2015 Instituto Superior Técnico
 *
 * This file is part of Applications and Admissions Module.
 *
 * Applications and Admissions Module is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Applications and Admissions Module is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with FenixEdu Spaces.  If not, see <http://www.gnu.org/licenses/>.
 */
package pt.ist.applications.admissions.ui;

import java.io.IOException;
import java.util.Map;

import javax.servlet.http.HttpServletResponse;

import org.fenixedu.bennu.core.domain.Bennu;
import org.fenixedu.bennu.spring.portal.SpringApplication;
import org.fenixedu.bennu.spring.portal.SpringFunctionality;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import pt.ist.applications.admissions.domain.Candidate;
import pt.ist.applications.admissions.domain.Contest;
import pt.ist.applications.admissions.util.Utils;
import pt.ist.drive.sdk.ClientFactory;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

@SpringApplication(group = "anyone", path = "applications", title = "title.applications.admissions",
        hint = "applications-admissions")
@SpringFunctionality(app = ApplicationsAdmissionsController.class, title = "title.applications.admissions")
@RequestMapping("/admissions")
public class ApplicationsAdmissionsController {

    @RequestMapping(method = RequestMethod.GET)
    public String home(final Model model) {
        final JsonArray contests =
                Bennu.getInstance().getContestSet().stream().map(this::toJsonObject).collect(Utils.toJsonArray());
        model.addAttribute("contests", contests);
        return "applications-admissions/home";
    }

    private JsonObject toJsonObject(final Contest c) {
        final JsonObject object = new JsonObject();
        object.addProperty("id", c.getExternalId());
        object.addProperty("contestName", c.getContestName());
        object.addProperty("beginDate", c.getBeginDate().toString(Utils.DATE_TIME_PATTERN));
        object.addProperty("endDate", c.getEndDate().toString(Utils.DATE_TIME_PATTERN));
        if (Contest.canManageContests()) {
            object.addProperty("viewHash", c.getViewHash());
        }
        return object;
    }

    @RequestMapping(value = "/createContest", method = RequestMethod.GET)
    public String createContest(final Model model) {
        return "applications-admissions/createContest";
    }

    @RequestMapping(value = "/createContest/save", method = RequestMethod.POST)
    public String createContestSave(final Model model, @RequestBody final String stuff) {
        final DateTimeFormatter formatter = DateTimeFormat.forPattern(Utils.DATE_TIME_PATTERN);

        final Map<String, String> map = Utils.toMap(stuff, "name", "value");
        final String contestName = map.get("contestName");
        final DateTime beginDate = formatter.parseDateTime(map.get("beginDate"));
        final DateTime endDate = formatter.parseDateTime(map.get("endDate"));

        Contest.create(contestName, beginDate, endDate);

        return "redirect:/admissions";
    }

    @RequestMapping(value = "/contest/{contest}", method = RequestMethod.GET)
    public String contest(@PathVariable Contest contest, @RequestParam(required = false) String hash, final Model model) {
        final JsonObject result = toJsonObject(contest);
        if (Contest.canManageContests() || contest.verifyHashForView(hash)) {
            final JsonArray candidates = contest.getCandidateSet().stream().map(this::toJsonObject).collect(Utils.toJsonArray());
            result.add("candidates", candidates);
        }
        model.addAttribute("contest", result);
        return "applications-admissions/contest";
    }

    @RequestMapping(value = "/contest/{contest}/registerCandidate", method = RequestMethod.GET)
    public String registerCandidate(@PathVariable Contest contest, final Model model) {
        model.addAttribute("contest", toJsonObject(contest));
        return "applications-admissions/registerCandidate";
    }

    @RequestMapping(value = "/contest/{contest}/registerCandidate", method = RequestMethod.POST)
    public String registerCandidateSave(@PathVariable Contest contest, final Model model, @RequestBody final String stuff) {
        final Map<String, String> map = Utils.toMap(stuff, "name", "value");
        final String name = map.get("name");

        final Candidate candidate = contest.registerCandidate(name);

        return candidate == null ? "redirect:/admissions/contest/" + contest.getExternalId() : "redirect:/admissions/candidate/"
                + candidate.getExternalId();
    }

    @RequestMapping(value = "/contest/{contest}/undispose", method = RequestMethod.POST)
    public String contestUndispose(@PathVariable Contest contest, final Model model) {
        if (Contest.canManageContests()) {
            contest.undispose();
        }
        return "redirect:/admissions/contest/" + contest.getExternalId();
    }

    @RequestMapping(value = "/contest/{contest}/generateLink", method = RequestMethod.POST)
    public String contestGenerateLink(@PathVariable Contest contest, final Model model) {
        if (Contest.canManageContests()) {
            contest.generateHash();
        }
        return "redirect:/admissions/contest/" + contest.getExternalId();
    }

    @RequestMapping(value = "/contest/{contest}/delete", method = RequestMethod.POST)
    public String contestDelete(@PathVariable Contest contest, final Model model) {
        if (Contest.canManageContests()) {
            contest.delete();
        }
        return "redirect:/admissions";
    }

    @RequestMapping(value = "/contest/{contest}/edit", method = RequestMethod.POST)
    public String contestEdit(@PathVariable Contest contest, final Model model, @RequestBody final String stuff) {
        if (Contest.canManageContests()) {
            final DateTimeFormatter formatter = DateTimeFormat.forPattern(Utils.DATE_TIME_PATTERN);

            final Map<String, String> map = Utils.toMap(stuff, "name", "value");
            final String contestName = map.get("contestName");
            final DateTime beginDate = formatter.parseDateTime(map.get("beginDate"));
            final DateTime endDate = formatter.parseDateTime(map.get("endDate"));

            contest.edit(contestName, beginDate, endDate);
        }
        return "redirect:/admissions/contest/" + contest.getExternalId();
    }

    @RequestMapping(value = "/candidate/{candidate}", method = RequestMethod.GET)
    public String candidate(@PathVariable Candidate candidate, @RequestParam(required = false) String hash, final Model model) {
        if (Contest.canManageContests() || candidate.verifyHash(hash)) {
            model.addAttribute("candidate", toJsonObjectWithContest(candidate, hash));
            return "applications-admissions/candidate";
        }
        return "redirect:/admissions/contest/" + candidate.getContest().getExternalId();
    }

    @RequestMapping(value = "/candidate/{candidate}/upload", method = RequestMethod.POST)
    public String candidateUpload(@PathVariable Candidate candidate, @RequestParam String hash, @RequestParam MultipartFile file,
            @RequestParam String name, final Model model) {
        if (candidate.verifyHashForEdit(hash)) {
            try {
                ClientFactory.configurationDriveClient().upload(candidate.getDirectoryForCandidateDocuments(), name,
                        file.getInputStream(), file.getContentType());
            } catch (final IOException e) {
                throw new Error(e);
            }
        }
        return "redirect:/admissions/candidate/" + candidate.getExternalId() + "?hash=" + hash;
    }

    @RequestMapping(value = "/candidate/{candidate}/undispose", method = RequestMethod.POST)
    public String candidateUndispose(@PathVariable Candidate candidate, final Model model) {
        if (Contest.canManageContests()) {
            candidate.undispose();
        }
        return "redirect:/admissions/candidate/" + candidate.getExternalId();
    }

    @RequestMapping(value = "/candidate/{candidate}/generateLink", method = RequestMethod.POST)
    public String candidateGenerateLink(@PathVariable Candidate candidate, final Model model) {
        if (Contest.canManageContests()) {
            candidate.generateHash();
        }
        return "redirect:/admissions/candidate/" + candidate.getExternalId();
    }

    @RequestMapping(value = "/candidate/{candidate}/download", method = RequestMethod.GET)
    public String candidateDownload(@PathVariable Candidate candidate, @RequestParam(required = false) String hash,
            final Model model, final HttpServletResponse response) throws IOException {
        if (Contest.canManageContests() || candidate.getContest().verifyHashForView(hash)) {
            ClientFactory.configurationDriveClient().downloadDir(candidate.getDirectory(), response);
            return null;
        } else {
            return "redirect:/admissions/candidate/" + candidate.getExternalId() + "?hash=" + hash;
        }
    }

    @RequestMapping(value = "/candidate/{candidate}/download/{id}", method = RequestMethod.GET)
    public String download(@PathVariable Candidate candidate, @PathVariable String id,
            @RequestParam(required = false) String hash, final HttpServletResponse response) throws IOException {
        if (Contest.canManageContests() || candidate.verifyHash(hash)) {
            ClientFactory.configurationDriveClient().downloadFile(id, response);
            return null;
        } else {
            return "redirect:/admissions/candidate/" + candidate.getExternalId() + "?hash=" + hash;
        }
    }

    @RequestMapping(value = "/candidate/{candidate}/delete", method = RequestMethod.POST)
    public String candidateDelete(@PathVariable Candidate candidate, final Model model) {
        final String id = candidate.getContest().getExternalId();
        if (Contest.canManageContests()) {
            candidate.delete();
        }
        return "redirect:/admissions/contest/" + id;
    }

    @RequestMapping(value = "/candidate/{candidate}/delete/{item}", method = RequestMethod.POST)
    public String candidateDeleteItem(@PathVariable Candidate candidate, @PathVariable String item, @RequestParam(
            required = false) String hash, final Model model) {
        final String id = candidate.getContest().getExternalId();
        if (candidate.verifyHashForEdit(hash)) {
            candidate.deleteItem(item);
        }
        return "redirect:/admissions/contest/" + id;
    }

    private JsonObject toJsonObject(final Candidate c) {
        final JsonObject object = new JsonObject();
        object.addProperty("id", c.getExternalId());
        object.addProperty("candidateNumber", c.getCandidateNumber());
        object.addProperty("name", c.getName());
        if (Contest.canManageContests()) {
            object.addProperty("editHash", c.getEditHash());
        }
        return object;
    }

    private JsonObject toJsonObjectWithContest(final Candidate c, final String hash) {
        final JsonObject object = toJsonObject(c);
        final Contest contest = c.getContest();
        object.add("contest", toJsonObject(contest));
        object.add("items", ClientFactory.configurationDriveClient().listDirectory(c.getDirectoryForCandidateDocuments()));
        if (Contest.canManageContests() || contest.verifyHashForView(hash)) {
            object.add("letterItems",
                    ClientFactory.configurationDriveClient().listDirectory(c.getDirectoryForLettersOfRecomendation()));
        }
        return object;
    }

}
