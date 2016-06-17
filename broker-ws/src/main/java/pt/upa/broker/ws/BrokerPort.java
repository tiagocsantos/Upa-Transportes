package pt.upa.broker.ws;

import static javax.xml.ws.BindingProvider.ENDPOINT_ADDRESS_PROPERTY;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.regex.Pattern;

import javax.jws.WebService;
import javax.xml.registry.JAXRException;
import javax.xml.ws.BindingProvider;
import javax.xml.ws.Endpoint;

import pt.ulisboa.tecnico.sdis.ws.uddi.UDDINaming;
import pt.upa.transporter.ws.BadJobFault_Exception;
import pt.upa.transporter.ws.BadLocationFault_Exception;
import pt.upa.transporter.ws.BadPriceFault_Exception;
import pt.upa.transporter.ws.JobView;
import pt.upa.transporter.ws.cli.TransporterClient;
import javax.jws.HandlerChain;


@WebService(
	endpointInterface="pt.upa.broker.ws.BrokerPortType",
	wsdlLocation="broker.1_0.wsdl",
	name="BrokerWebService",
	portName="BrokerPort",
	targetNamespace="http://ws.broker.upa.pt/",
	serviceName="BrokerService"
)
public class BrokerPort implements BrokerPortType{
	private String[] cidades = {"Porto", "Braga", "Viana do Castelo", "Vila Real", "Bragança", 
								"Lisboa", "Leiria", "Santarém", "Castelo Branco", "Coimbra", "Aveiro", "Viseu", "Guarda",
								"Setubal", "Évora", "Portalegre", "Beja", "Faro"};
	private String[] statesBroker = {"REQUESTED","BUDGETED","FAILED",  "BOOKED",  "HEADING","ONGOING","COMPLETED"};
	private String[] statesTransp = {            "PROPOSED","REJECTED","ACCEPTED","HEADING","ONGOING","COMPLETED"};
	private List<TreeMap<String, Transport>> transportlist;
	private TransporterClient[] upas;
	private int numUPAS;
	private BrokerPortType broker2;
	private boolean mode; //true = secundario , false = primario
	private boolean alive = true;
	private boolean notRunning = true;
	private boolean stopnotifier=false;
	private boolean stopchecker=false;
	private UDDINaming uddiNaming;
	private String url;
	private String name;
	private Endpoint endpoint;
	
	public BrokerPort(int n, boolean initmode) throws Exception{
		mode = initmode;
		numUPAS = n;
		transportlist = new ArrayList<TreeMap<String, Transport>>();
		if (n>0){
		upas = new TransporterClient[n];
			for (int i=0; i<n;i++){
				transportlist.add(new TreeMap<String, Transport>());
				upas[i] = new TransporterClient(i+1);
			}
		}
		if (!mode){
			//connectar com o secundario
			connectSecondary();
			new Thread(new Notifier()).start();
		}
		else {
			new Thread(new Checker()).start();
		}
	}

	
	private void connectSecondary() {
		String uddiURL = "http://localhost:9090";
		String name = "UpaBrokerSecundary";
		System.out.printf("Contacting UDDI at %s%n", uddiURL);
		UDDINaming uddiNaming = null;
		String endpointAddress = null;
		try {
			uddiNaming = new UDDINaming(uddiURL);
			System.out.printf("Looking for '%s'%n", name);
			endpointAddress = uddiNaming.lookup(name);
		} catch (JAXRException e) {
			e.printStackTrace();
		}
		if (endpointAddress == null) {
			System.out.println("Not found!");
			return;
		} else {
			System.out.printf("Found %s%n", endpointAddress);
		}
	
		System.out.println("Creating stub ...");
		BrokerService service = new BrokerService();
		broker2 = service.getBrokerPort();
	
		System.out.println("Setting endpoint address ...");
		BindingProvider bindingProvider = (BindingProvider) broker2;
		Map<String, Object> requestContext = bindingProvider.getRequestContext();
		requestContext.put(ENDPOINT_ADDRESS_PROPERTY, endpointAddress);
	}


	public BrokerPort(TransporterClient t) {
		numUPAS=1;
		upas = new TransporterClient[1];
		upas[0] = t;
	}
	
	@Override
	public String ping(String name) {
		String result="";
		for (int i =0; i<numUPAS;i++){
			result += "UPA"+(i+1)+": " +upas[i].ping(name)+"\n";
		}
		return result;
	}
	


	@Override
	public String requestTransport(String origin, String destination, int price)
			throws InvalidPriceFault_Exception, UnavailableTransportFault_Exception,
			UnavailableTransportPriceFault_Exception, UnknownLocationFault_Exception {
		
		//nao aceitar este tipo de chamadas quando a travalhar em modo secundario
		if (mode)
			return null;
		
		if (!Arrays.asList(cidades).contains(origin)){
			UnknownLocationFault f = new UnknownLocationFault();
			f.setLocation(origin);
			throw new UnknownLocationFault_Exception("Broker não conhece cidade de Origem", f);
		}
		if (!Arrays.asList(cidades).contains(destination)){
			UnknownLocationFault f = new UnknownLocationFault();
			f.setLocation(destination);
			throw new UnknownLocationFault_Exception("Broker não conhece cidade de Destino", f);
		}
		if (price < 1 ){
			InvalidPriceFault f = new InvalidPriceFault();
			f.setPrice(price);
			throw new InvalidPriceFault_Exception("Preço Inválido", f);
		}
		
		JobView job;
		int[] prices = new int[numUPAS];
		Arrays.fill(prices, 99999999);
		Transport[] trans = new Transport[numUPAS];
		
		int min, upaindex, i, rejectsPrice=0, rejectsLocation=0;
		String[] idbroker = new String[numUPAS];
		for (i=0; i<numUPAS;i++){
			trans[i]=null;
			try {
				job = upas[i].requestJob(origin, destination, price);
				if (job!=null) {
					trans[i] = new Transport(origin, destination, job.getJobPrice(), job.getCompanyName(), job.getJobIdentifier());				
				}
				else{
					//so e returnado null se o preco dado for maior que 100
					//se uma retorna null todas vao retornar null
					UnavailableTransportFault f = new UnavailableTransportFault();
					f.setOrigin(origin);
					f.setDestination(destination);
					throw new UnavailableTransportFault_Exception("Invalid price given", f);
				}
			
			}catch(BadPriceFault_Exception e){
				rejectsPrice++;
			}
			catch(BadLocationFault_Exception e){
				rejectsLocation++;
			}
			
		}
		if (rejectsLocation == numUPAS){
			UnavailableTransportFault f = new UnavailableTransportFault();
			f.setOrigin(origin);
			f.setDestination(destination);
			throw new UnavailableTransportFault_Exception("No Transport possible for that Route", f);
		}
		min=9999;
		upaindex = 0;
		for (i=0;i<numUPAS;i++){
			if (trans[i]!=null)
				if (trans[i]._price <min){
					min = trans[i]._price;
					upaindex=i;
				}
		}
		
		if (min > price){
			for (i=0;i<numUPAS;i++){
				if (trans[i]!=null)
					try {
						upas[i].decideJob(trans[i]._id, false);
					} catch (Exception e) {
						//Nao Acontece
					}
			}
			UnavailableTransportPriceFault f = new UnavailableTransportPriceFault();
			f.setBestPriceFound(min);
			throw new UnavailableTransportPriceFault_Exception("No Transport for that price", f);
		}
		
		for (i=0;i<numUPAS; i++){
			if (i==upaindex)
				try {
					upas[i].decideJob(trans[i]._id, true);
					
					int id = transportlist.get(i).size();
					idbroker[i] = "UPA"+(i+1)+"-"+id;
					transportlist.get(i).put(idbroker[i], trans[i]);
					
				} catch (BadJobFault_Exception e) {}
			else if (trans[i]!=null)
				try {
					upas[i].decideJob(trans[i]._id, false);
				} catch (BadJobFault_Exception e) {}
		}
		if (!mode)
			broker2.addTransport(trans[upaindex].export2(), idbroker[upaindex]);	
		return idbroker[upaindex];
	}


	@Override
	public TransportView viewTransport(String id) throws UnknownTransportFault_Exception {
		//nao aceitar este tipo de chamadas quando a travalhar em modo secundario
		if (mode)
			return null;
		
		if ((id == null) || (!Pattern.matches("UPA[0-9]-[0-9+]", id)) ){
			UnknownTransportFault f = new UnknownTransportFault();
			f.setId(id);
			throw new UnknownTransportFault_Exception("Transporte Desconhecido", f); 
		}
		char upaindexchar = id.charAt(3);
		if (!Character.isDigit(upaindexchar)){
			UnknownTransportFault f = new UnknownTransportFault();
			f.setId(id);
			throw new UnknownTransportFault_Exception("Transporte Desconhecido", f);
		}
			
		int upaindex = Character.getNumericValue(upaindexchar)-1;
		
		if (upaindex < 0 | upaindex > 2){
			UnknownTransportFault f = new UnknownTransportFault();
			f.setId(id);
			throw new UnknownTransportFault_Exception("Transporte Desconhecido", f);
		}
		Transport t = transportlist.get(upaindex).get(id);
	
		TransportView tv = updateTransport(id, upaindex).export(id);
		
		if (tv == null){
			UnknownTransportFault f = new UnknownTransportFault();
			f.setId(id);
			throw new UnknownTransportFault_Exception("Transporte Desconhecido", f);
		}
			
		
		return tv;
	}

	@Override
	public List<TransportView> listTransports() {
		//nao aceitar este tipo de chamadas quando a travalhar em modo secundario
		if (mode)
			return null;
		
		List<TransportView> list = new ArrayList<TransportView>();
		Transport transport;
		for (int i=0;i<numUPAS;i++){
			for (String id : transportlist.get(i).keySet()){
				try {
				transport = updateTransport(id, i);
				list.add(transport.export(id));
				}
				catch(Exception e){
					
				}
			}
		}
		return list;
	}

	@Override
	public void clearTransports() {
		//nao aceitar este tipo de chamadas quando a travalhar em modo secundario
		if (mode)
			return;
		
		for (int i=0;i<numUPAS;i++){
			upas[i].clearJobs();
			transportlist.get(i).clear();
		}
		if (!mode)
			broker2.cleanTransports();
	}
	
	private Transport updateTransport(String id, int upaindex) throws UnknownTransportFault_Exception{
		Transport t = transportlist.get(upaindex).get(id);
		if (t == null){
			UnknownTransportFault f = new UnknownTransportFault();
			f.setId(id);
			throw new UnknownTransportFault_Exception("Transporte Desconhecido", f); 
		}
		String jobstatus = upas[upaindex].updateStatus(t._id);
		if (jobstatus == null){
			UnknownTransportFault f2 = new UnknownTransportFault();
			f2.setId(id);
			throw new UnknownTransportFault_Exception("Transporte Desconhecido", f2); 
		}
		int i = Arrays.asList(statesTransp).indexOf(jobstatus);
		t._state = statesBroker[i+1];
		return t;
	}

	@Override
	public void cleanTransports() {
		for (int i =0; i<numUPAS;i++)
			transportlist.get(i).clear();
		System.out.println("Secundary Clean");
	}

	@Override
	public void addTransport(TransportView transport, String id) {
		System.out.println("Secundary add transport: "+id);
		Transport t = Transport.fromTransportView(transport);
		transportlist.get(t.getUPAindex()).put(id, t);
	
	}
		
	@Override
	public void imAlive() {
		System.out.println("Primary is Alive");		
		alive = true;
		notRunning = false;
	}
		
	public void registerAssets(UDDINaming uddin, Endpoint endPoint, String gname, String gurl){
		uddiNaming = uddin;
		endpoint = endPoint;
		name = gname;
		url = gurl;
	}
	
	public void stopWS(){
		stopnotifier = true;
		stopchecker = true;
		endpoint.stop();
		System.out.printf("Stopped %s%n", url);
		try {
			uddiNaming.unbind(name);
			System.out.printf("Deleted '%s' from UDDI%n", name);
		} catch (JAXRException e) {
			e.printStackTrace();
		}
	}
	
	public void replace(){
		mode = false;
		String uddiURL = "http://localhost:9090";
		url = "http://localhost:8080/broker-ws/endpoint";
		name = "UpaBroker";
		
		endpoint = Endpoint.create(this);
		System.out.printf("Starting %s%n", url);
		endpoint.publish(url);
		System.out.printf("Publishing '%s' to UDDI at %s%n", name, uddiURL);
		try{
			uddiNaming = new UDDINaming(uddiURL);
			uddiNaming.rebind(name, url);
		}
		catch (Exception e){
			System.out.printf("Caught exception: %s%n", e);
			e.printStackTrace();
		}
	}
	
	class Notifier implements Runnable {

		@Override
		public void run() {
			while(true){
				if (stopnotifier)
					break;
				broker2.imAlive();
				try {
					Thread.sleep(3000);
				} 
				catch (InterruptedException e) {
					e.printStackTrace();
				}
			}	
		}
	}
	
	class Checker implements Runnable {

		@Override
		public void run() {
			while (true){
				if (notRunning){
					//continue;
				}
				else if (stopchecker)
					break;
				
				else if (alive)
					alive = false;
							
				else {
					System.out.println("Primary is Dead");
					stopWS();
					replace();
					System.out.println("Now acting as Primary");
					break;
				}
				
				try {
					Thread.sleep(3200);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
		
	}
}
