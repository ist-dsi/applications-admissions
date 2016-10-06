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
import java.util.Comparator;
import java.util.Locale;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Form;
import javax.ws.rs.core.MediaType;

import org.apache.commons.validator.routines.EmailValidator;
import org.apache.tika.mime.MimeType;
import org.apache.tika.mime.MimeTypeException;
import org.apache.tika.mime.MimeTypes;
import org.fenixedu.bennu.ApplicationsAdmissionsConfiguration;
import org.fenixedu.bennu.core.domain.Bennu;
import org.fenixedu.bennu.core.security.Authenticate;
import org.fenixedu.bennu.portal.servlet.PortalBackend;
import org.fenixedu.bennu.spring.portal.SpringApplication;
import org.fenixedu.bennu.spring.portal.SpringFunctionality;
import org.fenixedu.commons.i18n.I18N;
import org.fenixedu.commons.spreadsheet.SheetData;
import org.fenixedu.commons.spreadsheet.SpreadsheetBuilder;
import org.fenixedu.commons.spreadsheet.WorkbookExportFormat;
import org.glassfish.jersey.media.multipart.MultiPartFeature;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import com.google.common.base.Strings;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import pt.ist.applications.admissions.domain.Candidate;
import pt.ist.applications.admissions.domain.Contest;
import pt.ist.applications.admissions.util.Utils;
import pt.ist.drive.sdk.ClientFactory;

@SpringApplication(group = "anyone", path = "applications", title = "title.applications.admissions",
        hint = "applications-admissions")
@SpringFunctionality(app = ApplicationsAdmissionsController.class, title = "title.applications.admissions")
@RequestMapping("/admissions")
public class ApplicationsAdmissionsController {

    protected static final Client CLIENT = ClientBuilder.newClient();

    static {
        CLIENT.register(MultiPartFeature.class);
    }

    @Autowired
    private MessageSource messageSource;

    @RequestMapping(method = RequestMethod.GET)
    public String home(final Model model) {
        final JsonArray contests = Bennu.getInstance().getContestSet().stream()
                .sorted(Comparator.comparing((Contest c) -> c.getBeginDate()).reversed()).map(this::toJsonObject)
                .collect(Utils.toJsonArray());
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
    public String contest(@PathVariable Contest contest, @RequestParam(required = false) String hash, final Model model,
            final HttpServletRequest request) {
        if (Authenticate.getUser() == null) {
            I18N.setLocale(request.getSession(false), new Locale("en", "GB"));
        }

        final JsonObject result = toJsonObject(contest);
        if (Contest.canManageContests() || contest.verifyHashForView(hash)) {
            final JsonArray candidates =
                    contest.getCandidateSet().stream().sorted(Comparator.comparing((Candidate c) -> c.getCandidateNumber()))
                            .map(this::toJsonObject).collect(Utils.toJsonArray());
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
    public String registerCandidateSave(@PathVariable Contest contest, final Model model, @RequestBody final String stuff,
            HttpServletRequest request) {
        final Map<String, String> map = Utils.toMap(stuff, "name", "value");
        model.addAttribute("contest", toJsonObject(contest));
        if (Authenticate.getUser() == null) {
            Form form = new Form();
            form.param("secret", ApplicationsAdmissionsConfiguration.getConfiguration().recaptchaSecretKey());
            form.param("response", map.get("g-recaptcha-response"));

            WebTarget target = CLIENT.target("https://www.google.com/recaptcha/api/siteverify");
            String post = target.request(MediaType.APPLICATION_JSON_TYPE)
                    .post(Entity.entity(form, MediaType.APPLICATION_FORM_URLENCODED_TYPE), String.class);

            JsonObject jo = new JsonParser().parse(post).getAsJsonObject();
            if (!jo.get("success").getAsBoolean()) {
                throw new Error(jo.get("error-codes").getAsString());
            }
        }
        Candidate candidate = null;
        final String email = map.get("email");
        if (EmailValidator.getInstance().isValid(email)) {
            String path = request.getRequestURL().substring(0,
                    request.getRequestURL().length() - request.getRequestURI().length() + request.getContextPath().length());
            candidate = contest.registerCandidate(map.get("name"), email, path, messageSource);
        }
        return candidate == null ? "redirect:/admissions/contest/" + contest.getExternalId() : "redirect:/admissions/candidate/"
                + candidate.getExternalId();

    }

    @RequestMapping(value = "/candidateRegistrationConfirmation/{contest}", method = RequestMethod.GET)
    public String candidateRegistrationConfirmation(@PathVariable Contest contest, final Model model) {
        final JsonObject result = toJsonObject(contest);
        model.addAttribute("contest", result);
        return "applications-admissions/candidateRegistrationConfirmation";
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
    public String candidate(@PathVariable Candidate candidate, @RequestParam(required = false) String hash, final Model model,
            final HttpServletRequest request) {
        if (Authenticate.getUser() == null) {
            I18N.setLocale(request.getSession(false), new Locale("en", "GB"));
        }
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
                final String contentType = file.getContentType();
                final String filename = chooseFileName(name, file.getOriginalFilename(), contentType);
                ClientFactory.configurationDriveClient().upload(candidate.getDirectoryForCandidateDocuments(), filename,
                        file.getInputStream(), contentType);
            } catch (final IOException e) {
                throw new Error(e);
            }
        }
        return "redirect:/admissions/candidate/" + candidate.getExternalId() + "?hash=" + hash;
    }

    private String chooseFileName(final String name, final String originalFilename, final String contentType) {
        if (name.indexOf('.') < 0) {
            if (!Strings.isNullOrEmpty(originalFilename)) {
                final int i = originalFilename.lastIndexOf('.');
                if (i >= 0) {
                    return name + originalFilename.substring(i);
                }
            }
            final MimeTypes allTypes = MimeTypes.getDefaultMimeTypes();
            try {
                MimeType type = allTypes.forName(contentType);
                if (type != null) {
                    for (final String ext : type.getExtensions()) {
                        return name + "." + ext;
                    }
                }
            } catch (final MimeTypeException ex) {
                // No problem, just don't add an extension
            }

        }
        return name;
    }

    @RequestMapping(value = "/candidate/{candidate}/undispose", method = RequestMethod.POST)
    public String candidateUndispose(@PathVariable Candidate candidate, final Model model) {
        if (Contest.canManageContests()) {
            candidate.undispose();
        }
        return "redirect:/admissions/candidate/" + candidate.getExternalId();
    }

    @RequestMapping(value = "/candidate/{candidate}/generateLink", method = RequestMethod.POST)
    public String candidateGenerateLink(@PathVariable Candidate candidate, final Model model, HttpServletRequest request) {
        if (Contest.canManageContests()) {
            String path = request.getRequestURL().substring(0,
                    request.getRequestURL().length() - request.getRequestURI().length() + request.getContextPath().length());
            candidate.generateHash(path, messageSource);
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
    public String candidateDeleteItem(@PathVariable Candidate candidate, @PathVariable String item,
            @RequestParam(required = false) String hash, final Model model) {
        final String id = candidate.getContest().getExternalId();
        if (candidate.verifyHashForEdit(hash)) {
            candidate.deleteItem(item);
        }
        return "redirect:/admissions/contest/" + id;
    }

    @RequestMapping(value = "/candidate/{candidate}/submitApplication", method = RequestMethod.POST)
    public String submitApplication(@PathVariable Candidate candidate, @RequestParam(required = false) String hash,
            final Model model) {
        if (candidate.verifyHashForEdit(hash)) {
            candidate.submitApplication(messageSource);
        }
        return "redirect:/admissions/candidate/" + candidate.getExternalId() + "?hash=" + hash;
    }

    @RequestMapping(value = "/candidate/{candidate}/logs", method = RequestMethod.GET)
    public String candidateLogs(@PathVariable Candidate candidate, final Model model) {
        if (Contest.canManageContests()) {
            model.addAttribute("candidate", toJsonObjectWithLogs(candidate));
            return "applications-admissions/candidateLogs";
        }
        return "redirect:/admissions/contest/" + candidate.getContest().getExternalId();
    }

    private JsonObject toJsonObject(final Candidate c) {
        final JsonObject object = new JsonObject();
        object.addProperty("id", c.getExternalId());
        object.addProperty("candidateNumber", c.getCandidateNumber());
        object.addProperty("name", c.getName());
        object.addProperty("email", c.getEmail());
        final DateTime sealDate = c.getSealDate();
        if (sealDate != null) {
            object.addProperty("sealDate", sealDate.toString(Utils.DATE_TIME_PATTERN));
            object.addProperty("seal", c.getSeal());
        }
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
        if (c.getSealDate() != null) {
            object.addProperty("calculatedDigest", c.calculateDigest());
        }
        return object;
    }

    private JsonObject toJsonObjectWithLogs(final Candidate c) {
        final JsonObject object = toJsonObject(c);
        final Contest contest = c.getContest();
        object.add("contest", toJsonObject(contest));
        object.add("logs",
                ClientFactory.configurationDriveClient().logs(c.getDirectoryForCandidateDocuments(), Integer.MAX_VALUE));
        object.add("recommendationLogs",
                ClientFactory.configurationDriveClient().logs(c.getDirectoryForLettersOfRecomendation(), Integer.MAX_VALUE));
        return object;
    }

    @RequestMapping(value = "/contest/{contest}/exportContestCandidates", method = RequestMethod.GET)
    public String exportContestCandidates(@PathVariable Contest contest, final Model model, HttpServletResponse response) throws IOException {
        SpreadsheetBuilder builder = new SpreadsheetBuilder();
        String fileName = messageSource.getMessage("title.applications.admissions", null, I18N.getLocale());
        response.setContentType("application/vnd.ms-excel");
        response.setHeader("Content-Disposition", "filename=" + fileName+".xls");
        
        builder.addSheet(fileName,
                new SheetData<Candidate>(contest.getCandidateSet()) {
                    @Override
                    protected void makeLine(Candidate candidate) {
                        addCell(messageSource.getMessage("label.applications.admissions.candidate.number", null, I18N.getLocale()), candidate.getCandidateNumber());
                        addCell(messageSource.getMessage("label.applications.admissions.candidate", null, I18N.getLocale()), candidate.getName());
                        addCell(messageSource.getMessage("label.applications.admissions.candidate.email", null, I18N.getLocale()), candidate.getEmail());
                        addCell(messageSource.getMessage("label.application.seal.date", null, I18N.getLocale()), candidate.getSealDate());
                        
                        final JsonArray candidateDocuments = ClientFactory.configurationDriveClient().listDirectory(candidate.getDirectoryForCandidateDocuments());
                        addCell(messageSource.getMessage("label.applications.admissions.candidate.documents", null, I18N.getLocale()), candidateDocuments.size());
                        final JsonArray lettersOfRecomendation = ClientFactory.configurationDriveClient().listDirectory(candidate.getDirectoryForLettersOfRecomendation());
                        addCell(messageSource.getMessage("label.applications.admissions.candidate.lettersOfRecommendation", null, I18N.getLocale()), lettersOfRecomendation.size());
                    }
                });
        
        
        builder.build(WorkbookExportFormat.TSV, response.getOutputStream());
        response.flushBuffer();
        return null;
    }
}
