package io.jenkins.plugins.view.calendar;

import hudson.Util;
import hudson.model.*;
import io.jenkins.plugins.view.calendar.util.DateUtil;

import java.util.*;

public class CalendarEvent {
    private final TopLevelItem item;
    private final Run build;
    private final Calendar start;
    private final Calendar end;
    private final Calendar nextRun;
    private final CalendarEventType type;
    private final String title;
    private final String url;
    private final long duration;
    private final boolean future;

    @SuppressWarnings("PMD.NullAssignment")
    public CalendarEvent(final TopLevelItem item, final Calendar start, final long durationInMillis) {
        this.item = item;
        this.build = null;
        this.future = true;
        this.type = CalendarEventType.FUTURE;
        this.title = item.getFullDisplayName();
        this.url = item.getUrl();
        this.duration = durationInMillis;
        this.start = start;
        this.end = initEnd();
        this.nextRun = null;
    }

    public CalendarEvent(final TopLevelItem item, final Run build) {
        this.item = item;
        this.build = build;
        this.future = false;
        this.type = CalendarEventType.fromResult(build.getResult());
        this.title = build.getFullDisplayName();
        this.url = build.getUrl();
        this.duration = build.getDuration();
        this.start = Calendar.getInstance();
        this.start.setTimeInMillis(build.getStartTimeInMillis());
        this.end = initEnd();
        this.nextRun = new CronJobService().getNextRun(item);
    }

    private Calendar initEnd() {
        // duration needs to be at least 1sec otherwise
        // fullcalendar will not properly display the event
        final long dur = (this.duration < 1000) ? 1000 : this.duration;
        final Calendar end = Calendar.getInstance();
        end.setTime(this.start.getTime());
        end.add(Calendar.SECOND, (int) (dur / 1000));
        return end;
    }

    public TopLevelItem getItem() {
        return this.item;
    }

    public Calendar getStart() {
        return start;
    }

    public String getStartAsDateTime() {
        return DateUtil.formatDateTime(getStart());
    }

    public Calendar getEnd() {
        return this.end;
    }

    public String getEndAsDateTime() {
        return DateUtil.formatDateTime(getEnd());
    }

    public CalendarEventType getType() {
        return this.type;
    }

    public String getTypeAsClassName() {
        return "event-" + type.name().toLowerCase(Locale.ENGLISH);
    }

    public String getUrl() {
        return this.url;
    }

    public String getTitle() {
        return this.title;
    }

    public long getDuration() {
        return this.duration;
    }

    public boolean isFuture() {
        return this.future;
    }

    public String getTimestampString() {
        final long now = new GregorianCalendar().getTimeInMillis();
        final long difference = Math.abs(now - start.getTimeInMillis());
        return Util.getPastTimeString(difference);
    }

    public String getDurationString() {
        return Util.getTimeSpanString(duration);
    }

    public String getIconClassName() {
        if (isFuture()) {
            return ((AbstractProject) this.item).getBuildHealth().getIconClassName();
        }
        switch (getType()) {
            case SUCCESS:
                return "icon-blue";
            case UNSTABLE:
                return "icon-yellow";
            case FAILURE:
                return "icon-red";
            default:
                return "icon-grey";
        }
    }

    public List<Build> getLastBuilds() {
        if (item instanceof Job) {
            return ((Job)item).getLastBuildsOverThreshold(5, Result.ABORTED);
        }
        return new ArrayList<>();
    }

    public Run getPreviousBuild() {
        if (build != null) {
            return build.getPreviousBuild();
        }
        return null;
    }

    public Run getNextBuild() {
        if (build != null) {
            return build.getNextBuild();
        }
        return null;
    }

    public Run getBuild() {
        return build;
    }

    public Job getJob() {
        if (build != null) {
            return build.getParent();
        }
        return null;
    }

    public Calendar getNextRun() {
        return nextRun;
    }
}
