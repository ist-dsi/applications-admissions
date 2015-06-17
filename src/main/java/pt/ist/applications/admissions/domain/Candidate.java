package pt.ist.applications.admissions.domain;

import java.util.UUID;

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

}
