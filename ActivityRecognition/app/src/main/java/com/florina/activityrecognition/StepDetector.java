package com.florina.activityrecognition;

public class StepDetector {

    private static int n = 100;
    private static int m=20;
    private int count = 0;
    private int velocityCount = 0;
    private float step_threshold = 29;
    private long last_stepTime = 0;
    private long minTime = 250000000;
    private float lastVelocity = 0;
    private float[] accAlongX = new float[n];
    private float[] accAlongY = new float[n];
    private float[] accAlongZ = new float[n];
    private float[] velocity = new float[n];

    private StepListener listener;

    public void registerListener(StepListener listener) {
        this.listener = listener;
    }


    public void updateAccel(long timeNs, float x, float y, float z) {

        count++;

        accAlongX[count % n] = x;
        accAlongY[count % n] = y;
        accAlongZ[count % n] = z;


        float[] worldZ = new float[3];
        worldZ[0] = sumOfSensors(accAlongX) / Math.min(count, n);
        worldZ[1] = sumOfSensors(accAlongY) / Math.min(count, n);
        worldZ[2] = sumOfSensors(accAlongZ) / Math.min(count, n);

        float currentZ = multiply(worldZ, x,y,z);
        velocityCount++;
        velocity[velocityCount % m] = currentZ;

        float velocityValue = sumOfSensors(velocity);

        if (velocityValue > step_threshold && lastVelocity <= step_threshold
                && (timeNs - last_stepTime > minTime)) {
            listener.step(timeNs);
            last_stepTime = timeNs;
        }
        lastVelocity = velocityValue;
    }

    public static float sumOfSensors(float[] array){
        float sum = 0;
        for(int i=0;i<array.length;i++){
            sum+=array[i];
        }
        return sum;
    }

    public static float multiply(float[] zVector, float xAxis, float yAxis, float zAxis){
        float product = zVector[0]*xAxis + zVector[1]*yAxis + zVector[2]*zAxis;
        return product;
    }

}
