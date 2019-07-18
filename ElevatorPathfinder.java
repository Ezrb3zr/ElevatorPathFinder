import java.util.ArrayList;
import java.util.HashMap;
import java.util.Stack;
import java.util.StringTokenizer;
import java.util.TreeMap;

import com.sun.tools.javac.tree.TreeMaker;

public class ElevatorPathfinder{

    HashMap<String, Boolean> visitedNode = new HashMap<>();
    int[][] elevatorArray;
    String[] elevatorNames;
    Stack<String> pathStack = new Stack<>();

    public static void main(String[] args){
        ElevatorPathfinder elevatorPathfinder = new ElevatorPathfinder();
        int[][] elevatorStates = {                            
                                    {1,3,2,6}, //t=0
                                    {4,3,2,1}, //t=1
                                    {3,3,6,1}, //t=2
                                    {2,4,6,4}, //t=3
                                    {2,2,6,5}  //t=4
                                };
        String[] elevatorNames = {"A", "B", "C", "D"};
        String finalDestination = "5-4";
        String startingElevator = "A";
        elevatorPathfinder.findElevatorPath(elevatorStates, startingElevator, finalDestination, elevatorNames);
    }

    private class ConnectionNode{
        public ConnectionNode(String elevatorName, int floorNumber, int timeStamp){
            this.name = elevatorName;
            this.floor = floorNumber;
            this.time = timeStamp;
        }
        public String name = "";
        public int floor;
        public int time;

        public void print(){
            System.out.println("Name:" + name + " | Floor:" + floor + " | Time:" + time);
        }
    }

    // A Hashmap that functions
    private HashMap<String, ArrayList<ConnectionNode>> connectionNodesMap = new HashMap<>();


    public void findElevatorPath(int[][] elevatorStates, String startingElevator, String finalDestination, String[] names) {
       
        StringTokenizer destinationStringTokenizer = new StringTokenizer(finalDestination, "-");
        int destFloor = Integer.parseInt(destinationStringTokenizer.nextToken());
        int destTime = Integer.parseInt(destinationStringTokenizer.nextToken());

        elevatorArray = elevatorStates;
        elevatorNames = names;
        Boolean initialCheck = false;
        for(int index = 0; index < elevatorArray[destTime-1].length; index++ ){
            if(elevatorArray[destTime-1][index] == destFloor){
                initialCheck = true;
            }
        }
        if(initialCheck == true) {
            buildConnectionNodes();
            if(depthFirstSearchElevators(startingElevator, destTime, 0, destFloor) == false){
                System.out.println("No Solution Found");
            } else {
                int len = pathStack.size();
                System.out.print("PATH FOUND : ");
                for(int i = 0; i < len; i++){
                    System.out.print(pathStack.pop());
                }
            }
        } else {
            System.out.println("No Solution Found! No Elevator goes there at that time!");
        }
        pathStack = new Stack<>();

    }


    // This is the hardest part. Trying to transform a 2D array of ints into a list of connected nodes, (essentially a graph). The time complexity on this is immense, unfortunately.
    // In an Ideal world, I'd be able to transform this into an Adjacency matrix. Unfortunately the time-dimension on it makes it essentially a 3 dimensional Graph.
    public void buildConnectionNodes(){
        connectionNodesMap.clear();
        for(int time=0 ; time < elevatorArray.length; time++){
            HashMap<Integer, Boolean> floorVisited = new HashMap<>(); //reset the record of floors visited already
            for(int elevatorIndex = 0; elevatorIndex < elevatorArray[time].length; elevatorIndex++){
                ArrayList<String> connectionStack = new ArrayList<>(); //reset the connectionStack for each time iteration
                if(floorVisited.containsKey(elevatorArray[time][elevatorIndex]) == false){
                    Boolean firstTime = true;
                    //ddddd
                    for(int k = elevatorIndex + 1; k <  elevatorArray[time].length; k++){
                        if(elevatorArray[time][elevatorIndex] == elevatorArray[time][k]){ // If we find two Elevators on the same floor
                            System.out.println("Elevators " + elevatorNames[elevatorIndex] + " and " + elevatorNames[k] + " have a connection at floor " +  elevatorArray[time][elevatorIndex] + " @ t=" +time);
                            if(firstTime){
                                firstTime = false;
                                connectionStack.add(elevatorNames[elevatorIndex]);  //Push the initial Elevator once onto the ConnectionStack, so we don't repeat ourselves
                            }
                            connectionStack.add(elevatorNames[k]); // Push the second Elevator onto the ConnectionStack
                        }
                    }
                    
                    floorVisited.put(elevatorArray[time][elevatorIndex], true); //we've gathered all the connected Elevators on this floor, so we ignore any elevators that are on the floor when parsing through the rest.
                    ArrayList<ConnectionNode> tempConnectionNodeList = new ArrayList<>(); 
                    if(connectionStack.size() > 2){  // Make a Hashmap where <ElevatorName> is the key, and all connection nodes are listed underneath it. i.e. ConnectionNodesMap<'A'> might have Connection Nodes (name=B, floor=3, t=3), and (name=B, floor=2, t=5)
                        System.out.println("ConnectionStack for Floor " + elevatorArray[time][elevatorIndex] + " @ t=" + time + " is : " + connectionStack);
                        for(int index = 0; index < connectionStack.size(); index++){
                            for(int index2 = index+1; index2 < connectionStack.size(); index2++){
                                tempConnectionNodeList = new ArrayList<>();
                                if(connectionNodesMap.containsKey(connectionStack.get(index))){
                                    tempConnectionNodeList = connectionNodesMap.get(connectionStack.get(index));
                                }
                                tempConnectionNodeList.add(new ConnectionNode(connectionStack.get(index2), elevatorArray[time][elevatorIndex], time));
                                connectionNodesMap.put(connectionStack.get(index), tempConnectionNodeList);
                                tempConnectionNodeList = new ArrayList<>();

                                if(connectionNodesMap.containsKey(connectionStack.get(index2))){
                                    tempConnectionNodeList = connectionNodesMap.get(connectionStack.get(index2));
                              
                                }
                                tempConnectionNodeList.add(new ConnectionNode(connectionStack.get(index), elevatorArray[time][elevatorIndex], time));
                                connectionNodesMap.put(connectionStack.get(index2), tempConnectionNodeList);
                            }
                        }
                    } else if(connectionStack.size() == 2) {
                        System.out.println("ConnectionStack for Floor " + elevatorArray[time][elevatorIndex] + " @ t=" + time + " is : " + connectionStack);
                        tempConnectionNodeList = new ArrayList<>();
                        if(connectionNodesMap.containsKey(connectionStack.get(0))){
                            tempConnectionNodeList = connectionNodesMap.get(connectionStack.get(0));
                        }
                        tempConnectionNodeList.add(new ConnectionNode(connectionStack.get(1), elevatorArray[time][elevatorIndex], time));
                        System.out.print("ConnectionNodeList for " + connectionStack.get(0) + " just added > ");
                        tempConnectionNodeList.get(tempConnectionNodeList.size()-1).print();
                        connectionNodesMap.put(connectionStack.get(0), tempConnectionNodeList);
                        tempConnectionNodeList = new ArrayList<>();
                                            
                        if(connectionNodesMap.containsKey(connectionStack.get(1))){
                            tempConnectionNodeList = connectionNodesMap.get(connectionStack.get(1));   
                        }
                        tempConnectionNodeList.add(new ConnectionNode(connectionStack.get(0), elevatorArray[time][elevatorIndex], time));
                        System.out.print("ConnectionNodeList for " + connectionStack.get(1) + " just added > ");
                        tempConnectionNodeList.get(tempConnectionNodeList.size()-1).print();
                        connectionNodesMap.put(connectionStack.get(1), tempConnectionNodeList);
                    }
                }
            }
        }
    }

    //A Depth-first-search. Recursive. Very efficient.
    public Boolean depthFirstSearchElevators(String elevatorName, int destTime, int currentTime, int destFloor){
        
        if(currentTime == 0){
            visitedNode.clear(); // reset visited nodes in the very beginning
        }
        visitedNode.put(elevatorName, true);
        if(currentTime > destTime){
            return false;
        }
        for(int i = 0; i < elevatorNames.length; i++){
            if(elevatorNames[i].equals(elevatorName)){ //String comparison needed here
                if(elevatorArray[destTime-1][i] == destFloor){
                    for(int iter = 0; iter < destTime-currentTime-1; iter++){
                        pathStack.push(elevatorNames[i]);
                    }
                    if(currentTime==0){
                        pathStack.push(elevatorName);
                    }
                    return true;
                }
                if(connectionNodesMap.containsKey(elevatorName)){
                    
                for(ConnectionNode node : connectionNodesMap.get(elevatorName)){
                    if(visitedNode.containsKey(node.name) == false){
                        if(depthFirstSearchElevators(node.name, destTime, node.time, destFloor) == true){
                            
                            pathStack.push(node.name);
                            for(int iter = 0; iter < node.time-currentTime-1; iter++){
                                pathStack.push(elevatorName);
                            }
                            if(currentTime==0){
                                pathStack.push(elevatorName);
                            }
                            System.out.println("Current PathStack = " + pathStack);
                            return true;
                        }
                    }
                }
            }
            return false;
            }
        }
        return false;
    }


    TreeMap<ConnectionNode> elevatorTree = new TreeMap<>();
    



}