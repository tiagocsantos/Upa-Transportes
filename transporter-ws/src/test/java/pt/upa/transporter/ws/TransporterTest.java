package pt.upa.transporter.ws;

import org.junit.*;
import static org.junit.Assert.*;

import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 *  Unit Test example
 *  
 *  Invoked by Maven in the "test" life-cycle phase
 *  If necessary, should invoke "mock" remote servers 
 */
public class TransporterTest {

    // static members
	private static TransporterPort port;
	private static TransporterPort port2;
	private String id;
	private JobView job;
	
    // one-time initialization and clean-up

    @BeforeClass
    public static void oneTimeSetUp() throws Exception {
    	port = new TransporterPort("UpaTransporter1");
    	port2 = new TransporterPort("UpaTransporter2");
    }

    @AfterClass
    public static void oneTimeTearDown() {
    	port = null;
    	port2 = null;
    }


    // members


    // initialization and clean-up for each test

    @Before
    public void setUp() {
    	try {
			id = port.requestJob("Setubal", "Faro", 20).getJobIdentifier();
		} catch (Exception e){
			System.out.printf("Caught " + e);
		}
    }

    @After
    public void tearDown() {
    	port.clearJobs();
    }


    // tests

    @Test
    public void testping() {
    	String arg = "Cenas";
    	String response = port.ping(arg);
    	assertEquals("Response to Ping request from " + arg, response);
    }
    
    
    @Test(expected = BadPriceFault_Exception.class)
    public void testNewJobBadPrice() throws BadLocationFault_Exception, BadPriceFault_Exception{
    	port.requestJob("Setubal", "Évora", -50);
    }
    
    
    @Test(expected = BadLocationFault_Exception.class)
    public void testNewJobBadLocation1() throws BadLocationFault_Exception, BadPriceFault_Exception{
    	port.requestJob("Rio Maior", "Évora", 10);
    }

    
    @Test(expected = BadLocationFault_Exception.class)
    public void testNewJobBadLocation2() throws BadLocationFault_Exception, BadPriceFault_Exception{
    	port.requestJob("Évora","Rio Maior", 10);
    }
    
    
    @Test(expected = BadLocationFault_Exception.class)
    public void testRequestBadLocation3() throws BadLocationFault_Exception, BadPriceFault_Exception{
    	job = port.requestJob("Lisboa", "Porto", 7);
    }
    
    
    @Test(expected = BadLocationFault_Exception.class)
    public void testRequestBadLocation4() throws BadLocationFault_Exception, BadPriceFault_Exception{
    	job = port.requestJob("Porto", "Lisboa", 7);
    }
    
    
    @Test
    public void testRequestJobUnderPriced() throws BadLocationFault_Exception, BadPriceFault_Exception{
    	job = port.requestJob("Lisboa", "Leiria", 7);
    	assertTrue(job.getJobPrice() < 7);
    }
    
    
    @Test
    public void testRequestJobOverPricedEvenNumber() throws BadLocationFault_Exception, BadPriceFault_Exception{
    	job = port.requestJob("Lisboa", "Leiria", 42);
    	assertTrue(job.getJobPrice() > 42);
    }
    
    
    @Test
    public void testRequestJobUnderPricedOddNumber() throws BadLocationFault_Exception, BadPriceFault_Exception{
    	job = port.requestJob("Lisboa", "Leiria", 43);
    	assertTrue(job.getJobPrice() < 43);
    }
    
    
    @Test
    public void testRequestJobUnderPricedEvenNumber() throws BadLocationFault_Exception, BadPriceFault_Exception{
    	job = port2.requestJob("Lisboa", "Leiria", 42);
    	assertTrue(job.getJobPrice() < 42);
    }
    
    
    @Test
    public void testRequestJobOddPricedOddNumber() throws BadLocationFault_Exception, BadPriceFault_Exception{
    	job = port2.requestJob("Lisboa", "Leiria", 43);
    	assertTrue(job.getJobPrice() > 43);
    }
    
    
    @Test
    public void testDecideJobAccept() throws BadJobFault_Exception{
		String response = port.decideJob(id, true).getJobState().value();
		assertEquals("ACCEPTED", response);
    }
    
    
    @Test
    public void testDecideJobReject() throws BadJobFault_Exception{
		String response = port.decideJob(id, false).getJobState().value();
		assertEquals("REJECTED", response);
    }
    
    
    @Test(expected = BadJobFault_Exception.class)
    public void testDecideJobWrongId() throws BadJobFault_Exception{
		String response = port.decideJob("coisa", true).getJobState().value();
    }
    
    
    @Test
    public void testJobStatus(){
    	String response = port.jobStatus(id).getJobState().value();
    	assertEquals("PROPOSED", response);
    }
    
    
    @Test
    public void testListJobs(){
    	List<JobView> list = port.listJobs();
    	assertEquals(1, list.size());
    }
    
    @Test
    public void testClearJobs(){
    	port.clearJobs();
    	List<JobView> list = port.listJobs();
    	assertEquals(0, list.size());
    }
    
}