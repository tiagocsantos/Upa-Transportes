package pt.upa.transporter.ws;

class Job {
	private String _origin;
	private String _dest;
	private int _state;
	protected int _price;
	private String _identifier;
	private String _transporter;
    
	private String[] states = {"PROPOSED", "REJECTED", "ACCEPTED", "HEADING", "ONGOING", "COMPLETED"};

	public Job() {		
	}

	public JobView export() {
		JobView job = new JobView();
		job.companyName = _transporter;
		job.jobIdentifier = _identifier;
		job.jobDestination = _dest;
		job.jobOrigin = _origin;
		job.jobPrice = _price;
		job.jobState =  JobStateView.valueOf(states[_state]);
		return job;
	}
	public String getJobIdentifier() {
		return _identifier;
	}
	public void setJobState(int state) {
		_state=state;
	}
	
	public int getJobState(){
		return _state;
	}
	
	public void setCompanyName(String companyName) {
		_transporter=companyName;
	}
	public void setJobIdentifier(String generateId) {
		_identifier=generateId;
	}
	public void setJobOrigin(String origin) {
		_origin=origin;
	}
	public void setJobDestination(String destination) {
		_dest = destination;
	}
	public void setJobPrice(int randomNumber) {
		_price = randomNumber;
	}
	
}
