package com.nshmura.snappyimageviewer;

public class DegreeVelocityTracker {
    private float degreeVelocity;
    private float lastMsec;

    public float getDegreeVelocity() {
        return degreeVelocity;
    }

    public void addDegree(float degree) {
        float currMsec = getMsec();
        float deltaMsec = currMsec - lastMsec;
        lastMsec = currMsec;
        degreeVelocity = degree / deltaMsec;
    }

    public void reset() {
        lastMsec = getMsec();
    }

    public float getMsec() {
        return System.nanoTime() / 1000000f;
    }
}
