package pt.ist.applications.admissions.domain;

import java.util.UUID;

import org.fenixedu.bennu.ApplicationsAdmissionsConfiguration;
import org.fenixedu.bennu.core.domain.Bennu;
import org.fenixedu.bennu.core.domain.User;
import org.fenixedu.bennu.core.groups.DynamicGroup;
import org.fenixedu.bennu.core.security.Authenticate;
import org.joda.time.DateTime;
import org.joda.time.Interval;

import pt.ist.applications.admissions.util.Utils;
import pt.ist.drive.sdk.ClientFactory;
import pt.ist.fenixframework.Atomic;

public class Contest extends Contest_Base {

    public Contest(final String contestName, final DateTime beginDate, final DateTime endDate) {
        super();
        edit(contestName, beginDate, endDate);
        setBennu(Bennu.getInstance());
        setDirectory(ClientFactory.configurationDriveClient().createDirectory(contestName,
                ApplicationsAdmissionsConfiguration.getConfiguration().contestDir()));
    }

    @Atomic
    public static Contest create(final String contestName, final DateTime beginDate, final DateTime endDate) {
        return canManageContests() ? new Contest(contestName, beginDate, endDate) : null;
    }

    @Atomic
    public Candidate registerCandidate(final String name) {
        return canManageContests() ? new Candidate(this, name) : null;
    }

    public static boolean canManageContests() {
        final User user = Authenticate.getUser();
        return DynamicGroup.get("managers").isMember(user) || DynamicGroup.get("applications_admissions_manager").isMember(user);
    }

    public boolean isInPeriod() {
        final DateTime begin = getBeginDate();
        final DateTime end = getEndDate();
        return begin != null && end != null && new Interval(begin, end).contains(new DateTime());
    }

    @Atomic
    public void undispose() {
        setViewHash(null);
    }

    @Atomic
    public void generateHash() {
        setViewHash(UUID.randomUUID().toString());
    }

    public boolean verifyHashForView(String hash) {
        return Utils.match(getViewHash(), hash);
    }

    @Atomic
    public void delete() {
        getCandidateSet().forEach(Candidate::delete);
        setBennu(null);
        deleteDomainObject();
    }

    @Atomic
    public void edit(final String contestName, final DateTime beginDate, final DateTime endDate) {
        setContestName(contestName);
        setBeginDate(beginDate);
        setEndDate(endDate);
    }

}
