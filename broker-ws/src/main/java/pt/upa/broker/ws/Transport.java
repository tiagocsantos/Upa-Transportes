package pt.upa.broker.ws;

public class Transport {
	protected String _origin;
    protected String _destination;
    protected int _price;
    protected String _transporterCompany;
    protected String _state;   
    protected String _id;
    
	public Transport(String origin, String destination, int price, String transporter, String id) {
		_origin = origin;
		_destination = destination;
		_price = price;
		_state = "REQUESTED";
		_transporterCompany = transporter;
		_id = id;
		
	}

	public int getUPAindex() {
		return Character.getNumericValue(_transporterCompany.charAt(_transporterCompany.length()-1))-1;
	}

	public TransportView export(String id) {
		TransportView job = new TransportView();
		job.destination = _destination;
		job.origin = _origin;
		job.transporterCompany = _transporterCompany;
		job.price = _price;
		job.state = TransportStateView.fromValue(_state);
		job.id = id;
		return job;
	}
	
	public static Transport fromTransportView(TransportView tv){
		Transport newt = new Transport(tv.getOrigin(), tv.getOrigin(), tv.getPrice(), 
									   tv.getTransporterCompany(), tv.getId());
		return newt;
	}

	public TransportView export2() {
		TransportView job = new TransportView();
		job.setDestination(_destination);
		job.setOrigin(_origin);
		job.setId(_id);
		job.setPrice(_price);
		job.setTransporterCompany(_transporterCompany);
		return job;
	}
}