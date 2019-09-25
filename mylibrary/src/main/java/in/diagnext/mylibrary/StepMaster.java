package in.diagnext.mylibrary;

public class StepMaster {
    public String sepMasterId;
    public String userId;
    public long date;
    public int steps;
    public float km;
    public float kcal;
    public int time;

    public StepMaster()
    {}

    public StepMaster(String sepMasterId, String userId, long date, int steps, float km, float kcal, int time) {
        this.sepMasterId = sepMasterId;
        this.userId = userId;
        this.date = date;
        this.steps = steps;
        this.km = km;
        this.kcal = kcal;
        this.time = time;
    }

    public StepMaster(long date, int steps) {
        this.date = date;
        this.steps = steps;

    }

    public String getSepMasterId() {
        return sepMasterId;
    }

    public void setSepMasterId(String sepMasterId) {
        this.sepMasterId = sepMasterId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public long getDate() {
        return date;
    }

    public void setDate(long date) {
        this.date = date;
    }

    public int getSteps() {
        return steps;
    }

    public void setSteps(int steps) {
        this.steps = steps;
    }

    public float getKm() {
        return km;
    }

    public void setKm(float km) {
        this.km = km;
    }

    public float getKcal() {
        return kcal;
    }

    public void setKcal(float kcal) {
        this.kcal = kcal;
    }

    public int getTime() {
        return time;
    }

    public void setTime(int time) {
        this.time = time;
    }
}
