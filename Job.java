public class Job {
    
    // The strings and data being sent to the server after the scheduling algorithm is ready to do the next job
    
    public int submitTime;
    public int  jobID;
    public int  runTime;
    public int  core;
    public int  memory;
    public int  disk;

    public Job(int sT, int jID, int rT, int c, int m, int d){
        this.submitTime = sT;
        this.jobID = jID;
        this.runTime = rT;
        this.core = c;
        this.memory = m;
        this.disk = d;
    }
    
    public int gtSubmitTime(){
        return this.submitTime;
    }

    public int gtJobID(){
        return this.jobID;
    }

    public int gtRunTime(){
        return this.runTime;
    }

    public int gtCore(){
        return this.core;
    }
    
    public int gtMemeory(){
        return this.memory;
    }

    public int gtDisk(){
        return this.disk;
    }
}
