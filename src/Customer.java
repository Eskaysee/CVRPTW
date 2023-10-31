public final class Customer implements Comparable<Customer> {
    private final int id;
    private final double[] coord;
    private final double demand;
    private final double readyTime;
    private final double dueTime;
    private final double serviceTime;

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

    public double getReadyTime() {
        return readyTime;
    }

    public double getDueTime() {
        return dueTime;
    }

    public double getServiceTime() {
        return serviceTime;
    }

    @Override
    public int compareTo(Customer o) {
        return Double.compare(this.readyTime, o.readyTime);
    }

    public boolean overlaps(Customer other) {
        if (this.compareTo(other)<0) return other.readyTime < this.dueTime;
        else if (this.compareTo(other)>0) return this.readyTime < other.dueTime;
        return true;
    }

    @Override
    public String toString() {
        return "Customer " + id + " at (" + coord[0] + ", " + coord[1] + ") demanding " + demand + " by "+dueTime+'\n';
    }
}
