package eu.hradio.core.radiodns;

import java.io.*;
import eu.hradio.core.radiodns.radioepg.programmeinformation.*;
import java.util.*;

public class RadioEpgProgrammeInformation implements Serializable
{
    private static final long serialVersionUID = -4841305937162561477L;
    private List<Schedule> mSchedules;
    
    public RadioEpgProgrammeInformation() {
        this.mSchedules = new ArrayList<Schedule>();
    }
    
    public void addSchedule(final Schedule schedules) {
        this.mSchedules.add(schedules);
    }
    
    public void addSchedules(final List<Schedule> schedules) {
        this.mSchedules.addAll(schedules);
    }
    
    public List<Schedule> getSchedules() {
        return this.mSchedules;
    }
}
