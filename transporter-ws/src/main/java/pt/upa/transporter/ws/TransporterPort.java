package pt.upa.transporter.ws;

import javax.jws.HandlerChain;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.Timer;
import java.util.concurrent.ThreadLocalRandom;
import javax.jws.WebService;


import pt.upa.transporter.ws.BadLocationFault_Exception;
import pt.upa.transporter.ws.BadPriceFault_Exception;
import pt.upa.transporter.ws.JobView;

@WebService(
	    endpointInterface="pt.upa.transporter.ws.TransporterPortType",
	    wsdlLocation="transporter.1_0.wsdl",
	    name="TransporterWebService",
	    portName="TransporterPort",
	    targetNamespace="http://ws.transporter.upa.pt/",
	    serviceName="TransporterService"
)
@HandlerChain(file = "/transporter_handler-chain.xml")
public class TransporterPort implements TransporterPortType {
	
	private String[] _citiesNorth = {"Porto", "Braga", "Viana do Castelo", "Vila Real", "Bragança"};
	private String[] _citiesCenter = {"Lisboa", "Leiria", "Santarém", "Castelo Branco", "Coimbra", "Aveiro", "Viseu", "Guarda"};
	private String[] _citiesSouth = {"Setubal", "Évora", "Portalegre", "Beja", "Faro"};
	
	private int _identifier;
	private String _name;
	private List <Job> _jobs;
	
	public TransporterPort(String name) throws Exception{
		_name = name;
		_jobs = new ArrayList<Job>();
		char number = name.charAt(name.length() - 1);
		_identifier = Character.getNumericValue(number);
	}
	
	
	
	@Override
	public String ping(String name){
		
		System.out.println(name);
		
		String message = "Response to Ping request from " + name;
		
		return message;
	}

	@Override
	public JobView requestJob(String origin, String destination, int price)
			throws BadLocationFault_Exception, BadPriceFault_Exception {
			
		int rndNumber = 0;
		Job job = null;
		
		if(!Arrays.asList(_citiesNorth).contains(origin) && !Arrays.asList(_citiesCenter).contains(origin) 
				&& !Arrays.asList(_citiesSouth).contains(origin)){
			BadLocationFault faultInfo = new BadLocationFault();
			faultInfo.setLocation(origin);
			throw new BadLocationFault_Exception("Transporter doesn't recognize origin!", faultInfo);
		}
		
		if(!Arrays.asList(_citiesNorth).contains(destination) && !Arrays.asList(_citiesCenter).contains(destination) 
				&& !Arrays.asList(_citiesSouth).contains(destination)){
			BadLocationFault faultInfo = new BadLocationFault();
			faultInfo.setLocation(destination);
			throw new BadLocationFault_Exception("Transporter doesn't recognize origin!", faultInfo);
		}
		
		if(isEven(this.getIdentifier()) && Arrays.asList(_citiesSouth).contains(origin)){
			BadLocationFault faultInfo = new BadLocationFault();
			faultInfo.setLocation(origin);
			throw new BadLocationFault_Exception("Transporter doesn't operate in selected origin!", faultInfo);
		}
		if(!isEven(this.getIdentifier()) && Arrays.asList(_citiesNorth).contains(origin)){
			BadLocationFault faultInfo = new BadLocationFault();
			faultInfo.setLocation(origin);
			throw new BadLocationFault_Exception("Transporter doesn't operate in selected origin!", faultInfo);
		}
		
		if(isEven(this.getIdentifier()) && Arrays.asList(_citiesSouth).contains(destination)){
			BadLocationFault faultInfo = new BadLocationFault();
			faultInfo.setLocation(destination);
			throw new BadLocationFault_Exception("Transporter doesn't operate in selected destination!", faultInfo);
		}
		if(!isEven(this.getIdentifier()) && Arrays.asList(_citiesNorth).contains(destination)){
			BadLocationFault faultInfo = new BadLocationFault();
			faultInfo.setLocation(destination);
			throw new BadLocationFault_Exception("Transporter doesn't operate in selected destination!", faultInfo);
		}
		if(price <= 0){
			BadPriceFault faultInfo = new BadPriceFault();
			faultInfo.setPrice(price);
			throw new BadPriceFault_Exception("Invalid Price!", faultInfo);
		}
		if (price > 100)
			return null;
		
		if(price <= 10){
			rndNumber = generateRandom(price-1, 1);
			job = createJob(origin, destination, rndNumber, this.getName());
			_jobs.add(job);
		}
		if(price > 10 && price<=100){
			if((isEven(price) && isEven(this.getIdentifier())) || (!isEven(price) && !isEven(this.getIdentifier()))){
				rndNumber = generateRandom(price - 1, 1);
				job = createJob(origin, destination, rndNumber, this.getName()); 
				_jobs.add(job);
			}
			else {
				rndNumber = generateRandom(price + 200, price + 1);
				job = createJob(origin, destination, rndNumber, this.getName()); 
				_jobs.add(job);
			}
		}
		
		System.out.println("JOB CREATED : "+ job._price);
		job.setJobState(0);
		return job.export();
	}

	@Override
	public JobView decideJob(String id, boolean accept) throws BadJobFault_Exception {
		
		Job job = null;
		
		for(Job j : _jobs){
			if(j.getJobIdentifier().equals(id))
				job = j;	
		}
		
		if(job == null){
			BadJobFault faultInfo = new BadJobFault();
			faultInfo.setId(id);
			throw new BadJobFault_Exception("Job doesn't exist!", faultInfo);
		}
		else{
			if (job.getJobState()!= 0){
				BadJobFault f = new BadJobFault();
				throw new BadJobFault_Exception(id, f);
			}
			else if(accept){
				job.setJobState(2);
				Updater up = new Updater(job,3);
				Timer timer = new Timer();
                timer.schedule(up, ThreadLocalRandom.current().nextLong(1500,5000));
                timer = null;
			}
			else
				job.setJobState(1);
		}
			
		return job.export();
	}

	@Override
	public JobView jobStatus(String id) {
		
		Job job = null;
		
		for(Job j : _jobs){
			if(j.getJobIdentifier().equals(id))
				job = j;
		}
		if (job == null)
			return null;
		return job.export();
	}

	@Override
	public List<JobView> listJobs() {
		List<JobView> jobs = new ArrayList<JobView>();
		for(Job j:_jobs){
			jobs.add(j.export());
		}		
		return jobs;
	}

	@Override
	public void clearJobs() {
		
		this.getJobsList().clear();
		
	}
	
	//Auxiliary Methods
	
	public String getName() {
		return _name;
	}
	
	
	public int getIdentifier(){
		return _identifier;
	}
	
	
	public List<Job> getJobsList(){
		return _jobs;
	}


	public boolean isEven(int num){
		if(num % 2 == 0)
			return true;
		else
			return false;
	}
	
	
	private int generateRandom(int max, int min){
	
		int rndNumber;
		
		Random random = new Random();
		
		rndNumber = random.nextInt((max - min) + 1) + min;
		
		return rndNumber;
	}
	
	
	private Job createJob(String origin, String destination, int randomNumber, String companyName){
		
		Job job = new Job();
		
		job.setCompanyName(companyName);
		job.setJobIdentifier(generateId());
		job.setJobOrigin(origin);
		job.setJobDestination(destination);
		job.setJobPrice(randomNumber);
		job.setJobState(0);
		
		return job;
	}
	
	
	
	
	private static String generateId(){
		
		char[] chars = "abcdefghijklmnopqrstuvwxyz0123456789".toCharArray();
		StringBuilder sb = new StringBuilder();
		Random random = new Random();
		
		for (int i = 0; i < 20; i++) {
		    char c = chars[random.nextInt(chars.length)];
		    sb.append(c);
		}
		
		String output = sb.toString();
		
		return output;
	}
}
