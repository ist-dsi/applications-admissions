package pt.ist.applications.admissions.ui;

import com.google.common.base.Strings;
import com.google.gson.*;
import org.apache.commons.validator.routines.EmailValidator;
import org.apache.tika.mime.MimeType;
import org.apache.tika.mime.MimeTypeException;
import org.apache.tika.mime.MimeTypes;
import org.fenixedu.bennu.ApplicationsAdmissionsConfiguration;
import org.fenixedu.bennu.core.domain.Bennu;
import org.fenixedu.bennu.core.security.Authenticate;
import org.fenixedu.bennu.core.security.SkipCSRF;
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
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import pt.ist.applications.admissions.domain.Candidate;
import pt.ist.applications.admissions.domain.Contest;
import pt.ist.applications.admissions.ui.json.CandidateAdapter;
import pt.ist.applications.admissions.ui.json.ContestAdapter;
import pt.ist.applications.admissions.util.Utils;
import pt.ist.drive.sdk.ClientFactory;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Form;
import javax.ws.rs.core.MediaType;
import java.io.IOException;
import java.util.Comparator;
import java.util.Locale;
import java.util.Map;
import java.util.function.Supplier;

@RestController
@RequestMapping(path = "/applicationsAdmissions", produces = "application/json; charset=UTF-8")
public class ApplicationsAdmissionsRestController {

    protected static final Client CLIENT = ClientBuilder.newClient();

    static {
        CLIENT.register(MultiPartFeature.class);
    }

    @Autowired
    private MessageSource messageSource;

    @RequestMapping(value = "/permissions", method = RequestMethod.GET)
    public ResponseEntity<?> getPermissions(final HttpServletRequest request,
            @RequestParam(required = false) Contest contest,
            @RequestParam(required = false) Candidate candidate,
            @RequestParam(required = false) String hash) {
        return sendResponse(() -> {
            JsonObject permissions = new JsonObject();

            permissions.addProperty("canViewContest",
                    true);
            permissions.addProperty("canCreateContest",
                    Contest.canManageContests());
            permissions.addProperty("canEditContest",
                    contest != null && Contest.canManageContests());
            permissions.addProperty("canDeleteContest",
                    contest != null && Contest.canManageContests());
            permissions.addProperty("canViewContestHash",
                    contest != null && Contest.canManageContests());
            permissions.addProperty("canCreateContestHash",
                    contest != null && Contest.canManageContests());
            permissions.addProperty("canDeleteContestHash",
                    contest != null && Contest.canManageContests());
            permissions.addProperty("canExportContestCandidates",
                    contest != null && Contest.canManageContests());

            permissions.addProperty("canViewCandidate",
                    candidate != null && (candidate.verifyHash(hash) || Contest.canManageContests()));
            permissions.addProperty("canCreateCandidate",
                    contest != null && (contest.isInPeriod() || Contest.canManageContests()));
            permissions.addProperty("canDeleteCandidate",
                    candidate != null && Contest.canManageContests());
            permissions.addProperty("canViewCandidateHash",
                    candidate != null && Contest.canManageContests());
            permissions.addProperty("canCreateCandidateHash",
                    candidate != null && Contest.canManageContests());
            permissions.addProperty("canDeleteCandidateHash",
                    candidate != null && Contest.canManageContests());
            permissions.addProperty("canCreateCandidateSeal",
                    candidate != null && candidate.verifyHashForEdit(hash) && (candidate.getSeal() == null));
            permissions.addProperty("canDownloadSingleCandidateFile",
                    candidate != null && (Contest.canManageContests() || candidate.verifyHash(hash)));
            permissions.addProperty("canDownloadAllCandidateFiles",
                    (candidate != null && (Contest.canManageContests() || candidate.getContest().verifyHashForView(hash)))
                    || (contest != null && (Contest.canManageContests() || contest.verifyHashForView(hash))));
            permissions.addProperty("canUploadCandidateFile",
                    candidate != null && candidate.verifyHashForEdit(hash) && (candidate.getSeal() == null));
            permissions.addProperty("canDeleteCandidateFile",
                    candidate != null && candidate.verifyHashForEdit(hash) && (candidate.getSeal() == null));
            permissions.addProperty("canViewCandidateLogs",
                    candidate != null && Contest.canManageContests());

            permissions.addProperty("timestamp", DateTime.now().toString());

            return permissions;
        });
    }

    @RequestMapping(value = "/recaptcha", method = RequestMethod.GET)
    public ResponseEntity<?> getRecaptchaConfig() {
        return sendResponse(() -> {
            final JsonObject object = new JsonObject();
            object.addProperty("siteKey",
                    ApplicationsAdmissionsConfiguration.getConfiguration().recaptchaSiteKey());
            return object;
        });
    }

    @RequestMapping(value = "/contest", method = RequestMethod.GET)
    public ResponseEntity<?> getContestList(final HttpServletRequest request) {
        if (Authenticate.getUser() == null) {
            I18N.setLocale(request.getSession(false), new Locale("en", "GB"));
        }

        return sendResponse(() -> Bennu.getInstance().getContestSet().stream()
                .sorted(Comparator.comparing((Contest c) -> c.getBeginDate()).reversed())
                .map(ContestAdapter::listItemView)
                .collect(Utils.toJsonArray()));
    }

    @RequestMapping(value = "/contest/{contest}", method = RequestMethod.GET)
    public ResponseEntity<?> getContest(@PathVariable Contest contest, @RequestParam(required = false) String hash,
            final HttpServletRequest request) {
        if (Authenticate.getUser() == null) {
            I18N.setLocale(request.getSession(false), new Locale("en", "GB"));
        }

        return sendResponse(() -> ContestAdapter.detailedView(contest, hash));
    }

    @SkipCSRF
    @RequestMapping(value = "/contest", method = RequestMethod.POST)
    public ResponseEntity<?> createContest(@RequestBody final String body) {
        if (!Contest.canManageContests()) {
            return sendForbidden();
        }

        Contest resultContest = saveContest(body, null);
        if (resultContest == null) {
            return sendBadRequest();
        }

        return sendResponse(() -> ContestAdapter.detailedView(resultContest, null));
    }

    @SkipCSRF
    @RequestMapping(value = "/contest/{contest}", method = RequestMethod.PATCH)
    public ResponseEntity<?> editContest(@PathVariable Contest contest, @RequestBody final String body) {
        if (!Contest.canManageContests()) {
            return sendForbidden();
        }

        Contest resultContest = saveContest(body, contest);
        if (resultContest == null) {
            return sendBadRequest();
        }

        return sendResponse(() -> ContestAdapter.detailedView(resultContest, null));
    }

    @SkipCSRF
    @RequestMapping(value = "/contest/{contest}", method = RequestMethod.DELETE)
    public ResponseEntity<?> deleteContest(@PathVariable Contest contest) {
        if (!Contest.canManageContests()) {
            return sendForbidden();
        }

        contest.delete();

        return sendOk();
    }

    @SkipCSRF
    @RequestMapping(value = "/contest/{contest}/hash", method = RequestMethod.POST)
    public ResponseEntity<?> createContestHash(@PathVariable Contest contest) {
        if (!Contest.canManageContests()) {
            return sendForbidden();
        }

        contest.generateHash();

        return sendResponse(() -> ContestAdapter.detailedView(contest, null));
    }

    @SkipCSRF
    @RequestMapping(value = "/contest/{contest}/hash", method = RequestMethod.DELETE)
    public ResponseEntity<?> deleteContestHash(@PathVariable Contest contest) {
        if (!Contest.canManageContests()) {
            return sendForbidden();
        }

        contest.undispose();

        return sendResponse(() -> ContestAdapter.detailedView(contest, null));
    }

    @RequestMapping(value = "/contest/{contest}/export", method = RequestMethod.GET)
    public ResponseEntity<?> exportContestCandidates(@PathVariable Contest contest, HttpServletResponse response)
            throws IOException {
        if (!Contest.canManageContests()) {
            return sendForbidden();
        }
        
        SpreadsheetBuilder builder = new SpreadsheetBuilder();
        String fileName = messageSource.getMessage("title.applications.admissions", null, I18N.getLocale());
        response.setContentType("application/vnd.ms-excel");
        response.setHeader("Content-Disposition", "filename=" + fileName + ".xls");

        builder.addSheet(fileName, new SheetData<Candidate>(contest.getCandidateSet()) {
            @Override
            protected void makeLine(Candidate candidate) {
                addCell(messageSource.getMessage("label.applications.admissions.candidate.number", null, I18N.getLocale()),
                        candidate.getCandidateNumber());
                addCell(messageSource.getMessage("label.applications.admissions.candidate", null, I18N.getLocale()),
                        candidate.getName());
                addCell(messageSource.getMessage("label.applications.admissions.candidate.email", null, I18N.getLocale()),
                        candidate.getEmail());
                addCell(messageSource.getMessage("label.application.seal.date", null, I18N.getLocale()), candidate.getSealDate());

                final JsonArray candidateDocuments =
                        ClientFactory.configurationDriveClient().listDirectory(candidate.getDirectoryForCandidateDocuments());
                addCell(messageSource.getMessage("label.applications.admissions.candidate.documents", null, I18N.getLocale()),
                        candidateDocuments.size());
                final JsonArray lettersOfRecomendation =
                        ClientFactory.configurationDriveClient().listDirectory(candidate.getDirectoryForLettersOfRecomendation());
                addCell(messageSource.getMessage("label.applications.admissions.candidate.lettersOfRecommendation", null,
                        I18N.getLocale()), lettersOfRecomendation.size());
            }
        });

        builder.build(WorkbookExportFormat.TSV, response.getOutputStream());
        response.flushBuffer();
        return null;
    }

    @RequestMapping(value = "/candidate/{candidate}", method = RequestMethod.GET)
    public ResponseEntity<?> getCandidate(@PathVariable Candidate candidate, @RequestParam(required = false) String hash,
            final HttpServletRequest request) {
        if (Authenticate.getUser() == null) {
            I18N.setLocale(request.getSession(false), new Locale("en", "GB"));
        }

        if (Contest.canManageContests() || candidate.verifyHash(hash)) {
            return sendResponse(() -> CandidateAdapter.detailedView(candidate, hash));
        }

        return sendForbidden();
    }

    @SkipCSRF
    @RequestMapping(value = "/contest/{contest}/candidate", method = RequestMethod.POST)
    public ResponseEntity<?> createCandidate(@PathVariable Contest contest, @RequestBody final String body,
                                             HttpServletRequest request) {
        if (!(Contest.canManageContests() || contest.isInPeriod())) {
            return sendForbidden();
        }

        String path =
                request.getRequestURL().substring(
                        0,
                        request.getRequestURL().length() - request.getRequestURI().length()
                                + request.getContextPath().length());

        Candidate candidate = saveCandidate(contest, body, path);
        if (candidate == null) {
            return sendBadRequest();
        }
        return sendResponse(() -> CandidateAdapter.detailedView(candidate, null));
    }

    @SkipCSRF
    @RequestMapping(value = "/candidate/{candidate}", method = RequestMethod.DELETE)
    public ResponseEntity<?> deleteCandidate(@PathVariable Candidate candidate) {
        if (!Contest.canManageContests()) {
            return sendForbidden();
        }

        candidate.delete();

        return sendOk();
    }

    @SkipCSRF
    @RequestMapping(value = "/candidate/{candidate}/hash", method = RequestMethod.POST)
    public ResponseEntity<?> createCandidateHash(@PathVariable Candidate candidate, HttpServletRequest request) {
        if (!Contest.canManageContests()) {
            return sendForbidden();
        }

        String path =
                request.getRequestURL().substring(
                        0,
                        request.getRequestURL().length() - request.getRequestURI().length()
                                + request.getContextPath().length());
        candidate.generateHash(path, messageSource);

        return sendResponse(() -> CandidateAdapter.detailedView(candidate, null));
    }

    @SkipCSRF
    @RequestMapping(value = "/candidate/{candidate}/hash", method = RequestMethod.DELETE)
    public ResponseEntity<?> deleteCandidateHash(@PathVariable Candidate candidate) {
        if (!Contest.canManageContests()) {
            return sendForbidden();
        }

        candidate.undispose();

        return sendResponse(() -> CandidateAdapter.detailedView(candidate, null));
    }

    @SkipCSRF
    @RequestMapping(value = "/candidate/{candidate}/seal", method = RequestMethod.POST)
    public ResponseEntity<?> createCandidateSeal(@PathVariable Candidate candidate,
                                                 @RequestParam(required = false) String hash,
                                                 final HttpServletRequest request) {
        if (Authenticate.getUser() == null) {
            I18N.setLocale(request.getSession(false), new Locale("en", "GB"));
        }

        if (!(candidate.verifyHashForEdit(hash) && (candidate.getSeal() == null))) {
            return sendForbidden();
        }

        candidate.submitApplication(messageSource);

        return sendResponse(() -> CandidateAdapter.detailedView(candidate, hash));
    }

    @RequestMapping(value = "/candidate/{candidate}/file/{file}", method = RequestMethod.GET)
    public ResponseEntity<?> downloadSingleCandidateFile(@PathVariable Candidate candidate, @PathVariable String id,
                                                         @RequestParam(required = false) String hash,
                                                         final HttpServletResponse response)
            throws IOException {
        if (!(Contest.canManageContests() || candidate.verifyHash(hash))) {
            return sendForbidden();
        }

        if (!containsFileId(candidate, id)) {
            return sendBadRequest();
        }

        ClientFactory.configurationDriveClient().downloadFile(id, response);
        return null;
    }

    @RequestMapping(value = "/candidate/{candidate}/file/", method = RequestMethod.GET)
    public ResponseEntity<?> downloadAllCandidateFiles(@PathVariable Candidate candidate,
                                                       @RequestParam(required = false) String hash,
                                                       final HttpServletResponse response)
            throws IOException {
        if (!(Contest.canManageContests() || candidate.getContest().verifyHashForView(hash))) {
            return sendForbidden();
        }

        ClientFactory.configurationDriveClient().downloadDir(candidate.getDirectory(), response);

        return null;
    }

    @SkipCSRF
    @RequestMapping(value = "/candidate/{candidate}/file", method = RequestMethod.POST)
    public ResponseEntity<?> uploadCandidateFile(@PathVariable Candidate candidate, @RequestParam String hash,
                                                 @RequestParam String name, @RequestParam MultipartFile file,
                                                 final HttpServletRequest request) {

        if (Authenticate.getUser() == null) {
            I18N.setLocale(request.getSession(false), new Locale("en", "GB"));
        }

        if (!candidate.verifyHashForEdit(hash)) {
            return sendForbidden();
        }

        if (!(candidate.getSeal() == null)) {
            return sendForbidden();
        }

        try {
            final String contentType = file.getContentType();
            final String filename = chooseFileName(name, file.getOriginalFilename(), contentType);
            ClientFactory.configurationDriveClient().upload(candidate.getDirectoryForCandidateDocuments(), filename,
                    file.getInputStream(), contentType);
        } catch (final IOException e) {
            throw new Error(e);
        }

        return  sendResponse(() -> CandidateAdapter.detailedView(candidate, hash));
    }

    @SkipCSRF
    @RequestMapping(value = "/candidate/{candidate}/file/{file}", method = RequestMethod.DELETE)
    public ResponseEntity<?> deleteCandidateFile(@PathVariable Candidate candidate,
                                                 @PathVariable String file, @RequestParam(required = false) String hash,
                                                 final HttpServletRequest request)
            throws IOException {
        if (Authenticate.getUser() == null) {
            I18N.setLocale(request.getSession(false), new Locale("en", "GB"));
        }

        if (!(Contest.canManageContests() || candidate.verifyHash(hash))) {
            return sendForbidden();
        }

        if (!(candidate.getSeal() == null)) {
            return sendForbidden();
        }

        if (!containsFileId(candidate, file)) {
            return sendBadRequest();
        }

        ClientFactory.configurationDriveClient().deleteFile(file);

        return sendResponse(() -> CandidateAdapter.detailedView(candidate, hash));
    }

    @RequestMapping(value = "/candidate/{candidate}/logs", method = RequestMethod.GET)
    public ResponseEntity<?> getCandidateLogs(@PathVariable Candidate candidate,
                                          final HttpServletRequest request) {
        if (Authenticate.getUser() == null) {
            I18N.setLocale(request.getSession(false), new Locale("en", "GB"));
        }

        if (Contest.canManageContests()) {
            return sendResponse(() -> CandidateAdapter.logView(candidate));
        }

        return sendForbidden();
    }

    private ResponseEntity<?> sendResponse(final Supplier<JsonElement> response) {
        return ResponseEntity.ok(response.get().toString());
    }
    private ResponseEntity<?> sendOk() {
        return ResponseEntity.status(HttpStatus.OK).build();
    }
    private ResponseEntity<?> sendBadRequest() {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
    }
    private ResponseEntity<?> sendForbidden() {
        return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
    }

    private Contest saveContest(String body, Contest contest) {
        final String name;
        final DateTime begin;
        final DateTime end;

        try {
            final DateTimeFormatter formatter = DateTimeFormat.forPattern(Utils.DATE_TIME_PATTERN);
            final JsonElement jsonEl = new JsonParser().parse(body);
            if(!jsonEl.isJsonObject()) {
                return null;
            }
            final JsonObject jsonObj = jsonEl.getAsJsonObject();
            if (!(jsonObj.has("name")
                    && jsonObj.get("name").isJsonPrimitive()
                    && jsonObj.getAsJsonPrimitive("name").isString())) {
                return null;
            }
            name = jsonObj.get("name").getAsString();

            if (!(jsonObj.has("beginDate")
                    && jsonObj.get("beginDate").isJsonPrimitive()
                    && jsonObj.getAsJsonPrimitive("beginDate").isString())) {
                return null;
            }
            begin = formatter.parseDateTime(jsonObj.get("beginDate").getAsString());

            if (!(jsonObj.has("endDate")
                    && jsonObj.get("endDate").isJsonPrimitive()
                    && jsonObj.getAsJsonPrimitive("endDate").isString())) {
                return null;
            }
            end = formatter.parseDateTime(jsonObj.get("endDate").getAsString());

        } catch (JsonParseException | IllegalArgumentException e) {
            return null;
        }

        final Contest resultContest;
        if (contest != null) {
            contest.edit(name, begin, end);
            resultContest = contest;
        } else {
            resultContest = Contest.create(name, begin, end);
        }
        return resultContest;
    }

    private Candidate saveCandidate(Contest contest, String body, String path) {
        final String name;
        final String email;
        final String recaptchaResponse;

        try {
            final JsonElement jsonEl = new JsonParser().parse(body);
            if(!jsonEl.isJsonObject()) {
                return null;
            }
            final JsonObject jsonObj = jsonEl.getAsJsonObject();

            if (!(jsonObj.has("name")
                    && jsonObj.get("name").isJsonPrimitive()
                    && jsonObj.getAsJsonPrimitive("name").isString())) {
                return null;
            }
            name = jsonObj.get("name").getAsString();

            if (!(jsonObj.has("email")
                    && jsonObj.get("email").isJsonPrimitive()
                    && jsonObj.getAsJsonPrimitive("email").isString())) {
                return null;
            }
            email = jsonObj.get("email").getAsString();
            if (!EmailValidator.getInstance().isValid(email)) {
                return null;
            }

            if(!Authenticate.isLogged()) {
                if (!(jsonObj.has("recaptcha")
                        && jsonObj.get("recaptcha").isJsonPrimitive()
                        && jsonObj.getAsJsonPrimitive("recaptcha").isString())) {
                    return null;
                }
                recaptchaResponse = jsonObj.get("recaptcha").getAsString();
                if (!isRecaptchaResponseValid(recaptchaResponse)) {
                    return null;
                }
            }
        } catch (JsonParseException | IllegalArgumentException e) {
            return null;
        }

        Candidate resultCandidate = contest.registerCandidate(name, email, path, messageSource);
        return resultCandidate;
    }

    private boolean containsFileId(final Candidate candidate, final String id) {
        for (final JsonElement je : ClientFactory.configurationDriveClient().listDirectory(candidate.getDirectoryForCandidateDocuments())) {
            final JsonObject jo = je.getAsJsonObject();
            final String fileId = jo.get("id").getAsString();
            if (fileId.equals(id)) {
                return true;
            }
        }
        return false;
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

    private boolean isRecaptchaResponseValid(String response) {
        Form form = new Form();
        form.param("secret", ApplicationsAdmissionsConfiguration.getConfiguration().recaptchaSecretKey());
        form.param("response", response);

        WebTarget target = CLIENT.target("https://www.google.com/recaptcha/api/siteverify");
        String post =
                target.request(MediaType.APPLICATION_JSON_TYPE).post(
                        Entity.entity(form, MediaType.APPLICATION_FORM_URLENCODED_TYPE), String.class);

        JsonObject jo = new JsonParser().parse(post).getAsJsonObject();
        return jo.get("success").getAsBoolean();
    }

}
