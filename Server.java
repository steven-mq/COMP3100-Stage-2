public class Server {

	// The strings and data being sent to the server after the scheduling algorithm is ready to do the next job
	
	public String jobtype; 
	public int serverlimit;
	public int serverBootTime;
	public float serverHourlyRate;
	public int coreCountNum;
	public int memoryAmount;
	public int diskSp;
	public int serverId;
	public String serverStatus;
	public int severStartTime;
	public int serverWaitingJob;
	public int serverRunningJob;

	public Server(String type, int lt, int bT, float hR, int cC, int m, int d) {
		this.jobtype = type;
		this.serverlimit = lt;
		this.serverBootTime = bT;
		this.serverHourlyRate = hR;
		this.coreCountNum = cC;
		this.memoryAmount = m;
		this.diskSp = d;
	}


	public Server(String type, int id, String state, int sT, int cC, int m, int d, int wJ, int rJ) {

		this.jobtype = type;

		this.serverId = id;
		this.serverStatus = state;
		this.severStartTime = sT;

		this.coreCountNum = cC;
		this.memoryAmount = m;
		this.diskSp = d;

		this.serverWaitingJob = wJ;
		this.serverRunningJob = rJ;

	}


	public int gtserverID() {
		return this.serverId;
	}

	public int gtseverWaitJob() {
		return this.serverWaitingJob;
	}

	public String gtseverState() {
		return this.serverStatus;
	}

	public int gtseverStartTime() {
		return this.severStartTime;
	}

	public int gtserverRunTime() {
		return this.serverRunningJob;
	}

	public String gtjobType() {
		return this.jobtype;
	}

	public int gtserverLimit() {
		return this.serverlimit;
	}

	public int gtDiskSp() {
		return this.diskSp;
	}

	public int gtBootupTimeAm() {
		return this.serverBootTime;
	}

	public int gtCoresNum() {
		return this.coreCountNum;
	}

	public int gtMemAm() {
		return this.memoryAmount;
	}

	public Float gtserverHourlyRate() {
		return this.serverHourlyRate;
	}

	public void printData() {
		System.out.println(this.jobtype + " " + this.serverlimit + " " + this.serverBootTime + " " + this.serverHourlyRate + " "
				+ this.coreCountNum + " " + this.memoryAmount + " " + this.diskSp);
	}

}
