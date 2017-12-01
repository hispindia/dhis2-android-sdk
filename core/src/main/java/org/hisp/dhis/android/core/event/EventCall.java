package org.hisp.dhis.android.core.event;

import org.hisp.dhis.android.core.calls.Call;
import org.hisp.dhis.android.core.common.Payload;
import org.hisp.dhis.android.core.data.api.Fields;
import org.hisp.dhis.android.core.data.database.DatabaseAdapter;
import org.hisp.dhis.android.core.data.database.Transaction;
import org.hisp.dhis.android.core.resource.ResourceHandler;
import org.hisp.dhis.android.core.resource.ResourceModel;

import java.util.Date;
import java.util.List;

import retrofit2.Response;

public class EventCall implements Call<Response<Payload<Event>>> {

    private final EventService eventService;
    private final DatabaseAdapter databaseAdapter;
    private final EventQuery eventQuery;
    private final Date serverDate;
    private final ResourceHandler resourceHandler;
    private final EventHandler eventHandler;

    private boolean isExecuted;

    public EventCall(EventService eventService,
            DatabaseAdapter databaseAdapter,
            ResourceHandler resourceHandler,
            EventHandler eventHandler,
            Date serverDate,
            EventQuery eventQuery) {
        this.eventService = eventService;
        this.databaseAdapter = databaseAdapter;
        this.resourceHandler = resourceHandler;
        this.eventHandler = eventHandler;
        this.eventQuery = eventQuery;
        this.serverDate = new Date(serverDate.getTime());

        if (eventQuery != null && eventQuery.getUIds() != null &&
                eventQuery.getUIds().size() > MAX_UIDS) {
            throw new IllegalArgumentException(
                    "Can't handle the amount of events: " + eventQuery.getUIds().size() + ". " +
                            "Max size is: " + MAX_UIDS);
        }
    }

    @Override
    public boolean isExecuted() {
        synchronized (this) {
            return isExecuted;
        }
    }

    @Override
    public Response<Payload<Event>> call() throws Exception {
        synchronized (this) {
            if (isExecuted) {
                throw new IllegalStateException("Already executed");
            }
            isExecuted = true;
        }

        String lastSyncedEvents = resourceHandler.getLastUpdated(ResourceModel.Type.EVENT);

        Response<Payload<Event>> eventsByLastUpdated = eventService.getEvents(
                eventQuery.getOrgUnit(), eventQuery.getProgram(),
                eventQuery.getTrackedEntityInstance(), getSingleFields(),
                Event.lastUpdated.gt(lastSyncedEvents), Event.uid.in(eventQuery.getUIds()),
                Boolean.TRUE, eventQuery.getPage(), eventQuery.getPageSize()).execute();

        if (eventsByLastUpdated.isSuccessful() && eventsByLastUpdated.body().items() != null) {
            Transaction transaction = databaseAdapter.beginNewTransaction();
            try {
                List<Event> events = eventsByLastUpdated.body().items();
                int size = events.size();
                for (int i = 0; i < size; i++) {
                    Event event = events.get(i);
                    eventHandler.handle(event);
                }
                resourceHandler.handleResource(ResourceModel.Type.EVENT, serverDate);
                transaction.setSuccessful();
            } finally {
                transaction.end();
            }
        }
        return eventsByLastUpdated;
    }

    private Fields<Event> getSingleFields() {
        return Fields.<Event>builder().fields(
                Event.uid, Event.created, Event.lastUpdated,
                Event.eventStatus, Event.coordinates, Event.program, Event.programStage,
                Event.organisationUnit, Event.eventDate, Event.completeDate,
                Event.dueDate, Event.deleted, Event.trackedEntityDataValues
        ).build();
    }

    private Fields<Event> getAllFields() {
        return Fields.<Event>builder().fields(
                Event.uid, Event.enrollment, Event.created, Event.lastUpdated,
                Event.eventStatus, Event.coordinates, Event.program, Event.programStage,
                Event.organisationUnit, Event.eventDate, Event.completeDate,
                Event.dueDate, Event.deleted, Event.trackedEntityDataValues
        ).build();
    }
}
