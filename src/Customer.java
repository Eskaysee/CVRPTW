public class Customer implements Comparable<Customer> {
    private final int id;
    private final double[] coord;
    private final double demand;
    private final double readyTime;
    private final double dueTime;
    private final double serviceTime;
//    protected Vehicle delivery;

    public Customer() {
        id = 0;
        coord = new double[]{35.00, 35.00};
        demand = 0.0;
        readyTime = 0.0;
        dueTime = 230.0;
        serviceTime = 0.0;
    }

    public Customer(int id, double[] coord, double demand, double readyTime, double dueTime, double serviceTime) {
        this.id = id;
        this.coord = coord;
        this.demand = demand;
        this.readyTime = readyTime;
        this.dueTime = dueTime;
        this.serviceTime = serviceTime;
    }

    public int getId() {
        return id;
    }

    public double[] getCoord() {
        return coord;
    }

    public double getDemand() {
        return demand;
    }

    @Override
    public int compareTo(Customer o) {
        return Double.compare(this.readyTime, o.readyTime);
    }

    public boolean overlaps(Customer other) { //true if times overlap by more than 2
        if (this.compareTo(other)<0) return other.readyTime+1 < this.dueTime-1;
        else if (this.compareTo(other)>0) return this.readyTime+1 < other.dueTime-1;
        return true;
    }

    public double distance(Customer other) {
        double deltaX = coord[0] - other.getCoord()[0];
        double deltaY = coord[1] - other.getCoord()[1];
        double Xsqrd = deltaX * deltaX;
        double Ysqrd = deltaY * deltaY;
        return 1/Math.sqrt(Xsqrd + Ysqrd);
    }

    @Override
    public String toString() {
        return "Customer " + id + " at (" + coord[0] + ", " + coord[1] + ") demanding " + demand + " by "+dueTime+'\n';
    }
}
