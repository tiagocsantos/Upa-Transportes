package pt.upa.transporter.ws.it;

import org.junit.*;

import mockit.Expectations;
import mockit.Mocked;
import mockit.Verifications;
import pt.upa.transporter.ws.BadJobFault_Exception;
import pt.upa.transporter.ws.BadLocationFault_Exception;
import pt.upa.transporter.ws.BadPriceFault_Exception;
import pt.upa.transporter.ws.JobView;
import pt.upa.transporter.ws.TransporterPortType;
import pt.upa.transporter.ws.cli.TransporterClient;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.List;

/**
 *  Integration Test example
 *  
 *  Invoked by Maven in the "verify" life-cycle phase
 *  Should invoke "live" remote servers 
 */
public class TransporterClientIT {
    // static members
	private static TransporterClient bPort;
    // one-time initialization and clean-up

    @BeforeClass
    public static void oneTimeSetUp() throws Exception {
    bPort = new TransporterClient("http://localhost:9090", "UpaTransporter1");
    }

    @AfterClass
    public static void oneTimeTearDown() {
    	bPort.clearJobs();
    	bPort=null;

    }
    
    @Test
    public void testPing() throws Exception{
    	String name = "Friend";
    	String response = bPort.ping(name);
    	assertEquals("Response to Ping request from " + name, response);
    }
    
    @Test(expected = BadLocationFault_Exception.class)
    public void testRequest() throws BadLocationFault_Exception, BadPriceFault_Exception{
    	bPort.requestJob("Rio Maior", "Leiria", 20);
    }
    @Test(expected = BadLocationFault_Exception.class)
    public void testRequest2() throws BadLocationFault_Exception, BadPriceFault_Exception{
    	bPort.requestJob("Leiria", "Rio Maior", 20);
    }
    
    @Test(expected = BadPriceFault_Exception.class)
    public void testRequest3() throws BadLocationFault_Exception, BadPriceFault_Exception{
    	bPort.requestJob("Leiria", "Leiria", 0);
    }
    
    @Test
    public void testRequestSucess() throws BadLocationFault_Exception, BadPriceFault_Exception{
    	JobView job = bPort.requestJob("Leiria", "Leiria", 20);
    	assertNotNull(job);
    }
    
    @Test
    public void viewJob() throws BadLocationFault_Exception, BadPriceFault_Exception{
    	String id = bPort.requestJob("Leiria", "Leiria", 20).getJobIdentifier();
    	JobView job = bPort.jobStatus(id);
    	assertEquals(job.getJobState().value(), "PROPOSED");
    }
    
    @Test 
    public void decideJobREJECT() throws BadLocationFault_Exception, BadPriceFault_Exception, BadJobFault_Exception{
    	String id = bPort.requestJob("Leiria", "Leiria", 20).getJobIdentifier();
    	bPort.decideJob(id, false);
    	JobView job = bPort.jobStatus(id);
    	assertEquals(job.getJobState().value(), "REJECTED");
    }
    
    @Test 
    public void decideJobACCEPT() throws BadLocationFault_Exception, BadPriceFault_Exception, BadJobFault_Exception{
    	String id = bPort.requestJob("Leiria", "Leiria", 20).getJobIdentifier();
    	bPort.decideJob(id, true);
    	JobView job = bPort.jobStatus(id);
    	assertEquals(job.getJobState().value(), "ACCEPTED");
    }

}