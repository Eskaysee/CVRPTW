public class Vector {

    private double x;
    private double y;
    private double z;

    public Vector(double axisX, double axisY, double axisZ) {
        x = axisX;
        y = axisY;
        z = axisZ;
    }

    public Vector() {
        x = 0;
        y = 0;
        z = 0;
    }

    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }

    public void set(int x, int y, int z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public void add(Vector velocity) {
        x += velocity.getX();
        y += velocity.getY();
        z += velocity.getZ();
    }

    private double getZ() {
        return z;
    }

    public void multiply(double inertia) {
        x *= inertia;
        y *= inertia;
        z *= inertia;
    }

    public void subtract(Vector pPosition) {
        x -= pPosition.getX();
        y -= pPosition.getY();
        z -= pPosition.getZ();
    }

    @Override
    public Vector clone() {
        return new Vector(x,y,z);
    }
}
