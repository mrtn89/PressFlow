package de.pressflow.service;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import de.pressflow.model.PressRelease;
import de.pressflow.model.ReleaseStatus;
import de.pressflow.model.Role;
import de.pressflow.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Prüft die Berechtigungsregeln des Freigabeprozesses.
 * Die Regeln kommen ohne Datenbank aus und lassen sich daher direkt testen.
 */
class WorkflowServiceTest {

    private final WorkflowService workflow = new WorkflowService();

    private User author;
    private User otherEditor;
    private User reviewer;
    private User otherReviewer;
    private User spokesperson;
    private User admin;
    private PressRelease release;

    @BeforeEach
    void setUp() {
        author = user(1L, Role.REDAKTEUR);
        otherEditor = user(2L, Role.REDAKTEUR);
        reviewer = user(3L, Role.FACHPRUEFER);
        otherReviewer = user(4L, Role.FACHPRUEFER);
        spokesperson = user(5L, Role.PRESSESPRECHER);
        admin = user(6L, Role.ADMINISTRATOR);

        release = new PressRelease();
        release.setId(10L);
        release.setAuthor(author);
        release.setReviewer(reviewer);
    }

    @Test
    void onlyEditorsAndAdminsCanCreate() {
        assertTrue(workflow.canCreate(author));
        assertTrue(workflow.canCreate(admin));
        assertFalse(workflow.canCreate(reviewer));
        assertFalse(workflow.canCreate(spokesperson));
    }

    @Test
    void authorCanEditDraftButNotOtherEditors() {
        release.setStatus(ReleaseStatus.ENTWURF);
        assertTrue(workflow.canEdit(release, author));
        assertFalse(workflow.canEdit(release, otherEditor));
        assertFalse(workflow.canEdit(release, reviewer));
    }

    @Test
    void contentIsLockedDuringReviewAndAfterApproval() {
        release.setStatus(ReleaseStatus.IN_PRUEFUNG);
        assertFalse(workflow.canEdit(release, author));
        release.setStatus(ReleaseStatus.FREIGEGEBEN);
        assertFalse(workflow.canEdit(release, author));
        assertFalse(workflow.canEdit(release, admin));
    }

    @Test
    void authorCanEditAgainDuringRevision() {
        release.setStatus(ReleaseStatus.UEBERARBEITUNG);
        assertTrue(workflow.canEdit(release, author));
        assertTrue(workflow.canSubmit(release, author));
    }

    @Test
    void onlyAssignedReviewerCanReview() {
        release.setStatus(ReleaseStatus.IN_PRUEFUNG);
        assertTrue(workflow.canReview(release, reviewer));
        assertFalse(workflow.canReview(release, otherReviewer));
        assertFalse(workflow.canReview(release, author));
    }

    @Test
    void onlySpokespersonCanApproveAfterReview() {
        release.setStatus(ReleaseStatus.IN_PRUEFUNG);
        assertFalse(workflow.canApprove(release, spokesperson));

        release.setStatus(ReleaseStatus.ZUR_FREIGABE);
        assertTrue(workflow.canApprove(release, spokesperson));
        assertTrue(workflow.canApprove(release, admin));
        assertFalse(workflow.canApprove(release, reviewer));
        assertFalse(workflow.canApprove(release, author));
    }

    @Test
    void revisionCanBeRequestedByReviewerAndSpokesperson() {
        release.setStatus(ReleaseStatus.IN_PRUEFUNG);
        assertTrue(workflow.canRequestRevision(release, reviewer));

        release.setStatus(ReleaseStatus.ZUR_FREIGABE);
        assertTrue(workflow.canRequestRevision(release, spokesperson));
        assertFalse(workflow.canRequestRevision(release, author));
    }

    @Test
    void onlyApprovedReleasesCanBeExported() {
        release.setStatus(ReleaseStatus.ZUR_FREIGABE);
        assertFalse(workflow.canExport(release));
        release.setStatus(ReleaseStatus.FREIGEGEBEN);
        assertTrue(workflow.canExport(release));
    }

    @Test
    void forbiddenStepThrowsWorkflowException() {
        release.setStatus(ReleaseStatus.ZUR_FREIGABE);
        assertThrows(WorkflowException.class, () -> workflow.approve(release, author, null));
        assertThrows(WorkflowException.class, () -> workflow.confirmReview(release, reviewer, null));
    }

    @Test
    void revisionRequiresReason() {
        release.setStatus(ReleaseStatus.IN_PRUEFUNG);
        assertThrows(WorkflowException.class, () -> workflow.requestRevision(release, reviewer, "  "));
    }

    private User user(Long id, Role role) {
        User user = new User();
        user.setId(id);
        user.setRole(role);
        user.setFullName("Person " + id);
        return user;
    }
}
