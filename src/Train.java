import TSim.CommandException;
import TSim.SensorEvent;
import TSim.TSimInterface;

import java.util.concurrent.Semaphore;

import static java.lang.Thread.sleep;

public class Train implements Runnable {

    private final int trainId;
    private final int initialSpeed;
    private final Track track;
    private TSimInterface tSimInterface;
    private int direction;

    private boolean northStationPreferred = false;
    private boolean overtakePreferred = false;
    private boolean southStationPreferred = false;

    public Train(int trainId, int initialSpeed, Track track) {
        this.trainId = trainId;
        this.initialSpeed = initialSpeed;
        this.track = track;
        this.tSimInterface = TSimInterface.getInstance();
        this.direction = 1;
        if( this.trainId == 1) {
            northStationPreferred = true;
        } else {
            southStationPreferred = true;
        }
    }

    @Override
    public void run() {
        Track.Sensor _switch;
        int switchDir;
        Semaphore criticalStationSem;
        Semaphore criticalSectionSem;

        int stoppingDistance;

        if( initialSpeed < 11 ) {
            stoppingDistance = 2;
        } else if(initialSpeed < 17 ) {
            stoppingDistance = 3;
        } else if(initialSpeed < 23){
            stoppingDistance = 4;
        } else if(initialSpeed < 27){
            stoppingDistance = 5;
        } else {
            throw new IllegalArgumentException("Maximum speed is 26");
        }

        int i;


        Track.Node<Track.Sensor> stoppingNode;
        Track.Node<Track.Sensor> releaseNode;

        try {
            criticalStationSem = isGoingToStationTwo() ? track.northStationSemaphore : track.southStationSemaphore;
            criticalStationSem.acquire();

            boolean isOnPreferred;

            while(true) {
                tSimInterface.setSpeed(trainId, fullSpeed());



                if( isGoingToStationTwo() ) {
                    stoppingNode = track.northStationCross.parents().get( northStationPreferred ? 0 : 1);
                    for( i = 1; i < stoppingDistance; i++) {
                        stoppingNode = stoppingNode.parents().get(0);
                    }

                    skipUntil(stoppingNode.data(), false);
                    System.out.println("Train "+trainId+" started to break...");
                    tSimInterface.setSpeed( trainId, 0);

                    track.northStationCrossSemaphore.acquire();
                    System.out.println("Train "+trainId+" acquired semaphore, accelerating...");
                    tSimInterface.setSpeed(trainId, fullSpeed());

                    Track.Node<Track.Sensor> releasingNode = track.northStationCross.children().get( northStationPreferred ? 0 : 1);
                    skipUntil(releasingNode.data(), true);
                    System.out.println("Train "+trainId+" passed semaphore, releasing...");
                    track.northStationCrossSemaphore.release();
                }






                Track.Node<Track.Sensor> stationSwitch = isGoingToStationTwo() ? track.northStationSwitch : track.southStationSwitch;

                if( isGoingToStationTwo() ) {
                    stoppingNode = stationSwitch.parents().get( northStationPreferred ? 0 : 1);
                    for( i = 1; i < stoppingDistance; i++) {
                        stoppingNode = stoppingNode.parents().get(0);
                    }
                    releaseNode = stationSwitch.children().get(0);

                } else {
                    stoppingNode = stationSwitch.children().get( southStationPreferred ? 0 : 1);
                    for( i = 1; i < stoppingDistance; i++) {
                        stoppingNode = stoppingNode.children().get(0);
                    }
                    releaseNode = stationSwitch.parents().get(0);
                }
                skipUntil(stoppingNode.data(), false);
                System.out.println("Train "+trainId+" started to break...");
                tSimInterface.setSpeed(trainId, 0);

                criticalSectionSem = isGoingToStationTwo() ? track.eastCriticalSectionSemaphore : track.westCriticalSectionSemaphore;
                criticalSectionSem.acquire();
                System.out.println("Train "+trainId+" acquired critical section");

                _switch = stationSwitch.data();
                if (isGoingToStationTwo()) {
                    switchDir = northStationPreferred ? TSimInterface.SWITCH_RIGHT : TSimInterface.SWITCH_LEFT;
                } else {
                    switchDir = southStationPreferred ? TSimInterface.SWITCH_LEFT : TSimInterface.SWITCH_RIGHT;
                }

                tSimInterface.setSwitch(_switch.posX, _switch.posY, switchDir);
                tSimInterface.setSpeed(trainId, fullSpeed());

                skipUntil(releaseNode.data(), true);

                if( isGoingToStationTwo() && northStationPreferred || !isGoingToStationTwo() && southStationPreferred) {
                    criticalStationSem.release();
                }
                System.out.println("Train "+trainId+" releases station...");





                Track.Node<Track.Sensor> overtakeSwitch = isGoingToStationTwo() ? track.overtakeEastSwitch : track.overtakeWestSwitch;
                _switch = overtakeSwitch.data();
                if( isGoingToStationTwo() ) {
                    stoppingNode = overtakeSwitch.parents().get(0);
                    for( i = 1; i < stoppingDistance; i++ ) {
                        stoppingNode = stoppingNode.parents().get(0);
                    }
                } else {
                    stoppingNode = overtakeSwitch.children().get(0);
                    for( i = 1; i < stoppingDistance; i++ ) {
                        stoppingNode = stoppingNode.children().get(0);
                    }
                }
                //System.out.println("Overtake:");
                //skipUntil(stoppingNode.data(), false);
                System.out.println("Train "+1+" starts approaching overtake...");

                if( track.overtakeSemaphore.availablePermits() == 0) {
                    System.out.println("Train "+trainId+" takes parallell overtake route...");
                    switchDir = isGoingToStationTwo() ? TSimInterface.SWITCH_LEFT : TSimInterface.SWITCH_RIGHT;
                    overtakePreferred = false;
                } else {
                    System.out.println("Train "+trainId+" takes preferred overtake route...");
                    track.overtakeSemaphore.acquire();
                    overtakePreferred = true;
                    switchDir = isGoingToStationTwo() ? TSimInterface.SWITCH_RIGHT : TSimInterface.SWITCH_LEFT;
                }

                tSimInterface.setSwitch( _switch.posX, _switch.posY, switchDir);

                if( overtakePreferred ) {
                    releaseNode = isGoingToStationTwo() ? overtakeSwitch.children().get(0) : overtakeSwitch.parents().get(0);
                } else {
                    releaseNode = isGoingToStationTwo() ? overtakeSwitch.children().get(1) : overtakeSwitch.parents().get(1);
                }
                skipUntil( releaseNode.data(), true);
                criticalSectionSem.release();





                overtakeSwitch = isGoingToStationTwo() ? track.overtakeWestSwitch : track.overtakeEastSwitch;
                _switch = overtakeSwitch.data();
                if ( isGoingToStationTwo() ) {
                    stoppingNode = overtakeSwitch.parents().get( overtakePreferred ? 0 : 1);
                    for( i = 1; i < stoppingDistance; i++) {
                        stoppingNode = stoppingNode.parents().get(0);
                    }
                    releaseNode = overtakeSwitch.children().get(0);
                } else {
                    stoppingNode = overtakeSwitch.children().get( overtakePreferred ? 0 : 1);
                    for( i = 1; i < stoppingDistance; i++) {
                        stoppingNode = stoppingNode.children().get(0);
                    }
                    releaseNode = overtakeSwitch.parents().get(0);
                }

                skipUntil( stoppingNode.data(), false);
                tSimInterface.setSpeed(trainId,0);
                System.out.println("Train "+trainId+" approaching end of overtake");
                criticalSectionSem = isGoingToStationTwo() ? track.westCriticalSectionSemaphore : track.eastCriticalSectionSemaphore;

                criticalSectionSem.acquire();
                System.out.println("Train "+trainId+" acquired critical section");
                tSimInterface.setSpeed(trainId, fullSpeed());

                // Sätt switch
                if( isGoingToStationTwo() ) {
                    switchDir = overtakePreferred ? TSimInterface.SWITCH_LEFT : TSimInterface.SWITCH_RIGHT;
                } else {
                    switchDir = overtakePreferred ? TSimInterface.SWITCH_RIGHT : TSimInterface.SWITCH_LEFT;
                }
                tSimInterface.setSwitch( _switch.posX, _switch.posY, switchDir);

                skipUntil(releaseNode.data(), true);
                if( overtakePreferred ) {
                    track.overtakeSemaphore.release();
                    System.out.println("Train "+trainId+" released overtake");
                }




                stationSwitch = isGoingToStationTwo() ? track.southStationSwitch : track.northStationSwitch;
                _switch = stationSwitch.data();



                criticalStationSem = isGoingToStationTwo() ? track.southStationSemaphore : track.northStationSemaphore;

                if( criticalStationSem.availablePermits() == 0) {
                    switchDir = isGoingToStationTwo() ? TSimInterface.SWITCH_RIGHT : TSimInterface.SWITCH_LEFT;
                    if( isGoingToStationTwo()) {
                        southStationPreferred = false;
                        releaseNode = stationSwitch.children().get(1);
                    } else {
                        northStationPreferred = false;
                        releaseNode = stationSwitch.parents().get(1);
                    }
                } else {
                    criticalStationSem.acquire();
                    switchDir = isGoingToStationTwo() ? TSimInterface.SWITCH_LEFT : TSimInterface.SWITCH_RIGHT;
                    if( isGoingToStationTwo()) {
                        southStationPreferred = true;
                        releaseNode = stationSwitch.children().get(0);
                    } else {
                        northStationPreferred = true;
                        releaseNode = stationSwitch.parents().get(0);
                    }
                }
                tSimInterface.setSwitch( _switch.posX, _switch.posY, switchDir);
                skipUntil( releaseNode.data(), true);
                criticalSectionSem.release();




                if( !isGoingToStationTwo()) {
                    stoppingNode = track.northStationCross.children().get( northStationPreferred ? 0 : 1);
                    for( i = 1; i < stoppingDistance; i++) {
                        stoppingNode = stoppingNode.children().get(0);
                    }

                    skipUntil(stoppingNode.data(), false);
                    System.out.println("Train "+trainId+" started to break...");
                    tSimInterface.setSpeed( trainId, 0);

                    track.northStationCrossSemaphore.acquire();
                    System.out.println("Train "+trainId+" acquired semaphore, accelerating...");
                    tSimInterface.setSpeed(trainId, fullSpeed());

                    Track.Node<Track.Sensor> releasingNode = track.northStationCross.parents().get( northStationPreferred ? 0 : 1);
                    skipUntil(releasingNode.data(), true);
                    System.out.println("Train "+trainId+" passed semaphore, releasing...");
                    track.northStationCrossSemaphore.release();
                }





                Track.Node<Track.Sensor> stationStop;
                if( isGoingToStationTwo() ) {
                    stationStop = southStationPreferred ? track.southStationPreferredStop : track.southStationParallellStop;
                } else {
                    stationStop = northStationPreferred ? track.northStationPreferredStop : track.northStationParallellStop;
                }

                if( isGoingToStationTwo() ) {
                    stoppingNode = stationStop.parents().get(0);
                    for( i = 1; i < stoppingDistance; i++) {
                        stoppingNode = stoppingNode.parents().get(0);
                    }
                } else {
                    stoppingNode = stationStop.children().get(0);
                    for( i = 1; i < stoppingDistance; i++) {
                        stoppingNode = stoppingNode.children().get(0);
                    }
                }

                skipUntil( stoppingNode.data(), false);
                tSimInterface.setSpeed( trainId, 0);

                sleep(3000);
                direction = -1*direction;


            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    private void skipUntil( Track.Sensor sensor, boolean untilPass) throws CommandException, InterruptedException {
        skipUntil(sensor.posX, sensor.posY, untilPass);
    }
    private void skipUntil(int posX, int posY, boolean untilPass) throws CommandException, InterruptedException {
        System.out.println("Train: " + trainId + " skipped sensors until: x:" + posX + " y:" + posY);
        int status = untilPass ? SensorEvent.ACTIVE : SensorEvent.INACTIVE;
        SensorEvent se;
        do {
            se = tSimInterface.getSensor(trainId);
        } while( (se.getXpos() != posX || se.getYpos() != posY) || se.getStatus() == status);
    }


    private boolean isGoingToStationTwo() {
        return trainId == 1 && direction == 1 || trainId == 2 && direction == -1;
    }

    private int fullSpeed() {
        return this.direction * this.initialSpeed;
    }


    private long waitingTime() {
        return 1000 + 20 * initialSpeed;
    }
}
