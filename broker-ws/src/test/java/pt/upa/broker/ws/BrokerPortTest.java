package pt.upa.broker.ws;

import org.junit.*;

import mockit.Expectations;
import mockit.Mocked;
import mockit.Verifications;
import pt.upa.transporter.ws.BadLocationFault_Exception;
import pt.upa.transporter.ws.BadPriceFault_Exception;
import pt.upa.transporter.ws.cli.TransporterClient;

import static org.junit.Assert.*;

/**
 *  Unit Test example
 *  
 *  Invoked by Maven in the "test" life-cycle phase
 *  If necessary, should invoke "mock" remote servers 
 */
public class BrokerPortTest {

    // static members
	private static BrokerPort bPort;
	
    // one-time initialization and clean-up

    @BeforeClass
    public static void oneTimeSetUp() throws Exception {
    bPort = new BrokerPort(0, false);
    }

    @AfterClass
    public static void oneTimeTearDown() {
    	bPort=null;

    }


    @Test
    public void testping(@Mocked final TransporterClient cli) throws BadLocationFault_Exception, BadPriceFault_Exception, InvalidPriceFault_Exception, UnavailableTransportFault_Exception, UnavailableTransportPriceFault_Exception, UnknownLocationFault_Exception{
    	
    	BrokerPort brokerTestPort = new BrokerPort(cli);
    	String NAME = "Friend";
    	
  	  new Expectations() {{
            cli.ping(NAME); result = "Response to Ping request from "+NAME;
        }};
        
    	String response = brokerTestPort.ping(NAME);
    	
    	 new Verifications() {{
             cli.ping(NAME); maxTimes = 1;
         }};
         System.out.println(response);
        assertEquals("UPA1: Response to Ping request from Friend\n", response); 
    }
    
    @Test (expected = UnknownTransportFault_Exception.class)
    public void testJobNotKnown() throws UnknownTransportFault_Exception{
    	bPort.viewTransport("ididididid");
    }

    @Test(expected = UnknownLocationFault_Exception.class)
    public void unknownLocationOrigin() throws InvalidPriceFault_Exception, UnavailableTransportFault_Exception, UnavailableTransportPriceFault_Exception, UnknownLocationFault_Exception {
    	bPort.requestTransport("Porto Salvo", "Porto", 20);
    }
    
    @Test(expected = UnknownLocationFault_Exception.class)
    public void unknownLocationDestination() throws InvalidPriceFault_Exception, UnavailableTransportFault_Exception, UnavailableTransportPriceFault_Exception, UnknownLocationFault_Exception {
    	bPort.requestTransport("Porto", "Porto Salvo", 20);
    }
    
    @Test(expected = InvalidPriceFault_Exception.class)
    public void InvalidPriceFault() throws InvalidPriceFault_Exception, UnavailableTransportFault_Exception, UnavailableTransportPriceFault_Exception, UnknownLocationFault_Exception {
    	bPort.requestTransport("Porto", "Setubal", 0);
    }
    
}