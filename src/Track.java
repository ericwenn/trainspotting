import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Semaphore;

/**
 * Model representing track given in the problem statement.
 *   \  S1  /
 *    \    /    Station one semaphore
 *     \  /
 *      v
 *      |
 *      |       Critical section one semaphore
 *      |
 *   --------
 *   |      |
 *   |      |   Overtake semaphore
 *   |      |
 *   --------
 *      |
 *      |       Critical section two semaphore
 *      |
 *     / \
 *    /   \     Station two semaphore
 *   /  S2 \
 */
public class Track {




    Node<Sensor> northStationPreferredStop;
    Node<Sensor> northStationParallellStop;
    Node<Sensor> northStationCross;
    Node<Sensor> northStationSwitch;
    Node<Sensor> overtakeEastSwitch;
    Node<Sensor> overtakeWestSwitch;
    Node<Sensor> southStationSwitch;
    Node<Sensor> southStationPreferredStop;
    Node<Sensor> southStationParallellStop;

    Semaphore northStationSemaphore         = new Semaphore(1, true);
    Semaphore northStationCrossSemaphore           = new Semaphore(1, true);
    Semaphore eastCriticalSectionSemaphore  = new Semaphore(1, true);
    Semaphore overtakeSemaphore             = new Semaphore(1, true);
    Semaphore westCriticalSectionSemaphore  = new Semaphore(1, true);
    Semaphore southStationSemaphore         = new Semaphore(1, true);

    public Track() {

        int x = 17;
        int y = 3;
        Node<Sensor> currentNode = new Node<>(new Sensor(x,y));
        northStationPreferredStop = currentNode;


        for(;x > 6; x--) {
            currentNode = currentNode.addChild(new Sensor(x, y));
        }

        x = 6;
        y = 3;
        for(;y < 8; y++) {
            currentNode = currentNode.addChild(new Sensor(x,y));
        }


        x = 7;
        y = 7;
        for(;x< 18; x++) {
            currentNode = currentNode.addChild(new Sensor(x,y));
            if( x == 8 && y == 7) {
                northStationCross = currentNode;
            }
        }

        northStationSwitch = currentNode;


        x = 17;
        y = 8;

        Node<Sensor> backTrackNode = currentNode.addParent( new Sensor(x,y));

        for(;x>7;x--) {
            backTrackNode = backTrackNode.addParent( new Sensor( x, y));
        }

        x = 8;
        y = 7;
        for(;y>4;y--) {
            if( y == 7 ) {
                backTrackNode = backTrackNode.addParent(northStationCross);
            } else {
                backTrackNode = backTrackNode.addParent( new Sensor(x,y));
            }
        }

        x = 9;
        y = 5;
        for(;x<18;x++) {
            backTrackNode = backTrackNode.addParent(new Sensor(x,y));
        }
        northStationParallellStop = backTrackNode;

        x = 18;
        y = 7;

        // until 19,7
        for(;x<20;x++) {
            currentNode = currentNode.addChild( new Sensor(x,y));
        }
        x = 19;


        // until 19,9
        for(;y<10;y++) {
            currentNode = currentNode.addChild( new Sensor(x,y));
        }
        y = 9;

        // until 15,9
        for(;x> 14; x--) {
            currentNode = currentNode.addChild( new Sensor(x,y));
        }
        overtakeEastSwitch = currentNode;
        Node<Sensor> commonParent = currentNode;

        // until 4,9
        for(;x>3;x--) {
            currentNode = currentNode.addChild( new Sensor(x,y));
        }
        overtakeWestSwitch = currentNode;

        // until 4,10
        x = 4;
        y = 10;
        backTrackNode = currentNode.addParent( new Sensor(x,y));

        // until 15,10
        for(;y<16;y++) {
            backTrackNode = backTrackNode.addParent( new Sensor(x,y));
        }
        // until 15,9
        backTrackNode.addParent(commonParent);


        x = 3;
        y = 9;

        // until 1,9
        for(; x > 0; x--) {
            currentNode = currentNode.addChild( new Sensor(x,y));
        }

        // until 1,11
        for(;y < 12; y++) {
            currentNode = currentNode.addChild( new Sensor(x,y));
        }
        // until 3,11
        for(;x < 4;x++) {
            currentNode = currentNode.addChild( new Sensor(x,y));
        }
        southStationSwitch = currentNode;
        backTrackNode = currentNode;

        x = 4;
        y = 11;

        // until 18,11
        for(;x < 19; x++) {
            currentNode = currentNode.addChild( new Sensor(x,y));
        }

        southStationPreferredStop = currentNode;



        x = 3;
        y = 12;
        currentNode = backTrackNode;

        // until 3,13
        for(; y < 14; y++) {
            currentNode = currentNode.addChild( new Sensor(x,y));
        }

        // until 18,13
        for(; x < 19;x++) {
            currentNode = currentNode.addChild( new Sensor(x,y));
        }

        southStationParallellStop = currentNode;



    }


    protected static class Sensor {
        protected int posX;
        protected int posY;
        private Sensor(int x, int y) {
            posX = x;
            posY = y;
        }
    }

    protected static class Node<T> {
        private T data;

        private List<Node<T>> parents;
        private List<Node<T>> children;

        private  Node( T data ) {

            this.data = data;

            this.parents = new ArrayList<>();
            this.children = new ArrayList<>();

        }

        protected List<Node<T>> parents() {
            return parents;
        }
        protected List<Node<T>> children() {
            return children;
        }
        protected T data() {
            return data;
        }

        private Node<T> addChild( T data) {
            Node<T> child = new Node( data );
            return addChild(child);
        }

        private Node<T> addParent( T data ) {

            Node<T> parent = new Node(data);
            return addParent(parent);
        }

        private Node<T> addChild( Node<T> child ) {
            child.parents.add(this);
            this.children.add(child);
            return child;
        }

        private Node<T> addParent( Node<T> parent) {
            parent.children.add(this);
            this.parents.add(parent);
            return parent;
        }
    }



}
