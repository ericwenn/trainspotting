import TSim.*;

import java.util.concurrent.Semaphore;

public class Lab1 {


    public Lab1(Integer speed1, Integer speed2) {
        TSimInterface tsi = TSimInterface.getInstance();
        Semaphore semaphore = new Semaphore(1, true);
        Track track = new Track();

        Train train_1 = new Train(1, speed1, track);
        Train train_2 = new Train(2, speed2, track);


        Thread train_1_thread = new Thread(train_1);
        Thread train_2_thread = new Thread(train_2);

        train_1_thread.start();
        train_2_thread.start();
    }
}
