package com.collegegroup.processscheduling.Processes;

public class Processs {
    private String pid;
    private String burst;
    private String arrivalTime;
    private String startTime;
    private String endTime;
    private String name;

    public Processs(String pid, String burst, String arrivalTime) {
        this.pid = pid;
        this.burst = burst;
        this.arrivalTime = arrivalTime;
    }

    public String getStartTime() {
        return startTime;
    }

    public String getEndTime() {
        return endTime;
    }

    public String getName() {
        return name;
    }

    public Processs()
    {}

    public String  getPid() {
        return pid;
    }

    public String getBurst() {
        return burst;
    }

    public String getArrivalTime() {
        return arrivalTime;
    }

    public boolean isEmpty()
    {
        return pid.isBlank() || burst.isBlank() || arrivalTime.isBlank();
    }
    public boolean isValid()
    {
        try
        {
            Integer.parseInt(pid);
            Integer.parseInt(burst);
            Integer.parseInt(arrivalTime);
            return true;
        }
        catch (NumberFormatException e)
        {
            return false;
        }
    }
}
