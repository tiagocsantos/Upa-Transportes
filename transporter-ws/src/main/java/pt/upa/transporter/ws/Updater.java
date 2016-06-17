package pt.upa.transporter.ws;

import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ThreadLocalRandom;

public class Updater extends TimerTask {
	
	Job _job;
	int index;
	
	public Updater(Job job, int i){
		_job= job;
		index = i;
	}

	@Override
	public void run() {
		long random;
		Timer timer = new Timer();
		_job.setJobState(index);
		random = ThreadLocalRandom.current().nextLong(1500, 5000);
		if (index!=5)
			timer.schedule(new Updater(_job, index+1), random);
		else
			timer.cancel();
	}

}
