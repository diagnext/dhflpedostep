package in.diagnext.mylibrary;

import java.text.DecimalFormat;

public class d_StepDetector {
    private static final int ACCEL_RING_SIZE = 50;
    private static final int VEL_RING_SIZE = 10;



    // change this threshold according to your sensitivity preferences
    private static float STEP_THRESHOLD = 50f;

    private static final int STEP_DELAY_NS = 250000000;

    private int accelRingCounter = 0;
    private float[] accelRingX = new float[ACCEL_RING_SIZE];
    private float[] accelRingY = new float[ACCEL_RING_SIZE];
    private float[] accelRingZ = new float[ACCEL_RING_SIZE];
    private int velRingCounter = 0;
    private float[] velRing = new float[VEL_RING_SIZE];
    private long lastStepTimeNs = 0;
    private float oldVelocityEstimate = 0;

    private d_StepListener listener;
    private int stepCount;
    private d_Database data;


    public void registerListener(d_StepListener listener, Float threshold) {
        this.listener = listener;
        STEP_THRESHOLD = threshold;
    }


    public void updateAccel(int step,boolean gender,String height,long timeNs, float x, float y, float z) {
        float[] currentAccel = new float[3];
        currentAccel[0] = x;
        currentAccel[1] = y;
        currentAccel[2] = z;

        // First step is to update our guess of where the global z vector is.
        accelRingCounter++;
        accelRingX[accelRingCounter % ACCEL_RING_SIZE] = currentAccel[0];
        accelRingY[accelRingCounter % ACCEL_RING_SIZE] = currentAccel[1];
        accelRingZ[accelRingCounter % ACCEL_RING_SIZE] = currentAccel[2];

        float[] worldZ = new float[3];
        worldZ[0] = d_SensorFilter.sum(accelRingX) / Math.min(accelRingCounter, ACCEL_RING_SIZE);
        worldZ[1] = d_SensorFilter.sum(accelRingY) / Math.min(accelRingCounter, ACCEL_RING_SIZE);
        worldZ[2] = d_SensorFilter.sum(accelRingZ) / Math.min(accelRingCounter, ACCEL_RING_SIZE);

        float normalization_factor = d_SensorFilter.norm(worldZ);

        worldZ[0] = worldZ[0] / normalization_factor;
        worldZ[1] = worldZ[1] / normalization_factor;
        worldZ[2] = worldZ[2] / normalization_factor;

        float currentZ = d_SensorFilter.dot(worldZ, currentAccel) - normalization_factor;
        velRingCounter++;
        velRing[velRingCounter % VEL_RING_SIZE] = currentZ;

        float velocityEstimate = d_SensorFilter.sum(velRing);



        if (velocityEstimate > STEP_THRESHOLD && oldVelocityEstimate <= STEP_THRESHOLD
                && (timeNs - lastStepTimeNs > STEP_DELAY_NS)) {


            float tMin=step*0.009f;


            //Distance
            float mof = 0.413f;
            if(gender)
                mof = 0.415f;

            //Height in feet
            float feetHeight = Integer.valueOf(height) * 0.0328084f;
            float stride = feetHeight * mof;

            //Stride
            float distance = stride * step;
            int roundDistance = Math.round(distance);

            //feet to km
            float feettokm = roundDistance * 0.0003048f;
            DecimalFormat df = new DecimalFormat("0.00");
            float rounderKm = Float.parseFloat(df.format(feettokm));
            float roundekcal = Float.parseFloat(df.format(step*0.045f));

            int hours=(int)(tMin/60);
            int mins =(int)(tMin%60);


            listener.step(timeNs);
            lastStepTimeNs = timeNs;
        }
        oldVelocityEstimate = velocityEstimate;
    }



}
