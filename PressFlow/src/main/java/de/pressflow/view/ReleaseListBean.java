package de.pressflow.view;

import de.pressflow.model.PressRelease;
import de.pressflow.model.ReleaseStatus;
import de.pressflow.service.PressReleaseService;
import de.pressflow.service.WorkflowService;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import java.io.Serializable;
import java.util.List;

/**
 * View-scoped Bean für die Liste aller Pressemitteilungen.
 * Bietet einen Filter nach Status.
 */
@Named
@ViewScoped
public class ReleaseListBean implements Serializable {

    private static final long serialVersionUID = 1L;

    @Inject
    private PressReleaseService pressReleaseService;

    @Inject
    private WorkflowService workflowService;

    @Inject
    private LoginBean loginBean;

    // Ausgewählter Statusfilter; null bedeutet "alle"
    private ReleaseStatus statusFilter;

    // Liefert die Pressemitteilungen, ggf. gefiltert nach Status
    public List<PressRelease> getReleases() {
        if (statusFilter == null) {
            return pressReleaseService.findAll();
        }
        return pressReleaseService.findByStatus(statusFilter);
    }

    // Alle Statuswerte für die Filterauswahl
    public ReleaseStatus[] getStatuses() {
        return ReleaseStatus.values();
    }

    // Darf die angemeldete Person neue Pressemitteilungen anlegen?
    public boolean isCanCreate() {
        return workflowService.canCreate(loginBean.getCurrentUser());
    }

    public ReleaseStatus getStatusFilter() {
        return statusFilter;
    }

    public void setStatusFilter(ReleaseStatus statusFilter) {
        this.statusFilter = statusFilter;
    }
}
