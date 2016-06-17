package pt.upa.broker.ws.it;

import org.junit.*;

import mockit.Expectations;
import mockit.Mocked;
import mockit.Verifications;
import pt.upa.broker.ws.BrokerPortType;
import pt.upa.broker.ws.InvalidPriceFault_Exception;
import pt.upa.broker.ws.TransportView;
import pt.upa.broker.ws.UnavailableTransportFault_Exception;
import pt.upa.broker.ws.UnavailableTransportPriceFault_Exception;
import pt.upa.broker.ws.UnknownLocationFault_Exception;
import pt.upa.broker.ws.UnknownTransportFault_Exception;
import pt.upa.broker.ws.cli.BrokerClient;

import static org.junit.Assert.*;

import java.util.List;

/**
 *  Integration Test example
 *  
 *  Invoked by Maven in the "verify" life-cycle phase
 *  Should invoke "live" remote servers 
 */
public class BrokerClientIT {

    // static members
	static BrokerClient brokerCli;

    // one-time initialization and clean-up

    @BeforeClass
    public static void oneTimeSetUp() {
    	brokerCli = new BrokerClient("http://localhost:9090");

    }

    @AfterClass
    public static void oneTimeTearDown() {
    	brokerCli = null;
    }


    // members


    // initialization and clean-up for each test

    @Before
    public void setUp() {
    }

    @After
    public void tearDown() {
    	brokerCli.clearTransports();
    }


    // tests

    @Test
    public void testPing() throws Exception {
    	String name = "Friend";
    	String response = brokerCli.ping(name);
    	assertEquals(response, "UPA1: Response to Ping request from Friend\nUPA2: Response to Ping request from Friend\n");
    }
    
    @Test
    public void testList(){
    	List<TransportView> response = brokerCli.listTransports();
    	assertEquals(response.size(), 0);	
    }
    
    @Test(expected = InvalidPriceFault_Exception.class)
    public void testeBadPrice() throws InvalidPriceFault_Exception, UnavailableTransportFault_Exception, UnavailableTransportPriceFault_Exception, UnknownLocationFault_Exception{
    	brokerCli.requestTransport("Faro", "Beja", 0);
    }
    
    @Test(expected = UnavailableTransportFault_Exception.class)
    public void testNoTransportPossible() throws InvalidPriceFault_Exception, UnavailableTransportFault_Exception, UnavailableTransportPriceFault_Exception, UnknownLocationFault_Exception{
    	brokerCli.requestTransport("Porto", "Beja", 20);
    }
    
    @Test(expected = UnavailableTransportPriceFault_Exception.class)
    public void testUnavailableTransport() throws InvalidPriceFault_Exception, UnavailableTransportFault_Exception, UnavailableTransportPriceFault_Exception, UnknownLocationFault_Exception{
    	brokerCli.requestTransport("Faro", "Beja", 20);
    }
    
    @Test(expected = UnavailableTransportPriceFault_Exception.class)
    public void testUnavailableTransport2() throws InvalidPriceFault_Exception, UnavailableTransportFault_Exception, UnavailableTransportPriceFault_Exception, UnknownLocationFault_Exception{
    	brokerCli.requestTransport("Porto", "Braga", 21);
    }
    
    @Test
    public void testClear() throws InvalidPriceFault_Exception, UnavailableTransportFault_Exception, UnavailableTransportPriceFault_Exception, UnknownLocationFault_Exception{
    	brokerCli.requestTransport("Porto", "Braga",20);
    	brokerCli.requestTransport("Beja", "Faro",21);
    	brokerCli.clearTransports();
    	List<TransportView> response = brokerCli.listTransports();
    	assertEquals(response.size(), 0);
    }
    
    @Test(expected = UnknownTransportFault_Exception.class)
    public void testViewT() throws UnknownTransportFault_Exception{
    	brokerCli.viewTransport("NAOEXISTE");
    }
    
    @Test
    public void testViewT2() throws InvalidPriceFault_Exception, UnavailableTransportFault_Exception, UnavailableTransportPriceFault_Exception, UnknownLocationFault_Exception, UnknownTransportFault_Exception{
    	String id = brokerCli.requestTransport("Porto", "Braga",20);
    	assertEquals(id, "UPA2-0");
    	String state =brokerCli.viewTransport("UPA2-0").getState().value();
    	assertEquals(state, "BOOKED");
    	
    }
    
    

}