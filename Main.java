package com.company;

import java.io.*;
import java.lang.*;
import java.util.*;
import java.util.*;



public class Main {
    static void SJP_NP(Process processes[],boolean priority)
    {

        if(!priority)Arrays.sort(processes,new SortbySJP_NON());
        else Arrays.sort(processes,new SortbyPriority_NON());
        int done=0;
        int s=0,e=0;
        while(done< processes.length)
            for(int i=0;i< processes.length;i++)
            {
                if(!processes[i].visited) {
                    done++;
                    e+=processes[i].burst;
                    if(s<processes[i].getArrival())
                    {
                        s=processes[i].Arrival;
                        e=s+processes[i].burst;
                    }
                    System.out.println(processes[i].ID+" "+s+" "+e);
                    s=e;
                    Process.curr += processes[i].burst;
                    processes[i].visited=true;
                    if(!priority)Arrays.sort(processes,new SortbySJP_NON());
                    else Arrays.sort(processes,new SortbyPriority_NON());
                    break;
                }
            }
    }


    static ArrayList<Process> SJP_PREE(Process processes[],boolean priority)
    {
        Comparator<Process> comparator ;
        if(priority) {
            comparator = Comparator.comparing(Process::getArrival)
                    .thenComparing(Process::getPriority).thenComparing(Process::getID);
        }
        else comparator= Comparator.comparing(Process::getArrival).thenComparing(Process::getBurst).thenComparing(Process::getID);

        PriorityQueue<Process> pq = new PriorityQueue<>(comparator);

        for (int i = 0; i < processes.length; i++) {
            pq.add(processes[i]);
            /*if (!p.isCompleted && p.arrivalTime <= currentTime) {
                pq.add(p);
            }*/
        }

        int currentTime = 0,e=0;
        ArrayList<Process> ganttChart = new ArrayList<>();

        while(!pq.isEmpty()){
            Process process = pq.poll();
            if(process.burst+currentTime<(pq.peek().Arrival)){
                process.end=process.burst+currentTime;
                process.start=currentTime;
                currentTime=pq.peek().Arrival;
            }
            else{
                process.end=pq.peek().Arrival;
                process.start=currentTime;
                process.burst-=(process.end-process.start);
                currentTime=process.end;
                process.Arrival=currentTime;
                for (int i = 0; i < processes.length; i++) {
                    if(currentTime>processes[i].Arrival) processes[i].Arrival=currentTime;
                }
                pq.add(process);
            }
            ganttChart.add(process);
        }
        return ganttChart;
    }
    public static void main(String[] args) {
        // write your code here
        //1 0 8 0
        //2 1 4 0
        //3 2 9 0
        //4 3 5 0
        Scanner sc=new Scanner(System.in);
        int n,t;
        n=sc.nextInt();
        t= sc.nextInt();

        Process [] processes=new Process[n];
        for (int i=0;i<n;i++)
        {
            Process process =new Process(sc.nextInt(),sc.nextInt(),sc.nextInt(),sc.nextInt());
            processes[i]=process;
        }

        if(t==1)
        {
            Arrays.sort(processes,new SortbyFCFS());
            int s=0,e=0;
            for (int i=0;i<n;i++)
            {
                e+=processes[i].burst;
                System.out.println(processes[i].ID+" "+s+" "+e);
                s=e;
            }
        }
        else if(t==2)
        {
            SJP_NP(processes,false);
        }
        else if(t==4)
        {
            SJP_NP(processes,true);
        }
        else if (t == 3 || t == 5) {
            ArrayList<Process> gc = null;
            if(t==3) gc=SJP_PREE(processes,false);
            if(t==5) gc=SJP_PREE(processes,true);

            int last_id=0,start=0;
            for (int i=0;i< gc.size()-1;i++)
            {
                System.out.println(gc.get(i).ID+" "+gc.get(i).start+" "+gc.get(i).end);
            }
            System.out.println(gc.get(gc.size()-1).ID + " "+start+" "+ gc.size());
        }


    }
}

class Process {
    int ID,Arrival,burst,priority,start,end,time_completed;
    boolean visited=false;
    public static int curr = 0;
    public Process(int id, int arrival, int brust, int i) {
        this.ID=id;
        this.Arrival=arrival;
        this.burst=brust;
        this.priority=i;
    }

    public int getID() {
        return ID;
    }

    public int getArrival() {
        return Arrival;
    }

    public int getBurst() {
        return burst;
    }

    public int getPriority() {
        return priority;
    }

    public boolean isCompleted(){
        return this.time_completed==this.burst;
    }
}
class SortbySJP_NON implements Comparator<Process> {
    // Used for sorting in ascending order of
    // roll number
    public int compare(Process a, Process b)
    {
        if(a.Arrival - b.Arrival ==0)
        {
            return a.burst-b.burst;
        }
        else if (a.Arrival - Process.curr <=0 && b.Arrival- Process.curr <=0)
        {
            return a.burst-b.burst;
        }
        else return a.Arrival- b.Arrival;
    }
}
class SortbyFCFS implements Comparator<Process> {
    // Used for sorting in ascending order of
    // roll number
    public int compare(Process a, Process b)
    {
        return a.Arrival - b.Arrival;
    }
}
class SortbyPriority_NON implements Comparator<Process> {
    // Used for sorting in ascending order of
    // roll number
    public int compare(Process a, Process b)
    {
        if(a.Arrival - b.Arrival ==0)
        {
            return a.priority-b.priority;
        }
        else if (a.Arrival - Process.curr <=0 && b.Arrival- Process.curr <=0)
        {
            return a.priority-b.priority;
        }
        else return a.Arrival- b.Arrival;
    }
}


